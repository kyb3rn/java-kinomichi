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

public class SessionTrainerDataManager extends DataManager<SessionTrainerDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, SessionTrainer> sessionTrainers = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, SessionTrainer> getSessionTrainers() {
        return Collections.unmodifiableSortedMap(this.sessionTrainers);
    }

    // ─── Utility methods ─── //

    public SessionTrainer getSessionTrainerWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        SessionTrainer sessionTrainer = this.sessionTrainers.get(id);

        if (sessionTrainer == null) {
            throw new NoResultForPrimaryKeyException("Aucun des formateurs de session enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return sessionTrainer;
    }

    public void addSessionTrainer(SessionTrainer sessionTrainer) throws ModelException, DataManagerException {
        if (sessionTrainer == null) {
            throw new DataManagerException("Le formateur de session à ajouter ne peut pas être nul");
        }

        this.applyAutoIncrementIfPossible(sessionTrainer);

        if (!sessionTrainer.isValid()) {
            throw new ModelException("Le formateur de session à ajouter n'est pas valide");
        }

        if (this.sessionTrainers.containsKey(sessionTrainer.getId())) {
            throw new DataManagerException("Un formateur de session portant l'identifiant '%d' existe déjà".formatted(sessionTrainer.getId()));
        }

        // Check that the person is not already a trainer for this session
        if (this.isPersonTrainerForSession(sessionTrainer.getSessionId(), sessionTrainer.getPerson())) {
            throw new DataManagerException("La personne portant l'identifiant '%d' est déjà formateur de la session portant l'identifiant '%d'".formatted(sessionTrainer.getPersonId(), sessionTrainer.getSessionId()));
        }

        // Check that the person is not a trainer in another session that overlaps this one
        TimeSlot sessionTimeSlot = sessionTrainer.getSession().getTimeSlot();
        List<SessionTrainer> personOverlappingSessionTrainers = this.getPersonSessionTrainersDuringTimeSlot(sessionTrainer.getPerson(), sessionTimeSlot);
        if (!personOverlappingSessionTrainers.isEmpty()) {
            throw new DataManagerException("La personne portant l'identifiant '%d' est déjà formateur dans une autre session dont le créneau horaire se superpose".formatted(sessionTrainer.getPersonId()));
        }

        // Check that the person is not registered as a participant in a session that overlaps this one
        SessionRegistrationDataManager sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
        List<SessionRegistration> personOverlappingRegistrations = sessionRegistrationDataManager.getPersonSessionRegistrationsDuringTimeSlot(sessionTrainer.getPerson(), sessionTimeSlot);
        if (!personOverlappingRegistrations.isEmpty()) {
            throw new DataManagerException("La personne portant l'identifiant '%d' est déjà inscrite à une autre session dont le créneau horaire se superpose".formatted(sessionTrainer.getPersonId()));
        }

        // Check that the person is affiliated (affiliation overlaps the camp timeSlot)
        AffiliationDataManager affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        TimeSlot campTimeSlot = sessionTrainer.getSession().getCamp().getTimeSlot();
        List<Affiliation> personAffiliationsDuringCamp = affiliationDataManager.getPersonAffiliationsDuringTimeSlot(sessionTrainer.getPerson(), campTimeSlot);
        if (personAffiliationsDuringCamp.isEmpty()) {
            throw new DataManagerException("La personne portant l'identifiant '%d' n'est pas affiliée durant la période du stage lié à cette session".formatted(sessionTrainer.getPersonId()));
        }

        this.sessionTrainers.put(sessionTrainer.getId(), sessionTrainer);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public SessionTrainer addSessionTrainer(SessionTrainer.Data sessionTrainerData) throws ModelException, DataManagerException {
        if (sessionTrainerData == null) {
            throw new DataManagerException("Le formateur de session à ajouter ne peut pas être nul");
        }

        SessionTrainer sessionTrainer = new SessionTrainer();
        sessionTrainer.hydrate(sessionTrainerData);
        DataManagers.resolveModelReferences(sessionTrainer);

        this.addSessionTrainer(sessionTrainer);

        return sessionTrainer;
    }

    public void deleteSessionTrainer(int sessionTrainerId) throws ModelException, DataManagerException {
        SessionTrainer sessionTrainerToDelete = this.getSessionTrainerWithExceptions(sessionTrainerId);

        SessionRegistrationDataManager sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
        if (sessionRegistrationDataManager.isSessionUsed(sessionTrainerToDelete.getSession())) {
            throw new DataManagerException("Impossible de retirer un formateur d'une session ayant des inscrits");
        }

        this.sessionTrainers.remove(sessionTrainerToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public List<SessionTrainer> getSessionSessionTrainers(Session session) throws DataManagerException {
        if (session == null) {
            throw new DataManagerException("La session sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.sessionTrainers.values().stream().filter(sessionTrainer -> {
            try {
                return sessionTrainer.getSessionId() == session.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<SessionTrainer> getPersonSessionTrainers(Person person) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.sessionTrainers.values().stream().filter(sessionTrainer -> {
            try {
                return sessionTrainer.getPersonId() == person.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<SessionTrainer> getPersonSessionTrainersDuringTimeSlot(Person person, TimeSlot timeSlot) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        if (timeSlot == null) {
            throw new DataManagerException("La période de temps sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.sessionTrainers.values().stream().filter(sessionTrainer -> {
            try {
                return sessionTrainer.getPersonId() == person.getId() && sessionTrainer.getSession().getTimeSlot().overlaps(timeSlot);
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public boolean isPersonTrainerForSession(int sessionId, Person personToCheck) throws DataManagerException, ModelException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        int finalSessionId = IdentifiedModel.verifyId(sessionId);

        return this.getModels().stream().anyMatch(sessionTrainer -> {
            try {
                return sessionTrainer.getSessionId() == finalSessionId && sessionTrainer.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isPersonUsed(Person personToCheck) throws DataManagerException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(sessionTrainer -> {
            try {
                return sessionTrainer.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isSessionUsed(Session sessionToCheck) throws DataManagerException {
        if (sessionToCheck == null) {
            throw new DataManagerException("La session sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(sessionTrainer -> {
            try {
                return sessionTrainer.getSessionId() == sessionToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<SessionTrainer> getModels() {
        return this.sessionTrainers.values();
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
        return this.sessionTrainers.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (SessionTrainer.Data sessionTrainerData : dataObject.sessionTrainers) {
            SessionTrainer sessionTrainer = new SessionTrainer();
            sessionTrainer.hydrate(sessionTrainerData);
            this.pendingModels.add(sessionTrainer);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof SessionTrainer sessionTrainer)) {
            throw new ModelException("Le manager '%s' attend un objet de type SessionTrainer, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addSessionTrainer(sessionTrainer);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<SessionTrainer.Data> sessionTrainers = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(SessionTrainerDataManager sessionTrainerDataManager) throws ModelException {
            for (SessionTrainer sessionTrainer : sessionTrainerDataManager.getModels()) {
                this.sessionTrainers.add(sessionTrainer.dehydrate());
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

            if (!obj.has("sessionTrainers")) {
                throw new StringParserException("Le champ 'sessionTrainers' est manquant");
            } else if (!obj.get("sessionTrainers").isJsonArray()) {
                throw new StringParserException("Le champ 'sessionTrainers' doit être un tableau");
            }

            JsonArray sessionTrainersArray = obj.getAsJsonArray("sessionTrainers");
            for (JsonElement element : sessionTrainersArray) {
                SessionTrainer.Data sessionTrainerData = new SessionTrainer.Data();
                sessionTrainerData.parseJson(element.toString());
                this.sessionTrainers.add(sessionTrainerData);
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
