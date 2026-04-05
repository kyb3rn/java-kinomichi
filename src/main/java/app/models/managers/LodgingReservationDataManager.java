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

public class LodgingReservationDataManager extends DataManager<LodgingReservationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, LodgingReservation> lodgingReservations = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, LodgingReservation> getLodgingReservations() {
        return Collections.unmodifiableSortedMap(this.lodgingReservations);
    }

    // ─── Utility methods ─── //

    public LodgingReservation getLodgingReservationWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        LodgingReservation lodgingReservation = this.lodgingReservations.get(id);

        if (lodgingReservation == null) {
            throw new NoResultForPrimaryKeyException("Aucune des réservations d'hébergement enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return lodgingReservation;
    }

    public void addLodgingReservation(LodgingReservation lodgingReservation) throws ModelException, DataManagerException {
        if (lodgingReservation == null) {
            throw new DataManagerException("La réservation d'hébergement à ajouter ne peut pas être nulle");
        }

        this.applyAutoIncrementIfPossible(lodgingReservation);

        if (!lodgingReservation.isValid()) {
            throw new ModelException("La réservation d'hébergement à ajouter n'est pas valide");
        }

        if (this.lodgingReservations.containsKey(lodgingReservation.getId())) {
            throw new DataManagerException("Une réservation d'hébergement portant l'identifiant '%d' existe déjà".formatted(lodgingReservation.getId()));
        }

        if (this.isPersonReservedForLodging(lodgingReservation.getLodgingId(), lodgingReservation.getPerson())) {
            throw new DataManagerException("La personne portant l'identifiant '%d' a déjà une réservation pour l'hébergement portant l'identifiant '%d'".formatted(lodgingReservation.getPersonId(), lodgingReservation.getLodgingId()));
        }

        this.validateNoOverlappingLodgingReservation(lodgingReservation);

        this.lodgingReservations.put(lodgingReservation.getId(), lodgingReservation);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public LodgingReservation addLodgingReservation(LodgingReservation.Data lodgingReservationData) throws ModelException, DataManagerException {
        if (lodgingReservationData == null) {
            throw new DataManagerException("La réservation d'hébergement à ajouter ne peut pas être nulle");
        }

        LodgingReservation lodgingReservation = new LodgingReservation();
        lodgingReservation.hydrate(lodgingReservationData);
        DataManagers.resolveModelReferences(lodgingReservation);

        this.addLodgingReservation(lodgingReservation);

        return lodgingReservation;
    }

    public void deleteLodgingReservation(int lodgingReservationId) throws ModelException, DataManagerException {
        LodgingReservation lodgingReservationToDelete = this.getLodgingReservationWithExceptions(lodgingReservationId);

        if (lodgingReservationToDelete.getLodging().getTimeSlot().getStart().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de supprimer une réservation d'un hébergement dont la date est déjà passée");
        }

        this.lodgingReservations.remove(lodgingReservationToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Camp getCampLinkedTo(LodgingReservation lodgingReservation) throws DataManagerException, ModelException {
        if (lodgingReservation == null) {
            throw new DataManagerException("La réservation d'hébergement sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return DataManagers.get(LodgingDataManager.class).getCampLinkedTo(lodgingReservation.getLodging());
    }

    public List<LodgingReservation> getLodgingLodgingReservations(Lodging lodging) throws DataManagerException {
        if (lodging == null) {
            throw new DataManagerException("L'hébergement sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.lodgingReservations.values().stream().filter(lodgingReservation -> {
            try {
                return lodgingReservation.getLodgingId() == lodging.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<LodgingReservation> getCampLodgingReservations(Camp camp) throws DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        LodgingDataManager lodgingDataManager;
        try {
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Les hébergements n'ont pas pu être chargés", e);
        }

        List<Lodging> campLodgings;
        try {
            campLodgings = lodgingDataManager.getCampLodgings(camp);
        } catch (DataManagerException e) {
            throw new DataManagerException("Les hébergements du stage n'ont pas pu être chargés", e);
        }

        return this.lodgingReservations.values().stream().filter(lodgingReservation -> {
            try {
                int lodgingReservationLodgingId = lodgingReservation.getLodgingId();
                return campLodgings.stream().anyMatch(lodging -> lodging.getId() == lodgingReservationLodgingId);
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isPersonUsed(Person personToCheck) throws DataManagerException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(lodgingReservation -> {
            try {
                return lodgingReservation.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isPersonReservedForLodging(int lodgingId, Person personToCheck) throws DataManagerException, ModelException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        int finalLodgingId = IdentifiedModel.verifyId(lodgingId);

        return this.getModels().stream().anyMatch(lodgingReservation -> {
            try {
                return lodgingReservation.getLodgingId() == finalLodgingId && lodgingReservation.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isLodgingUsed(Lodging lodgingToCheck) throws DataManagerException {
        if (lodgingToCheck == null) {
            throw new DataManagerException("L'hébergement sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.getModels().stream().anyMatch(lodgingReservation -> {
            try {
                return lodgingReservation.getLodgingId() == lodgingToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    private void validateNoOverlappingLodgingReservation(LodgingReservation newReservation) throws DataManagerException, ModelException {
        Lodging targetLodging = newReservation.getLodging();
        Person person = newReservation.getPerson();

        for (LodgingReservation existingReservation : this.lodgingReservations.values()) {
            try {
                if (existingReservation.getCancellationDatetime() != null) {
                    continue;
                }
                if (existingReservation.getPersonId() == person.getId() && existingReservation.getLodgingId() != targetLodging.getId()) {
                    if (existingReservation.getLodging().getTimeSlot().overlaps(targetLodging.getTimeSlot())) {
                        throw new DataManagerException("La personne portant l'identifiant '%d' a déjà une réservation pour l'hébergement %s dont l'horaire chevauche celui de l'hébergement ciblé".formatted(person.getId(), existingReservation.getLodging().toString()));
                    }
                }
            } catch (DataManagerException e) {
                throw e;
            } catch (ModelException _) {
                // Skip reservations with unresolved references
            }
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<LodgingReservation> getModels() {
        return this.lodgingReservations.values();
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
        return this.lodgingReservations.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (LodgingReservation.Data lodgingReservationData : dataObject.lodgingReservations) {
            LodgingReservation lodgingReservation = new LodgingReservation();
            lodgingReservation.hydrate(lodgingReservationData);
            this.pendingModels.add(lodgingReservation);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof LodgingReservation lodgingReservation)) {
            throw new ModelException("Le manager '%s' attend un objet de type LodgingReservation, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addLodgingReservation(lodgingReservation);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<LodgingReservation.Data> lodgingReservations = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(LodgingReservationDataManager lodgingReservationDataManager) throws ModelException {
            for (LodgingReservation lodgingReservation : lodgingReservationDataManager.getModels()) {
                this.lodgingReservations.add(lodgingReservation.dehydrate());
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

            if (!obj.has("lodgingReservations")) {
                throw new StringParserException("Le champ 'lodgingReservations' est manquant");
            } else if (!obj.get("lodgingReservations").isJsonArray()) {
                throw new StringParserException("Le champ 'lodgingReservations' doit être un tableau");
            }

            JsonArray lodgingReservationsArray = obj.getAsJsonArray("lodgingReservations");
            for (JsonElement element : lodgingReservationsArray) {
                LodgingReservation.Data lodgingReservationData = new LodgingReservation.Data();
                lodgingReservationData.parseJson(element.toString());
                this.lodgingReservations.add(lodgingReservationData);
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
