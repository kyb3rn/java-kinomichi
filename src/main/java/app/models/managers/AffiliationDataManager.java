package app.models.managers;

import app.models.*;
import com.google.gson.*;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class AffiliationDataManager extends DataManager<AffiliationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Affiliation> affiliations = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Affiliation> getAffiliations() {
        return this.affiliations;
    }

    // ─── Utility methods ─── //

    public Affiliation getAffiliation(Integer personId) {
        return this.affiliations.get(personId);
    }

    public Affiliation getAffiliationWithExceptions(int personId) throws NoResultForPrimaryKeyException {
        Affiliation affiliation = this.getAffiliation(personId);

        if (affiliation == null) {
            throw new NoResultForPrimaryKeyException("Aucune des affiliations enregistrées ne référence une personne portant l'identifiant '%d'".formatted(personId));
        }

        return affiliation;
    }

    public void addAffiliation(Affiliation affiliation) throws ModelException, DataManagerException {
        if (!affiliation.isValid()) {
            throw new ModelException("L'affiliation qui a voulu être ajoutée n'est pas valide");
        }

        if (this.affiliations.containsKey(affiliation.getPerson().getId())) {
            throw new DataManagerException("Une affiliation référençant une personne portant l'identifiant '%d' existe déjà".formatted(affiliation.getPersonId()));
        }

        this.affiliations.put(affiliation.getPersonId(), affiliation);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Affiliation addAffiliation(Affiliation.Data affiliationData) throws ModelException, DataManagerException {
        Affiliation affiliation = new Affiliation();
        affiliation.hydrate(affiliationData);
        DataManagers.resolveModelReferences(affiliation);

        this.addAffiliation(affiliation);

        return affiliation;
    }

    public void updateAffiliation(Affiliation modifiedAffiliation) throws ModelException, DataManagerException {
        if (modifiedAffiliation == null) {
            throw new ModelException("L'affiliation modifiée ne peut pas être nul");
        }

        if (!modifiedAffiliation.isValid()) {
            throw new ModelException("L'affiliation modifiée reçue n'est pas valide");
        }

        Affiliation affiliationToModify = this.getAffiliationWithExceptions(modifiedAffiliation.getPersonId());

        affiliationToModify.hydrate(modifiedAffiliation.dehydrate());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void deleteAffiliation(int personId) throws ModelException, DataManagerException {
        Affiliation affiliationToDelete = this.getAffiliationWithExceptions(personId);

        this.affiliations.remove(affiliationToDelete.getPersonId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public boolean isClubUsed(Club clubToDelete) throws ModelException {
        if (clubToDelete == null) {
            throw new ModelException("Le club à chercher ne peut pas être nul");
        }

        return this.affiliations.values().stream().anyMatch(affiliation -> {
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
            for (Affiliation affiliation : affiliationDataManager.getAffiliations().values()) {
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
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
