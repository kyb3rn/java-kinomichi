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

public class LodgingDataManager extends DataManager<LodgingDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Lodging> lodgings = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Lodging> getLodgings() {
        return Collections.unmodifiableSortedMap(this.lodgings);
    }

    // ─── Utility methods ─── //

    public Lodging getLodgingWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        Lodging lodging = this.lodgings.get(id);

        if (lodging == null) {
            throw new NoResultForPrimaryKeyException("Aucun des hébergements enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return lodging;
    }

    public Camp getCampLinkedTo(Lodging lodging) throws DataManagerException, ModelException {
        if (lodging == null) {
            throw new DataManagerException("L'hébergement sur lequel effectuer la recherche ne peut pas être nul");
        }

        return DataManagers.get(CampDataManager.class).getCampWithExceptions(lodging.getCampId());
    }

    public void addLodging(Lodging lodging) throws ModelException, DataManagerException {
        if (lodging == null) {
            throw new DataManagerException("L'hébergement à ajouter ne peut pas être nul");
        }

        this.applyAutoIncrementIfPossible(lodging);

        if (!lodging.isValid()) {
            throw new ModelException("L'hébergement à ajouter n'est pas valide");
        }

        lodging.validateTimeSlotWithinCampBounds();

        if (this.lodgings.containsKey(lodging.getId())) {
            throw new DataManagerException("Un hébergement portant l'identifiant '%d' existe déjà".formatted(lodging.getId()));
        }

        this.lodgings.put(lodging.getId(), lodging);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Lodging addLodging(Lodging.Data lodgingData) throws ModelException, DataManagerException {
        if (lodgingData == null) {
            throw new DataManagerException("L'hébergement à ajouter ne peut pas être nul");
        }

        Lodging lodging = new Lodging();
        lodging.hydrate(lodgingData);
        DataManagers.resolveModelReferences(lodging);

        this.addLodging(lodging);

        return lodging;
    }

    public void updateLodging(int lodgingId, Lodging modifiedLodging) throws ModelException, DataManagerException {
        lodgingId = IdentifiedModel.verifyId(lodgingId);

        if (modifiedLodging == null) {
            throw new ModelException("L'hébergement modifié ne peut pas être nul");
        }

        if (!modifiedLodging.isValid()) {
            throw new ModelException("L'hébergement modifié reçu n'est pas valide");
        }

        Lodging lodgingToModify = this.getLodgingWithExceptions(lodgingId);

        if (lodgingToModify.getId() != modifiedLodging.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        if (lodgingToModify.getCampId() != modifiedLodging.getCampId()) {
            throw new ModelException("Modifier le stage de référence d'un hébergement n'est pas autorisé");
        }

        if (lodgingToModify.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de modifier un hébergement dont la date de fin est déjà passée");
        }

        if (!lodgingToModify.getTimeSlot().getStart().equals(modifiedLodging.getTimeSlot().getStart()) || !lodgingToModify.getTimeSlot().getEnd().equals(modifiedLodging.getTimeSlot().getEnd())) {
            LodgingReservationDataManager lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
            if (lodgingReservationDataManager.isLodgingUsed(lodgingToModify)) {
                throw new DataManagerException("Impossible de modifier l'horaire d'un hébergement ayant des réservations");
            }
        }

        modifiedLodging.validateTimeSlotWithinCampBounds();

        lodgingToModify.hydrate(modifiedLodging.dehydrate());
        DataManagers.resolveModelReferences(lodgingToModify);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void deleteLodging(int lodgingId) throws ModelException, DataManagerException {
        Lodging lodgingToDelete = this.getLodgingWithExceptions(lodgingId);

        if (lodgingToDelete.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de supprimer un hébergement dont la date de fin est déjà passée");
        }

        LodgingReservationDataManager lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
        if (lodgingReservationDataManager.isLodgingUsed(lodgingToDelete)) {
            throw new DeletingReferencedDataManagerDataException("Cet hébergement est référencé par au moins une réservation et est donc impossible à supprimer.");
        }

        this.lodgings.remove(lodgingToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public List<Lodging> getCampLodgings(Camp camp) throws DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.lodgings.values().stream().filter(lodging -> {
            try {
                return lodging.getCampId() == camp.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isPersonUsed(Person personToDelete) throws ModelException, DataManagerException {
        if (personToDelete == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        LodgingReservationDataManager lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
        return this.getModels().stream().anyMatch(lodging -> {
            try {
                return lodgingReservationDataManager.isPersonReservedForLodging(lodging.getId(), personToDelete);
            } catch (DataManagerException | ModelException e) {
                return false;
            }
        });
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Lodging> getModels() {
        return this.lodgings.values();
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
        return this.lodgings.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Lodging.Data lodgingData : dataObject.lodgings) {
            Lodging lodging = new Lodging();
            lodging.hydrate(lodgingData);
            this.pendingModels.add(lodging);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Lodging lodging)) {
            throw new ModelException("Le manager '%s' attend un objet de type Lodging, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addLodging(lodging);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Lodging.Data> lodgings = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(LodgingDataManager lodgingDataManager) throws ModelException {
            for (Lodging lodging : lodgingDataManager.getModels()) {
                this.lodgings.add(lodging.dehydrate());
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

            if (!obj.has("lodgings")) {
                throw new StringParserException("Le champ 'lodgings' est manquant");
            } else if (!obj.get("lodgings").isJsonArray()) {
                throw new StringParserException("Le champ 'lodgings' doit être un tableau");
            }

            JsonArray lodgingsArray = obj.getAsJsonArray("lodgings");
            for (JsonElement element : lodgingsArray) {
                Lodging.Data lodgingData = new Lodging.Data();
                lodgingData.parseJson(element.toString());
                this.lodgings.add(lodgingData);
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
