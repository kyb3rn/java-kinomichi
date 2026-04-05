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

public class DinnerDataManager extends DataManager<DinnerDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Dinner> dinners = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Dinner> getDinners() {
        return Collections.unmodifiableSortedMap(this.dinners);
    }

    // ─── Utility methods ─── //

    public Dinner getDinnerWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        Dinner camp = this.dinners.get(id);

        if (camp == null) {
            throw new NoResultForPrimaryKeyException("Aucun des repas enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return camp;
    }

    public Camp getCampLinkedTo(Dinner dinner) throws DataManagerException, ModelException {
        if (dinner == null) {
            throw new DataManagerException("Le repas sur lequel effectuer la recherche ne peut pas être nul");
        }

        return DataManagers.get(CampDataManager.class).getCampWithExceptions(dinner.getCampId());
    }

    public void addDinner(Dinner dinner) throws ModelException, DataManagerException {
        if (dinner == null) {
            throw new DataManagerException("Le repas à ajouter ne peut pas être nul");
        }

        this.applyAutoIncrementIfPossible(dinner);

        if (!dinner.isValid()) {
            throw new ModelException("Le repas à ajouter n'est pas valide");
        }

        dinner.validateTimeSlotWithinCampBounds();

        if (this.dinners.containsKey(dinner.getId())) {
            throw new DataManagerException("Un repas portant l'identifiant '%d' existe déjà".formatted(dinner.getId()));
        }

        this.dinners.put(dinner.getId(), dinner);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Dinner addDinner(Dinner.Data dinnerData) throws ModelException, DataManagerException {
        if (dinnerData == null) {
            throw new DataManagerException("Le repas à ajouter ne peut pas être nul");
        }

        Dinner dinner = new Dinner();
        dinner.hydrate(dinnerData);
        DataManagers.resolveModelReferences(dinner);

        this.addDinner(dinner);

        return dinner;
    }

    public void updateDinner(int dinnerId, Dinner modifiedDinner) throws ModelException, DataManagerException {
        dinnerId = IdentifiedModel.verifyId(dinnerId);

        if (modifiedDinner == null) {
            throw new ModelException("Le repas modifié ne peut pas être nul");
        }

        if (!modifiedDinner.isValid()) {
            throw new ModelException("Le repas modifié reçu n'est pas valide");
        }

        Dinner dinnerToModify = this.getDinnerWithExceptions(dinnerId);

        if (dinnerToModify.getId() != modifiedDinner.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        if (dinnerToModify.getCampId() != modifiedDinner.getCampId()) {
            throw new ModelException("Modifier le stage de référence d'un repas n'est pas autorisé");
        }

        if (dinnerToModify.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de modifier un repas dont la date de fin est déjà passée");
        }

        if (!dinnerToModify.getTimeSlot().getStart().equals(modifiedDinner.getTimeSlot().getStart()) || !dinnerToModify.getTimeSlot().getEnd().equals(modifiedDinner.getTimeSlot().getEnd())) {
            DinnerReservationDataManager dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
            if (dinnerReservationDataManager.isDinnerUsed(dinnerToModify)) {
                throw new DataManagerException("Impossible de modifier l'horaire d'un repas ayant des réservations");
            }
        }

        modifiedDinner.validateTimeSlotWithinCampBounds();

        dinnerToModify.hydrate(modifiedDinner.dehydrate());
        DataManagers.resolveModelReferences(dinnerToModify);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void deleteDinner(int dinnerId) throws ModelException, DataManagerException {
        Dinner dinnerToDelete = this.getDinnerWithExceptions(dinnerId);

        if (dinnerToDelete.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de supprimer un repas dont la date de fin est déjà passée");
        }

        DinnerReservationDataManager dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
        if (dinnerReservationDataManager.isDinnerUsed(dinnerToDelete)) {
            throw new DeletingReferencedDataManagerDataException("Ce repas est référencé par au moins une réservation et est donc impossible à supprimer.");
        }

        this.dinners.remove(dinnerToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public List<Dinner> getCampDinners(Camp camp) throws DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.dinners.values().stream().filter(dinner -> {
            try {
                return dinner.getCampId() == camp.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isPersonUsed(Person personToDelete) throws ModelException, DataManagerException {
        if (personToDelete == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        DinnerReservationDataManager dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
        return this.getModels().stream().anyMatch(dinner -> {
            try {
                return dinnerReservationDataManager.isPersonRegisteredForDinner(dinner.getId(), personToDelete);
            } catch (DataManagerException | ModelException e) {
                return false;
            }
        });
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Dinner> getModels() {
        return this.dinners.values();
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
        return this.dinners.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Dinner.Data dinnerData : dataObject.dinners) {
            Dinner dinner = new Dinner();
            dinner.hydrate(dinnerData);
            this.pendingModels.add(dinner);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Dinner dinner)) {
            throw new ModelException("Le manager '%s' attend un objet de type Dinner, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addDinner(dinner);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Dinner.Data> dinners = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(DinnerDataManager dinnerDataManager) throws ModelException {
            for (Dinner dinner : dinnerDataManager.getModels()) {
                this.dinners.add(dinner.dehydrate());
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

            if (!obj.has("dinners")) {
                throw new StringParserException("Le champ 'dinners' est manquant");
            } else if (!obj.get("dinners").isJsonArray()) {
                throw new StringParserException("Le champ 'dinners' doit être un tableau");
            }

            JsonArray dinnersArray = obj.getAsJsonArray("dinners");
            for (JsonElement element : dinnersArray) {
                Dinner.Data dinnerData = new Dinner.Data();
                dinnerData.parseJson(element.toString());
                this.dinners.add(dinnerData);
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
