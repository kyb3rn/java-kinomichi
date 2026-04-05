package app.models.managers;

import app.models.*;
import com.google.gson.*;
import java.time.Instant;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;

import java.util.Collection;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class CampDataManager extends DataManager<CampDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Camp> camps = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Camp> getCamps() {
        return Collections.unmodifiableSortedMap(this.camps);
    }

    // ─── Utility methods ─── //

    public Camp getCampWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        Camp camp = this.camps.get(id);

        if (camp == null) {
            throw new NoResultForPrimaryKeyException("Aucun des stages enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return camp;
    }

    public void addCamp(Camp camp) throws ModelException, DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage à ajouter ne peut pas être nul");
        }

        this.applyAutoIncrementIfPossible(camp);

        if (!camp.isValid()) {
            throw new ModelException("Le stage à ajouter n'est pas valide");
        }

        if (this.camps.containsKey(camp.getId())) {
            throw new DataManagerException("Un stage portant l'identifiant '%d' existe déjà".formatted(camp.getId()));
        }

        this.camps.put(camp.getId(), camp);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Camp addCamp(Camp.Data campData) throws ModelException, DataManagerException {
        if (campData == null) {
            throw new DataManagerException("Le stage à ajouter ne peut pas être nul");
        }

        Camp camp = new Camp();
        camp.hydrate(campData);
        DataManagers.resolveModelReferences(camp);

        this.addCamp(camp);

        return camp;
    }

    public void updateCamp(int campId, Camp modifiedCamp) throws ModelException, DataManagerException {
        campId = IdentifiedModel.verifyId(campId);

        if (modifiedCamp == null) {
            throw new ModelException("Le stage modifié ne peut pas être nul");
        }

        if (!modifiedCamp.isValid()) {
            throw new ModelException("Le stage modifié reçu n'est pas valide");
        }

        Camp campToModify = this.getCampWithExceptions(campId);

        if (campToModify.getId() != modifiedCamp.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        if (campToModify.getAddressId() != modifiedCamp.getAddressId()) {
            throw new ModelException("Modifier l'identifiant de référence de l'adresse d'un stage n'est pas autorisé");
        }

        if (campToModify.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de modifier un stage déjà terminé");
        }

        // Validate that all scheduled sub-elements fit within the new TimeSlot
        this.validateScheduledItemsWithinTimeSlot(campToModify, modifiedCamp.getTimeSlot());

        campToModify.hydrate(modifiedCamp.dehydrate());
        DataManagers.resolveModelReferences(campToModify);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    private void validateScheduledItemsWithinTimeSlot(Camp camp, app.utils.elements.time.TimeSlot newTimeSlot) throws ModelException, DataManagerException {
        DinnerDataManager dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        List<Dinner> campDinners = dinnerDataManager.getCampDinners(camp);

        for (Dinner dinner : campDinners) {
            if (!newTimeSlot.contains(dinner.getTimeSlot())) {
                throw new ModelVerificationException("Impossible de modifier la période du stage : le repas %s n'est plus contenu dans la nouvelle période".formatted(dinner.toString()));
            }
        }

        LodgingDataManager lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        List<Lodging> campLodgings = lodgingDataManager.getCampLodgings(camp);

        for (Lodging lodging : campLodgings) {
            if (!newTimeSlot.contains(lodging.getTimeSlot())) {
                throw new ModelVerificationException("Impossible de modifier la période du stage : l'hébergement %s n'est plus contenu dans la nouvelle période".formatted(lodging.toString()));
            }
        }

        SessionDataManager sessionDataManager = DataManagers.get(SessionDataManager.class);
        List<Session> campSessions = sessionDataManager.getCampSessions(camp);

        for (Session session : campSessions) {
            if (!newTimeSlot.contains(session.getTimeSlot())) {
                throw new ModelVerificationException("Impossible de modifier la période du stage : la session %s n'est plus contenue dans la nouvelle période".formatted(session.toString()));
            }
        }
    }

    public void deleteCamp(int campId) throws ModelException, DataManagerException {
        Camp campToDelete = this.getCampWithExceptions(campId);

        if (campToDelete.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de supprimer un stage déjà terminé");
        }

        if (DataManagers.get(InvitationDataManager.class).isCampUsed(campToDelete)) {
            throw new DeletingReferencedDataManagerDataException("Ce stage est référencé par au moins une invitation et est donc impossible à supprimer.");
        }

        if (DataManagers.get(DinnerDataManager.class).getCampDinners(campToDelete).size() > 0) {
            throw new DeletingReferencedDataManagerDataException("Ce stage est référencé par au moins un repas et est donc impossible à supprimer.");
        }

        if (DataManagers.get(LodgingDataManager.class).getCampLodgings(campToDelete).size() > 0) {
            throw new DeletingReferencedDataManagerDataException("Ce stage est référencé par au moins un hébergement et est donc impossible à supprimer.");
        }

        if (DataManagers.get(SessionDataManager.class).getCampSessions(campToDelete).size() > 0) {
            throw new DeletingReferencedDataManagerDataException("Ce stage est référencé par au moins une session et est donc impossible à supprimer.");
        }

        this.camps.remove(campToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public boolean isPersonUsed(Person personToDelete) throws ModelException, DataManagerException {
        if (personToDelete == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        // Check invitations
        if (DataManagers.get(InvitationDataManager.class).isPersonUsed(personToDelete)) {
            return true;
        }

        // Check sessions
        if (DataManagers.get(SessionDataManager.class).isPersonUsed(personToDelete)) {
            return true;
        }

        // Check lodgings
        if (DataManagers.get(LodgingDataManager.class).isPersonUsed(personToDelete)) {
            return true;
        }

        // Check dinners
        return DataManagers.get(DinnerDataManager.class).isPersonUsed(personToDelete);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Camp> getModels() {
        return this.camps.values();
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
        return this.camps.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Camp.Data campData : dataObject.camps) {
            Camp camp = new Camp();
            camp.hydrate(campData);
            this.pendingModels.add(camp);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Camp camp)) {
            throw new ModelException("Le manager '%s' attend un objet de type Camp, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addCamp(camp);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Camp.Data> camps = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(CampDataManager campDataManager) throws ModelException {
            for (Camp camp : campDataManager.getModels()) {
                this.camps.add(camp.dehydrate());
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

            if (!obj.has("camps")) {
                throw new StringParserException("Le champ 'camps' est manquant");
            } else if (!obj.get("camps").isJsonArray()) {
                throw new StringParserException("Le champ 'camps' doit être un tableau");
            }

            JsonArray campsArray = obj.getAsJsonArray("camps");
            for (JsonElement element : campsArray) {
                Camp.Data campData = new Camp.Data();
                campData.parseJson(element.toString());
                this.camps.add(campData);
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
