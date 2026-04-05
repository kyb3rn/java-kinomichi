package app.models.managers;

import app.models.*;
import app.utils.elements.time.TimeSlot;
import com.google.gson.*;
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

public class SessionRegistrationDataManager extends DataManager<SessionRegistrationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, SessionRegistration> sessionRegistrations = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, SessionRegistration> getSessionRegistrations() {
        return Collections.unmodifiableSortedMap(this.sessionRegistrations);
    }

    // ─── Utility methods ─── //

    public SessionRegistration getSessionRegistrationWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        SessionRegistration sessionRegistration = this.sessionRegistrations.get(id);

        if (sessionRegistration == null) {
            throw new NoResultForPrimaryKeyException("Aucune des inscriptions aux sessions enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return sessionRegistration;
    }

    public void addSessionRegistration(SessionRegistration sessionRegistration) throws ModelException, DataManagerException {
        if (sessionRegistration == null) {
            throw new DataManagerException("L'inscription à la session à ajouter ne peut pas être nulle");
        }

        this.applyAutoIncrementIfPossible(sessionRegistration);

        if (!sessionRegistration.isValid()) {
            throw new ModelException("L'inscription à la session à ajouter n'est pas valide");
        }

        if (this.sessionRegistrations.containsKey(sessionRegistration.getId())) {
            throw new DataManagerException("Une inscription à la session portant l'identifiant '%d' existe déjà".formatted(sessionRegistration.getId()));
        }

        // Check that the person is not already registered for this session
        if (this.isPersonRegisteredForSession(sessionRegistration.getSessionId(), sessionRegistration.getPerson())) {
            throw new DataManagerException("La personne portant l'identifiant '%d' est déjà inscrite à la session portant l'identifiant '%d'".formatted(sessionRegistration.getPersonId(), sessionRegistration.getSessionId()));
        }

        // Check that the session has at least one trainer
        SessionTrainerDataManager sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
        if (!sessionTrainerDataManager.isSessionUsed(sessionRegistration.getSession())) {
            throw new DataManagerException("Impossible de s'inscrire à la session portant l'identifiant '%d' : aucun formateur n'est assigné à cette session".formatted(sessionRegistration.getSessionId()));
        }

        // Check that the person is not registered in another session that overlaps this one
        TimeSlot sessionTimeSlot = sessionRegistration.getSession().getTimeSlot();
        List<SessionRegistration> personOverlappingRegistrations = this.getPersonSessionRegistrationsDuringTimeSlot(sessionRegistration.getPerson(), sessionTimeSlot);
        if (!personOverlappingRegistrations.isEmpty()) {
            throw new DataManagerException("La personne portant l'identifiant '%d' est déjà inscrite à une autre session dont le créneau horaire se superpose".formatted(sessionRegistration.getPersonId()));
        }

        // Check that the person is not a trainer in a session that overlaps this one
        List<SessionTrainer> personOverlappingTrainers = sessionTrainerDataManager.getPersonSessionTrainersDuringTimeSlot(sessionRegistration.getPerson(), sessionTimeSlot);
        if (!personOverlappingTrainers.isEmpty()) {
            throw new DataManagerException("La personne portant l'identifiant '%d' est déjà formateur dans une session dont le créneau horaire se superpose".formatted(sessionRegistration.getPersonId()));
        }

        this.sessionRegistrations.put(sessionRegistration.getId(), sessionRegistration);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public SessionRegistration addSessionRegistration(SessionRegistration.Data sessionRegistrationData) throws ModelException, DataManagerException {
        if (sessionRegistrationData == null) {
            throw new DataManagerException("L'inscription à la session à ajouter ne peut pas être nulle");
        }

        SessionRegistration sessionRegistration = new SessionRegistration();
        sessionRegistration.hydrate(sessionRegistrationData);
        DataManagers.resolveModelReferences(sessionRegistration);

        this.addSessionRegistration(sessionRegistration);

        return sessionRegistration;
    }

    public void deleteSessionRegistration(int sessionRegistrationId) throws ModelException, DataManagerException {
        SessionRegistration sessionRegistrationToDelete = this.getSessionRegistrationWithExceptions(sessionRegistrationId);

        this.sessionRegistrations.remove(sessionRegistrationToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public List<SessionRegistration> getSessionSessionRegistrations(Session session) throws DataManagerException {
        if (session == null) {
            throw new DataManagerException("La session sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.sessionRegistrations.values().stream().filter(sessionRegistration -> {
            try {
                return sessionRegistration.getSessionId() == session.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<SessionRegistration> getPersonSessionRegistrations(Person person) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.sessionRegistrations.values().stream().filter(sessionRegistration -> {
            try {
                return sessionRegistration.getPersonId() == person.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<SessionRegistration> getPersonSessionRegistrationsDuringTimeSlot(Person person, TimeSlot timeSlot) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        if (timeSlot == null) {
            throw new DataManagerException("La période de temps sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.sessionRegistrations.values().stream().filter(sessionRegistration -> {
            try {
                return sessionRegistration.getPersonId() == person.getId() && sessionRegistration.getSession().getTimeSlot().overlaps(timeSlot);
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isPersonRegisteredForSession(int sessionId, Person personToCheck) throws DataManagerException, ModelException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        int finalSessionId = IdentifiedModel.verifyId(sessionId);

        return this.getModels().stream().anyMatch(sessionRegistration -> {
            try {
                return sessionRegistration.getSessionId() == finalSessionId && sessionRegistration.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isPersonUsed(Person personToCheck) throws DataManagerException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(sessionRegistration -> {
            try {
                return sessionRegistration.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isSessionUsed(Session sessionToCheck) throws DataManagerException {
        if (sessionToCheck == null) {
            throw new DataManagerException("La session sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(sessionRegistration -> {
            try {
                return sessionRegistration.getSessionId() == sessionToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<SessionRegistration> getModels() {
        return this.sessionRegistrations.values();
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
        return this.sessionRegistrations.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (SessionRegistration.Data sessionRegistrationData : dataObject.sessionRegistrations) {
            SessionRegistration sessionRegistration = new SessionRegistration();
            sessionRegistration.hydrate(sessionRegistrationData);
            this.pendingModels.add(sessionRegistration);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof SessionRegistration sessionRegistration)) {
            throw new ModelException("Le manager '%s' attend un objet de type SessionRegistration, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addSessionRegistration(sessionRegistration);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<SessionRegistration.Data> sessionRegistrations = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(SessionRegistrationDataManager sessionRegistrationDataManager) throws ModelException {
            for (SessionRegistration sessionRegistration : sessionRegistrationDataManager.getModels()) {
                this.sessionRegistrations.add(sessionRegistration.dehydrate());
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

            if (!obj.has("sessionRegistrations")) {
                throw new StringParserException("Le champ 'sessionRegistrations' est manquant");
            } else if (!obj.get("sessionRegistrations").isJsonArray()) {
                throw new StringParserException("Le champ 'sessionRegistrations' doit être un tableau");
            }

            JsonArray sessionRegistrationsArray = obj.getAsJsonArray("sessionRegistrations");
            for (JsonElement element : sessionRegistrationsArray) {
                SessionRegistration.Data sessionRegistrationData = new SessionRegistration.Data();
                sessionRegistrationData.parseJson(element.toString());
                this.sessionRegistrations.add(sessionRegistrationData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(this);
        }

    }

}
