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

public class SessionDataManager extends DataManager<SessionDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Session> sessions = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Session> getSessions() {
        return Collections.unmodifiableSortedMap(this.sessions);
    }

    // ─── Utility methods ─── //

    public Session getSessionWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        Session session = this.sessions.get(id);

        if (session == null) {
            throw new NoResultForPrimaryKeyException("Aucune des sessions enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return session;
    }

    public void addSession(Session session) throws ModelException, DataManagerException {
        if (session == null) {
            throw new DataManagerException("La session à ajouter ne peut pas être nulle");
        }

        this.applyAutoIncrementIfPossible(session);

        if (!session.isValid()) {
            throw new ModelException("La session à ajouter n'est pas valide");
        }

        if (this.sessions.containsKey(session.getId())) {
            throw new DataManagerException("Une session portant l'identifiant '%d' existe déjà".formatted(session.getId()));
        }

        this.sessions.put(session.getId(), session);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Session addSession(Session.Data sessionData) throws ModelException, DataManagerException {
        if (sessionData == null) {
            throw new DataManagerException("La session à ajouter ne peut pas être nulle");
        }

        Session session = new Session();
        session.hydrate(sessionData);
        DataManagers.resolveModelReferences(session);

        this.addSession(session);

        return session;
    }

    public void updateSession(int sessionId, Session modifiedSession) throws ModelException, DataManagerException {
        sessionId = IdentifiedModel.verifyId(sessionId);

        if (modifiedSession == null) {
            throw new ModelException("La session modifiée ne peut pas être nulle");
        }

        if (!modifiedSession.isValid()) {
            throw new ModelException("La session modifiée reçue n'est pas valide");
        }

        Session sessionToModify = this.getSessionWithExceptions(sessionId);

        if (sessionToModify.getId() != modifiedSession.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        if (sessionToModify.getCampId() != modifiedSession.getCampId()) {
            throw new ModelException("Modifier le stage de référence d'une session n'est pas autorisé");
        }

        // If the timeSlot changes, both trainers and registrations must be empty
        boolean timeSlotChanged = !sessionToModify.getTimeSlot().getStart().equals(modifiedSession.getTimeSlot().getStart())
                || !sessionToModify.getTimeSlot().getEnd().equals(modifiedSession.getTimeSlot().getEnd());

        if (timeSlotChanged) {
            SessionTrainerDataManager sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
            if (sessionTrainerDataManager.isSessionUsed(sessionToModify)) {
                throw new DataManagerException("Impossible de modifier le créneau horaire d'une session ayant des formateurs assignés");
            }

            SessionRegistrationDataManager sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
            if (sessionRegistrationDataManager.isSessionUsed(sessionToModify)) {
                throw new DataManagerException("Impossible de modifier le créneau horaire d'une session ayant des inscrits");
            }
        }

        sessionToModify.hydrate(modifiedSession.dehydrate());
        DataManagers.resolveModelReferences(sessionToModify);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void deleteSession(int sessionId) throws ModelException, DataManagerException {
        Session sessionToDelete = this.getSessionWithExceptions(sessionId);

        SessionTrainerDataManager sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
        if (sessionTrainerDataManager.isSessionUsed(sessionToDelete)) {
            throw new DeletingReferencedDataManagerDataException("Cette session est référencée par au moins un formateur et est donc impossible à supprimer.");
        }

        SessionRegistrationDataManager sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
        if (sessionRegistrationDataManager.isSessionUsed(sessionToDelete)) {
            throw new DeletingReferencedDataManagerDataException("Cette session est référencée par au moins une inscription et est donc impossible à supprimer.");
        }

        this.sessions.remove(sessionToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public List<Session> getCampSessions(Camp camp) throws DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.sessions.values().stream().filter(session -> {
            try {
                return session.getCampId() == camp.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isCampUsed(Camp campToCheck) throws DataManagerException {
        if (campToCheck == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.getModels().stream().anyMatch(session -> {
            try {
                return session.getCampId() == campToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isPersonUsed(Person personToCheck) throws DataManagerException, ModelException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        if (DataManagers.get(SessionTrainerDataManager.class).isPersonUsed(personToCheck)) {
            return true;
        }

        return DataManagers.get(SessionRegistrationDataManager.class).isPersonUsed(personToCheck);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Session> getModels() {
        return this.sessions.values();
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
        return this.sessions.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Session.Data sessionData : dataObject.sessions) {
            Session session = new Session();
            session.hydrate(sessionData);
            this.pendingModels.add(session);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Session session)) {
            throw new ModelException("Le manager '%s' attend un objet de type Session, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addSession(session);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Session.Data> sessions = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(SessionDataManager sessionDataManager) throws ModelException {
            for (Session session : sessionDataManager.getModels()) {
                this.sessions.add(session.dehydrate());
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

            if (!obj.has("sessions")) {
                throw new StringParserException("Le champ 'sessions' est manquant");
            } else if (!obj.get("sessions").isJsonArray()) {
                throw new StringParserException("Le champ 'sessions' doit être un tableau");
            }

            JsonArray sessionsArray = obj.getAsJsonArray("sessions");
            for (JsonElement element : sessionsArray) {
                Session.Data sessionData = new Session.Data();
                sessionData.parseJson(element.toString());
                this.sessions.add(sessionData);
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
