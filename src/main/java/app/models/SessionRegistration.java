package app.models;

import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.models.managers.SessionDataManager;
import app.utils.tarification.DurationBasedChargingElement;
import com.google.gson.*;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.time.Duration;

public class SessionRegistration extends IdentifiedModel implements Hydratable<SessionRegistration.Data>, DurationBasedChargingElement {

    // ─── Properties ─── //

    @ModelReference(manager = SessionDataManager.class) private Session session;
    private int pendingSessionPk = -1;
    @ModelReference(manager = PersonDataManager.class) private Person person;
    private int pendingPersonPk = -1;

    // ─── Getters ─── //

    public Session getSession() {
        return this.session;
    }

    public Person getPerson() {
        return this.person;
    }

    // ─── Special getters ─── //

    public int getSessionId() throws ModelException {
        if (this.session == null) {
            throw new ModelException("La session de référence de l'inscription est nulle");
        }

        return this.session.getId();
    }

    public int getPersonId() throws ModelException {
        if (this.person == null) {
            throw new ModelException("La personne de référence de l'inscription est nulle");
        }

        return this.person.getId();
    }

    // ─── Setters ─── //

    public void setSession(Session session) throws ModelException {
        if (session == null) {
            throw new ModelVerificationException("La session de référence d'une inscription ne peut pas être nulle");
        }

        try {
            this.session = DataManagers.get(SessionDataManager.class).getSessionWithExceptions(session.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de session '%d'".formatted(session.getId()), e);
        }
    }

    public void setPerson(Person person) throws ModelException {
        if (person == null) {
            throw new ModelVerificationException("La personne de référence d'une inscription ne peut pas être nulle");
        }

        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(person.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de personne '%d'".formatted(person.getId()), e);
        }
    }

    // ─── Special setters ─── //

    public void setSessionFromPk(int sessionId) throws DataManagerException {
        try {
            this.session = DataManagers.get(SessionDataManager.class).getSessionWithExceptions(sessionId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de session '%d' (les sessions n'ont pas pu être chargées dans l'application)".formatted(sessionId), e);
        }
    }

    public void setPersonFromPk(int personId) throws DataManagerException {
        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(personId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de personne '%d' (les personnes n'ont pas pu être chargées dans l'application)".formatted(personId), e);
        }
    }

    public void setSessionFromPk(String sessionIdAsString) throws DataManagerException, ModelException {
        this.setSessionFromPk(Session.verifyId(sessionIdAsString));
    }

    public void setPersonFromPk(String personIdAsString) throws DataManagerException, ModelException {
        this.setPersonFromPk(Person.verifyId(personIdAsString));
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String sessionId = this.session != null ? "#" + this.session.getId() : "null";
        String personId = this.person != null ? "#" + this.person.getId() : "null";
        return "#%d %s %s".formatted(this.getId(), sessionId, personId);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.session != null && this.person != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingSessionPk = dataObject.getSessionId();
        this.pendingPersonPk = dataObject.getPersonId();
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new SessionRegistration.Data(this);
    }

    @Override
    public Duration getDuration() {
        return this.session != null && this.session.getTimeSlot() != null ? this.session.getTimeSlot().getDuration() : Duration.ZERO;
    }

    @Override
    public double getBasePrice() {
        return this.getSession() != null && this.session.getCamp() != null && this.session.getCamp().getSessionsPricePerHour() != null ? this.session.getCamp().getSessionsPricePerHour().getAmount() : 0;
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int sessionId = -1;
        private int personId = -1;

        // ─── Constructors ─── //

        public Data() {}

        public Data(SessionRegistration sessionRegistration) throws ModelException {
            this.setId(sessionRegistration.getId());
            this.setSessionId(sessionRegistration.getSessionId());
            this.setPersonId(sessionRegistration.getPersonId());
        }

        // ─── Getters ─── //

        public int getSessionId() {
            return this.sessionId;
        }

        public int getPersonId() {
            return this.personId;
        }

        // ─── Setters ─── //

        public void setSessionId(String sessionId) throws ModelException {
            this.sessionId = IdentifiedModel.verifyId(sessionId);
        }

        public void setSessionId(int sessionId) throws ModelException {
            this.setSessionId(String.valueOf(sessionId));
        }

        public void setPersonId(String personId) throws ModelException {
            this.personId = IdentifiedModel.verifyId(personId);
        }

        public void setPersonId(int personId) throws ModelException {
            this.setPersonId(String.valueOf(personId));
        }

        // ─── Overrides & inheritance ─── //

        @Override
        public void parseJson(String json) throws ParserException {
            JsonObject obj;
            try {
                obj = JsonParser.parseString(json).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                throw new StringParserException("Le JSON reçu n'est pas un objet valide (%s)".formatted(e.getMessage()), e);
            }

            if (!obj.has("id")) {
                throw new StringParserException("Le champ 'id' est manquant");
            }
            if (!obj.has("sessionId")) {
                throw new StringParserException("Le champ 'sessionId' est manquant");
            }
            if (!obj.has("personId")) {
                throw new StringParserException("Le champ 'personId' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setSessionId(obj.get("sessionId").getAsString());
                this.setPersonId(obj.get("personId").getAsString());
            } catch (ModelException e) {
                throw new ParserException(e);
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
