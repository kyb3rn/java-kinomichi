package app.models.managers;

import app.models.*;
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

public class DinnerReservationDataManager extends DataManager<DinnerReservationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, DinnerReservation> dinnerReservations = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, DinnerReservation> getDinnerReservations() {
        return Collections.unmodifiableSortedMap(this.dinnerReservations);
    }

    // ─── Utility methods ─── //

    public DinnerReservation getDinnerReservationWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        DinnerReservation camp = this.dinnerReservations.get(id);

        if (camp == null) {
            throw new NoResultForPrimaryKeyException("Aucune des réservations de repas enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return camp;
    }

    public void addDinnerReservation(DinnerReservation dinnerReservation) throws ModelException, DataManagerException {
        if (dinnerReservation == null) {
            throw new DataManagerException("L'réservation de repas à ajouter ne peut pas être nulle");
        }

        this.applyAutoIncrementIfPossible(dinnerReservation);

        if (!dinnerReservation.isValid()) {
            throw new ModelException("L'réservation de repas à ajouter n'est pas valide");
        }

        if (this.dinnerReservations.containsKey(dinnerReservation.getId())) {
            throw new DataManagerException("Une réservation de repas portant l'identifiant '%d' existe déjà".formatted(dinnerReservation.getId()));
        }

        if (this.isPersonRegisteredForDinner(dinnerReservation.getDinnerId(), dinnerReservation.getPerson())) {
            throw new DataManagerException("La personne portant l'identifiant '%d' a déjà une réservation pour le repas portant l'identifiant '%d'".formatted(dinnerReservation.getPersonId(), dinnerReservation.getDinnerId()));
        }

        this.validateNoOverlappingDinnerReservation(dinnerReservation);

        this.dinnerReservations.put(dinnerReservation.getId(), dinnerReservation);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public DinnerReservation addDinnerReservation(DinnerReservation.Data dinnerReservationData) throws ModelException, DataManagerException {
        if (dinnerReservationData == null) {
            throw new DataManagerException("L'réservation de repas à ajouter ne peut pas être nulle");
        }

        DinnerReservation dinnerReservation = new DinnerReservation();
        dinnerReservation.hydrate(dinnerReservationData);
        DataManagers.resolveModelReferences(dinnerReservation);

        this.addDinnerReservation(dinnerReservation);

        return dinnerReservation;
    }

    public void deleteDinnerReservation(int dinnerReservationId) throws ModelException, DataManagerException {
        DinnerReservation dinnerReservationToDelete = this.getDinnerReservationWithExceptions(dinnerReservationId);

        if (dinnerReservationToDelete.getDinner().getTimeSlot().getStart().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de supprimer une réservation d'un repas dont la date est déjà passée");
        }

        this.dinnerReservations.remove(dinnerReservationToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Camp getCampLinkedTo(DinnerReservation dinnerReservation) throws DataManagerException, ModelException {
        if (dinnerReservation == null) {
            throw new DataManagerException("La réservation de repas sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return DataManagers.get(DinnerDataManager.class).getCampLinkedTo(dinnerReservation.getDinner());
    }

    public List<DinnerReservation> getDinnerDinnerReservations(Dinner dinner) throws DataManagerException {
        if (dinner == null) {
            throw new DataManagerException("Le repas sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.dinnerReservations.values().stream().filter(dinnerReservation -> {
            try {
                return dinnerReservation.getDinnerId() == dinner.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<DinnerReservation> getCampDinnerReservations(Camp camp) throws DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        DinnerDataManager dinnerDataManager;
        try {
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Les repas n'ont pas pu être chargés", e);
        }

        List<Dinner> campDinners;
        try {
            campDinners = dinnerDataManager.getCampDinners(camp);
        } catch (DataManagerException e) {
            throw new DataManagerException("Les repas du stage n'ont pas pu être chargés", e);
        }

        return this.dinnerReservations.values().stream().filter(dinnerReservation -> {
            try {
                int dinnerReservationDinnerId = dinnerReservation.getDinnerId();
                return campDinners.stream().anyMatch(dinner -> dinner.getId() == dinnerReservationDinnerId);
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isPersonUsed(Person personToCheck) throws DataManagerException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(dinnerReservation -> {
            try {
                return dinnerReservation.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isPersonRegisteredForDinner(int dinnerId, Person personToCheck) throws DataManagerException, ModelException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        int finalDinnerId = IdentifiedModel.verifyId(dinnerId);

        return this.getModels().stream().anyMatch(dinnerReservation -> {
            try {
                return dinnerReservation.getDinnerId() == finalDinnerId && dinnerReservation.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isDinnerUsed(Dinner dinnerToCheck) throws DataManagerException {
        if (dinnerToCheck == null) {
            throw new DataManagerException("Le repas sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.getModels().stream().anyMatch(dinnerReservation -> {
            try {
                return dinnerReservation.getDinnerId() == dinnerToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    private void validateNoOverlappingDinnerReservation(DinnerReservation newReservation) throws DataManagerException, ModelException {
        Dinner targetDinner = newReservation.getDinner();
        Person person = newReservation.getPerson();

        for (DinnerReservation existingReservation : this.dinnerReservations.values()) {
            try {
                if (existingReservation.getCancellationDatetime() != null) {
                    continue;
                }
                if (existingReservation.getPersonId() == person.getId() && existingReservation.getDinnerId() != targetDinner.getId()) {
                    if (existingReservation.getDinner().getTimeSlot().overlaps(targetDinner.getTimeSlot())) {
                        throw new DataManagerException("La personne portant l'identifiant '%d' a déjà une réservation pour le repas %s dont l'horaire chevauche celui du repas ciblé".formatted(person.getId(), existingReservation.getDinner().toString()));
                    }
                }
            } catch (DataManagerException e) {
                throw e;
            } catch (ModelException _) {
                // Skip reservations with unresolved references
            }
        }
    }

    // ─── Overrides & inheritance ── //

    @Override
    public Collection<DinnerReservation> getModels() {
        return this.dinnerReservations.values();
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
        return this.dinnerReservations.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (DinnerReservation.Data dinnerReservationData : dataObject.dinnerReservations) {
            DinnerReservation dinnerReservation = new DinnerReservation();
            dinnerReservation.hydrate(dinnerReservationData);
            this.pendingModels.add(dinnerReservation);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof DinnerReservation dinnerReservation)) {
            throw new ModelException("Le manager '%s' attend un objet de type DinnerReservation, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addDinnerReservation(dinnerReservation);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<DinnerReservation.Data> dinnerReservations = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(DinnerReservationDataManager dinnerReservationDataManager) throws ModelException {
            for (DinnerReservation dinnerReservation : dinnerReservationDataManager.getModels()) {
                this.dinnerReservations.add(dinnerReservation.dehydrate());
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

            if (!obj.has("dinnerReservations")) {
                throw new StringParserException("Le champ 'dinnerReservations' est manquant");
            } else if (!obj.get("dinnerReservations").isJsonArray()) {
                throw new StringParserException("Le champ 'dinnerReservations' doit être un tableau");
            }

            JsonArray dinnerReservationsArray = obj.getAsJsonArray("dinnerReservations");
            for (JsonElement element : dinnerReservationsArray) {
                DinnerReservation.Data dinnerReservationData = new DinnerReservation.Data();
                dinnerReservationData.parseJson(element.toString());
                this.dinnerReservations.add(dinnerReservationData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                    .create()
                    .toJson(this);
        }

    }

}
