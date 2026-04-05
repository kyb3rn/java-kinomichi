package app.models.managers;

import app.models.*;
import app.utils.elements.time.TimeSlot;
import com.google.gson.*;
import java.time.Instant;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AffiliationDataManager extends DataManager<AffiliationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Affiliation> affiliations = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Affiliation> getAffiliations() {
        return Collections.unmodifiableSortedMap(this.affiliations);
    }

    // ─── Utility methods ─── //

    public Affiliation getAffiliationWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        Affiliation camp = this.affiliations.get(id);

        if (camp == null) {
            throw new NoResultForPrimaryKeyException("Aucune des affiliations enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return camp;
    }

    public List<Affiliation> getPersonAffiliations(int idPerson) throws ModelException {
        int finalIdPerson = IdentifiedModel.verifyId(idPerson);

        return this.getModels().stream().filter(affiliation -> {
            try {
                return affiliation.getPersonId() == finalIdPerson;
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<Affiliation> getAffiliationsDuringTimeSlot(TimeSlot timeSlot) throws DataManagerException {
        if (timeSlot == null) {
            throw new DataManagerException("La période de temps sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().filter(affiliation -> affiliation.getValidityPeriod().overlaps(timeSlot)).toList();
    }

    public List<Affiliation> getPersonAffiliationsDuringTimeSlot(Person person, TimeSlot timeSlot) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        if (timeSlot == null) {
            throw new DataManagerException("La période de temps sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().filter(affiliation -> {
            try {
                return affiliation.getPersonId() == person.getId() && affiliation.getValidityPeriod().overlaps(timeSlot);
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public void addAffiliation(Affiliation affiliation) throws ModelException, DataManagerException {
        if (affiliation == null) {
            throw new DataManagerException("L'affiliation à ajouter ne peut pas être nulle");
        }

        this.applyAutoIncrementIfPossible(affiliation);

        if (!affiliation.isValid()) {
            throw new ModelException("L'affiliation à ajouter n'est pas valide");
        }

        if (this.affiliations.containsKey(affiliation.getId())) {
            throw new DataManagerException("Une affiliation portant l'identifiant '%d' existe déjà".formatted(affiliation.getId()));
        }

        if (!this.getPersonAffiliationsDuringTimeSlot(affiliation.getPerson(), affiliation.getValidityPeriod()).isEmpty()) {
            throw new ModelException("La période de validité de l'affiliation à ajouter se superpose avec celle d'une autre affiliation de la personne à laquelle elle souhaite faire référence");
        }

        this.affiliations.put(affiliation.getId(), affiliation);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Affiliation addAffiliation(Affiliation.Data affiliationData) throws ModelException, DataManagerException {
        if (affiliationData == null) {
            throw new DataManagerException("L'affiliation à ajouter ne peut pas être nulle");
        }

        Affiliation affiliation = new Affiliation();
        affiliation.hydrate(affiliationData);
        DataManagers.resolveModelReferences(affiliation);

        this.addAffiliation(affiliation);

        return affiliation;
    }

    public void updateAffiliation(int affiliationId, Affiliation modifiedAffiliation) throws ModelException, DataManagerException {
        affiliationId = IdentifiedModel.verifyId(affiliationId);

        if (modifiedAffiliation == null) {
            throw new ModelException("L'affiliation modifiée ne peut pas être nulle");
        }

        if (!modifiedAffiliation.isValid()) {
            throw new ModelException("L'affiliation modifiée reçue n'est pas valide");
        }

        Affiliation affiliationToModify = this.getAffiliationWithExceptions(affiliationId);

        if (affiliationToModify.getId() != modifiedAffiliation.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        if (affiliationToModify.getPersonId() != modifiedAffiliation.getPersonId()) {
            SessionTrainerDataManager sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
            List<SessionTrainer> oldPersonSessionTrainers = sessionTrainerDataManager.getPersonSessionTrainers(affiliationToModify.getPerson());
            for (SessionTrainer sessionTrainer : oldPersonSessionTrainers) {
                TimeSlot campTimeSlot = sessionTrainer.getSession().getCamp().getTimeSlot();
                if (affiliationToModify.getValidityPeriod().overlaps(campTimeSlot)) {
                    List<Affiliation> otherCoveringAffiliations = this.getPersonAffiliationsDuringTimeSlot(affiliationToModify.getPerson(), campTimeSlot)
                            .stream().filter(affiliation -> affiliation.getId() != affiliationToModify.getId()).toList();
                    if (otherCoveringAffiliations.isEmpty()) {
                        throw new DataManagerException("Impossible de changer la personne de cette affiliation : l'ancienne personne est formateur dans au moins une session dont le stage tombe durant la période de validité de cette affiliation");
                    }
                }
            }
        }

        if (!this.getPersonAffiliationsDuringTimeSlot(modifiedAffiliation.getPerson(), modifiedAffiliation.getValidityPeriod()).isEmpty()) {
            throw new ModelException("La période de validité de l'affiliation modifiée se superpose avec celle d'une autre affiliation de la personne à laquelle elle souhaite faire référence");
        }

        affiliationToModify.hydrate(modifiedAffiliation.dehydrate());
        DataManagers.resolveModelReferences(affiliationToModify);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void deleteAffiliation(int id) throws ModelException, DataManagerException {
        Affiliation affiliationToDelete = this.getAffiliationWithExceptions(id);

        SessionTrainerDataManager sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
        List<SessionTrainer> personSessionTrainers = sessionTrainerDataManager.getPersonSessionTrainers(affiliationToDelete.getPerson());
        for (SessionTrainer sessionTrainer : personSessionTrainers) {
            TimeSlot campTimeSlot = sessionTrainer.getSession().getCamp().getTimeSlot();
            if (affiliationToDelete.getValidityPeriod().overlaps(campTimeSlot)) {
                List<Affiliation> otherCoveringAffiliations = this.getPersonAffiliationsDuringTimeSlot(affiliationToDelete.getPerson(), campTimeSlot)
                        .stream().filter(affiliation -> affiliation.getId() != affiliationToDelete.getId()).toList();
                if (otherCoveringAffiliations.isEmpty()) {
                    throw new DeletingReferencedDataManagerDataException("Impossible de supprimer cette affiliation : la personne est formateur dans au moins une session dont le stage tombe durant la période de validité de cette affiliation");
                }
            }
        }

        this.affiliations.remove(affiliationToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public boolean isPersonUsed(Person personToDelete) throws DataManagerException {
        if (personToDelete == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(affiliation -> {
            try {
                return affiliation.getPersonId() == personToDelete.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isClubUsed(Club clubToDelete) throws DataManagerException {
        if (clubToDelete == null) {
            throw new DataManagerException("Le club sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.getModels().stream().anyMatch(affiliation -> {
            try {
                return affiliation.getClubId() == clubToDelete.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Affiliation> getModels() {
        return this.affiliations.values();
    }

    @Override
    public void init() throws LoadDataManagerDataException {
        this.defaultJsonInit(new Data());
    }

    @Override
    public void export() throws DataManagerException, ModelException {
        Data data = new Data(this);
        super.export(data);
    }

    @Override
    public void export(FileType fileType) throws DataManagerException, ModelException {
        Data data = new Data(this);
        super.export(fileType, data);
    }

    @Override
    public int count() {
        return this.affiliations.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Affiliation.Data affiliationData : dataObject.affiliations) {
            Affiliation affiliation = new Affiliation();
            affiliation.hydrate(affiliationData);
            this.pendingModels.add(affiliation);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Affiliation affiliation)) {
            throw new ModelException("Le manager '%s' attend un objet de type Affiliation, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addAffiliation(affiliation);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Affiliation.Data> affiliations = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(AffiliationDataManager affiliationDataManager) throws ModelException {
            for (Affiliation affiliation : affiliationDataManager.getModels()) {
                this.affiliations.add(affiliation.dehydrate());
            }
        }

        public Data() {}

        // ─── Overrides & inheritance ─── //

        @Override
        public void parseJson(String json) throws ParserException {
            JsonObject obj;
            try {
                JsonElement parsed = JsonParser.parseString(json);

                if (parsed.isJsonNull()) {
                    return;
                }

                obj = parsed.getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                throw new StringParserException("Le JSON reçu n'est pas un objet valide (%s)".formatted(e.getMessage()), e);
            }

            if (!obj.has("affiliations")) {
                throw new StringParserException("Le champ 'affiliations' est manquant");
            } else if (!obj.get("affiliations").isJsonArray()) {
                throw new StringParserException("Le champ 'affiliations' doit être un tableau");
            }

            JsonArray affiliationsArray = obj.getAsJsonArray("affiliations");
            for (JsonElement element : affiliationsArray) {
                Affiliation.Data affiliationData = new Affiliation.Data();
                affiliationData.parseJson(element.toString());
                this.affiliations.add(affiliationData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                    .create()
                    .toJson(this);
        }

    }

}
