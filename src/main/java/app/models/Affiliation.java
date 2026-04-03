package app.models;

import app.models.managers.ClubDataManager;
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

public class Affiliation extends Model implements Hydratable<Affiliation.Data> {

    // ─── Properties ─── //

    @ModelReference(manager = PersonDataManager.class) private Person person;
    private int pendingPersonPk = -1;
    @ModelReference(manager = ClubDataManager.class) private Club club;
    private int pendingClubPk = -1;
    private String affiliationNumber;

    // ─── Getters ─── //

    public Person getPerson() {
        return this.person;
    }

    public Club getClub() {
        return this.club;
    }

    public String getAffiliationNumber() {
        return this.affiliationNumber;
    }

    // ─── Special getters ─── //

    public int getPersonId() throws ModelException {
        if (this.person == null) {
            throw new ModelException("La personne de référence est nulle");
        }

        return this.person.getId();
    }

    public int getClubId() throws ModelException {
        if (this.club == null) {
            throw new ModelException("Le club de référence est nul");
        }

        return this.club.getId();
    }

    // ─── Setters ─── //

    public void setPerson(Person person) throws ModelException {
        if (person == null) {
            throw new ModelException("La personne est requise pour un affilié (valeur null reçue)");
        }

        this.setPersonFromPk(person.getId());
    }

    public void setClub(Club club) throws ModelException {
        if (club == null) {
            throw new ModelException("Le club est requis pour un affilié (valeur null reçue)");
        }

        this.setClubFromPk(club.getId());
    }

    public void setAffiliationNumber(String affiliationNumber) throws ModelException {
        if (affiliationNumber == null || affiliationNumber.isBlank()) {
            throw new ModelException("Le numéro d'affiliation ne peut pas être vide ou nul");
        }

        this.affiliationNumber = affiliationNumber.strip();
    }

    // ─── Special setters ─── //

    public void setPersonFromPk(int personId) throws ModelException {
        Person person;
        try {
            person = DataManagers.get(PersonDataManager.class).getPerson(personId);
        } catch (DataManagerException | ModelException e) {
            throw new ModelException("Impossible de vérifier l'identifiant de personne '%d' (les personnes n'ont pas pu être chargées dans l'application)".formatted(personId), e);
        }

        if (person == null) {
            throw new ModelException("Aucune des personnes enregistrées ne porte l'identifiant '%d'".formatted(personId));
        }

        this.person = person;
    }

    public void setClubFromPk(int clubId) throws ModelException {
        Club club;
        try {
            club = DataManagers.get(ClubDataManager.class).getClub(clubId);
        } catch (DataManagerException | ModelException e) {
            throw new ModelException("Impossible de vérifier l'identifiant de club '%d' (les clubs n'ont pas pu être chargés dans l'application)".formatted(clubId), e);
        }

        if (club == null) {
            throw new ModelException("Aucun des clubs enregistrés ne porte l'identifiant '%d'".formatted(clubId));
        }

        this.club = club;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String personId = this.person != null ? "#" + this.person.getId() : "null";
        String clubId = this.club != null ? "#" + this.club.getId() : "null";
        return "%s %s %s".formatted(personId, clubId, this.affiliationNumber);
    }

    @Override
    public boolean isValid() {
        return this.person != null && this.club != null && this.affiliationNumber != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingPersonPk = dataObject.getPersonId();
        this.pendingClubPk = dataObject.getClubId();
        this.setAffiliationNumber(dataObject.getAffiliationNumber());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Affiliation.Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int personId = -1;
        private int clubId = -1;
        private String affiliationNumber;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(Affiliation affiliation) throws ModelException {
            this.setPersonId(affiliation.getPersonId());
            this.setClubId(affiliation.getClub().getId());
            this.setAffiliationNumber(affiliation.getAffiliationNumber());
        }

        // ─── Getters ─── //

        public int getPersonId() {
            return this.personId;
        }

        public int getClubId() {
            return this.clubId;
        }

        public String getAffiliationNumber() {
            return this.affiliationNumber;
        }

        // ─── Setters ─── //

        public void setPersonId(int personId) throws ModelException {
            if (personId <= 0 && personId != -1) {
                throw new ModelException("L'identifiant référant à une personne doit être un entier strictement positif (ou -1)");
            }

            this.personId = personId;
        }

        public void setPersonId(String personId) throws ModelException {
            int personIdAsInt;
            try {
                personIdAsInt = Integer.parseInt(personId);
            } catch (NumberFormatException e) {
                throw new ModelException("L'identifiant référant à une personne doit être un entier strictement positif (ou -1)", e);
            }

            this.setPersonId(personIdAsInt);
        }

        public void setClubId(int clubId) throws ModelException {
            if (clubId <= 0 && clubId != -1) {
                throw new ModelException("L'identifiant référant à un club doit être un entier strictement positif (ou -1)");
            }

            this.clubId = clubId;
        }

        public void setClubId(String clubId) throws ModelException {
            int clubIdAsInt;
            try {
                clubIdAsInt = Integer.parseInt(clubId);
            } catch (NumberFormatException e) {
                throw new ModelException("L'identifiant référant à un club doit être un entier strictement positif (ou -1)", e);
            }

            this.setClubId(clubIdAsInt);
        }

        public void setAffiliationNumber(String affiliationNumber) throws ModelException {
            if (affiliationNumber == null || affiliationNumber.isBlank()) {
                throw new ModelException("Le numéro d'affiliation ne peut pas être vide ou nul");
            }

            this.affiliationNumber = affiliationNumber.strip();
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

            if (!obj.has("personId")) {
                throw new StringParserException("Le champ 'personId' est manquant");
            }
            if (!obj.has("clubId")) {
                throw new StringParserException("Le champ 'clubId' est manquant");
            }
            if (!obj.has("affiliationNumber")) {
                throw new StringParserException("Le champ 'affiliationNumber' est manquant");
            }

            try {
                this.setPersonId(obj.get("personId").getAsString());
                this.setClubId(obj.get("clubId").getAsString());
                this.setAffiliationNumber(obj.get("affiliationNumber").getAsString());
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
