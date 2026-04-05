package app.models;

import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

public class Invitation extends IdentifiedModel implements Hydratable<Invitation.Data> {

    // ─── Properties ─── //

    @ModelReference(manager = CampDataManager.class) private Camp camp;
    private int pendingCampPk = -1;
    @ModelReference(manager = PersonDataManager.class) private Person person;
    private int pendingPersonPk = -1;

    // ─── Getters ─── //

    public Camp getCamp() {
        return this.camp;
    }

    public Person getPerson() {
        return this.person;
    }

    // ─── Special getters ─── //

    public int getCampId() throws ModelException {
        if (this.camp == null) {
            throw new ModelException("Le stage de référence de l'invitation est nul");
        }

        return this.camp.getId();
    }

    public int getPersonId() throws ModelException {
        if (this.person == null) {
            throw new ModelException("La personne de référence de l'invitation est nulle");
        }

        return this.person.getId();
    }

    // ─── Setters ─── //

    public void setCamp(Camp camp) throws ModelException {
        if (camp == null) {
            throw new ModelVerificationException("Le stage de référence d'une invitation ne peut pas être nul");
        }

        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(camp.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de stage '%d'".formatted(camp.getId()), e);
        }
    }

    public void setPerson(Person person) throws ModelException {
        if (person == null) {
            throw new ModelVerificationException("La personne de référence d'une invitation ne peut pas être nulle");
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

    public void setCampFromPk(int campId) throws DataManagerException {
        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(campId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de stage '%d' (les stages n'ont pas pu être chargés dans l'application)".formatted(campId), e);
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

    public void setCampFromPk(String campIdAsString) throws DataManagerException, ModelException {
        this.setCampFromPk(Camp.verifyId(campIdAsString));
    }

    public void setPersonFromPk(String personIdAsString) throws DataManagerException, ModelException {
        this.setPersonFromPk(Person.verifyId(personIdAsString));
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String campId = this.camp != null ? "#" + this.camp.getId() : "null";
        String personId = this.person != null ? "#" + this.person.getId() : "null";
        return "#%d %s %s".formatted(this.getId(), campId, personId);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.camp != null && this.person != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingCampPk = dataObject.getCampId();
        this.pendingPersonPk = dataObject.getPersonId();
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Invitation.Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int campId = -1;
        private int personId = -1;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(Invitation invitation) throws ModelException {
            this.setId(invitation.getId());
            this.setCampId(invitation.getCampId());
            this.setPersonId(invitation.getPersonId());
        }

        // ─── Getters ─── //

        public int getCampId() {
            return this.campId;
        }

        public int getPersonId() {
            return this.personId;
        }

        // ─── Setters ─── //

        public void setCampId(String campId) throws ModelException {
            this.campId = IdentifiedModel.verifyId(campId);
        }

        public void setCampId(int campId) throws ModelException {
            this.setCampId(String.valueOf(campId));
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
            if (!obj.has("campId")) {
                throw new StringParserException("Le champ 'campId' est manquant");
            }
            if (!obj.has("personId")) {
                throw new StringParserException("Le champ 'personId' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setCampId(obj.get("campId").getAsString());
                this.setPersonId(obj.get("personId").getAsString());
            } catch (ModelException e) {
                throw new ParserException(e);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
