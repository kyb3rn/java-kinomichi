package app.models;

import app.models.managers.ClubDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;
import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.ParsingValidatorException;
import utils.helpers.validation.Validators;
import app.utils.elements.time.TimeSlot;

import java.time.Instant;
import java.util.regex.Pattern;

public class Affiliation extends IdentifiedModel implements Hydratable<Affiliation.Data> {

    private static final Pattern AFFILIATION_NUMBER_PATTERN = Pattern.compile("[0-9]{4}-[A-Z]{5}");

    // ─── Properties ─── //

    @ModelReference(manager = PersonDataManager.class) private Person person;
    private int pendingPersonPk = -1;
    @ModelReference(manager = ClubDataManager.class) private Club club;
    private int pendingClubPk = -1;
    private String affiliationNumber;
    private TimeSlot validityPeriod;

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

    public TimeSlot getValidityPeriod() {
        return this.validityPeriod;
    }

    // ─── Special getters ─── //

    public int getPersonId() throws ModelException {
        if (this.person == null) {
            throw new ModelException("La personne de référence de l'affiliation est nulle");
        }

        return this.person.getId();
    }

    public int getClubId() throws ModelException {
        if (this.club == null) {
            throw new ModelException("Le club de référence de l'affiliation est nul");
        }

        return this.club.getId();
    }

    // ─── Setters ─── //

    public void setPerson(Person person) throws ModelException {
        if (person == null) {
            throw new ModelVerificationException("La personne de réference d'une affiliation ne peut pas être nulle");
        }

        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(person.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de personne '%d'".formatted(person.getId()), e);
        }
    }

    public void setClub(Club club) throws ModelException {
        if (club == null) {
            throw new ModelVerificationException("Le club de réference d'une affiliation ne peut pas être nul");
        }

        try {
            this.club = DataManagers.get(ClubDataManager.class).getClubWithExceptions(club.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de club '%d'".formatted(club.getId()), e);
        }
    }

    public void setAffiliationNumber(String affiliationNumber) throws ModelException {
        this.affiliationNumber = verifyAffiliationNumber(affiliationNumber);
    }

    public void setValidityPeriod(TimeSlot validityPeriod) throws ModelException {
        if (validityPeriod == null) {
            throw new ModelVerificationException("La période de validité d'une affiliation ne peut pas être nulle");
        }

        this.validityPeriod = validityPeriod;
    }

    // ─── Special setters ─── //

    public void setPersonFromPk(int personId) throws DataManagerException {
        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(personId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de personne '%d' (les personnes n'ont pas pu être chargées dans l'application)".formatted(personId), e);
        }
    }

    public void setClubFromPk(int clubId) throws DataManagerException {
        try {
            this.club = DataManagers.get(ClubDataManager.class).getClubWithExceptions(clubId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de club '%d' (les clubs n'ont pas pu être chargés dans l'application)".formatted(clubId), e);
        }
    }

    public void setPersonFromPk(String personIdAsString) throws DataManagerException, ModelException {
        this.setPersonFromPk(Person.verifyId(personIdAsString));
    }

    public void setClubFromPk(String clubIdAsString) throws DataManagerException, ModelException {
        this.setClubFromPk(Club.verifyId(clubIdAsString));
    }

    // ─── Utility methods ─── //

    public static String verifyAffiliationNumber(String affiliationNumber) throws ModelException {
        affiliationNumber = verifyNotNullOrEmpty(affiliationNumber, "Le numéro d'affiliation d'une affiliation ne peut pas être vide ou nul");

        if (!AFFILIATION_NUMBER_PATTERN.matcher(affiliationNumber).matches()) {
            throw new ModelVerificationException("Le numéro d'affiliation '%s' ne respecte pas le format attendu (ex: [0-9]{4}-[A-Z]{5})".formatted(affiliationNumber));
        }

        return affiliationNumber;
    }

    public static Instant verifyValidityPeriodStart(String validityPeriodStart) throws ModelException {
        try {
            return Validators.validateInstant(validityPeriodStart);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de début de validité d'une affiliation ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de début de validité a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Instant verifyValidityPeriodEnd(String validityPeriodEnd) throws ModelException {
        try {
            return Validators.validateInstant(validityPeriodEnd);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de fin de validité d'une affiliation ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de fin de validité a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String personId = this.person != null ? "#" + this.person.getId() : "null";
        String clubId = this.club != null ? "#" + this.club.getId() : "null";
        return "#%d %s %s %s (%s)".formatted(this.getId(), personId, clubId, this.affiliationNumber, this.validityPeriod);
    }

    @Override
    public Affiliation clone() {
        Affiliation clone = new Affiliation();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.person = this.person;
        clone.pendingPersonPk = this.pendingPersonPk;
        clone.club = this.club;
        clone.pendingClubPk = this.pendingClubPk;
        clone.affiliationNumber = this.affiliationNumber;
        clone.validityPeriod = this.validityPeriod;

        return clone;
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.person != null && this.club != null && this.affiliationNumber != null && this.validityPeriod != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingPersonPk = dataObject.getPersonId();
        this.pendingClubPk = dataObject.getClubId();
        this.setAffiliationNumber(dataObject.getAffiliationNumber());
        this.setValidityPeriod(dataObject.getValidityPeriod());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Affiliation.Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int personId = -1;
        private int clubId = -1;
        private String affiliationNumber;
        private Instant validityPeriodStart;
        private Instant validityPeriodEnd;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(Affiliation affiliation) throws ModelException {
            this.setId(affiliation.getId());
            this.setPersonId(affiliation.getPersonId());
            this.setClubId(affiliation.getClub().getId());
            this.setAffiliationNumber(affiliation.getAffiliationNumber());
            this.setValidityPeriodStart(affiliation.getValidityPeriod().getFormattedStart());
            this.setValidityPeriodEnd(affiliation.getValidityPeriod().getFormattedEnd());
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

        public Instant getValidityPeriodStart() {
            return this.validityPeriodStart;
        }

        public Instant getValidityPeriodEnd() {
            return this.validityPeriodEnd;
        }

        // ─── Special getters ─── //

        public TimeSlot getValidityPeriod() {
            return new TimeSlot(this.validityPeriodStart, this.validityPeriodEnd);
        }

        // ─── Setters ─── //

        public void setPersonId(String personId) throws ModelException {
            this.personId = IdentifiedModel.verifyId(personId);
        }

        public void setPersonId(int personId) throws ModelException {
            this.setPersonId(String.valueOf(personId));
        }

        public void setClubId(String clubId) throws ModelException {
            this.clubId = IdentifiedModel.verifyId(clubId);
        }

        public void setClubId(int clubId) throws ModelException {
            this.setClubId(String.valueOf(clubId));
        }

        public void setAffiliationNumber(String affiliationNumber) throws ModelException {
            this.affiliationNumber = verifyAffiliationNumber(affiliationNumber);
        }

        public void setValidityPeriodStart(String validityPeriodStart) throws ModelException {
            Instant validityPeriodStartInstant = verifyValidityPeriodStart(validityPeriodStart);

            if (this.validityPeriodEnd != null && (validityPeriodStartInstant.isAfter(this.validityPeriodEnd) || validityPeriodStartInstant.equals(this.validityPeriodEnd))) {
                throw new ModelVerificationException("La date de début de validité d'une affiliation doit être strictement antérieure à sa date de fin");
            }

            this.validityPeriodStart = validityPeriodStartInstant;
        }

        public void setValidityPeriodEnd(String validityPeriodEnd) throws ModelException {
            Instant validityPeriodEndInstant = verifyValidityPeriodEnd(validityPeriodEnd);

            if (this.validityPeriodStart != null && (validityPeriodEndInstant.isBefore(this.validityPeriodStart) || validityPeriodEndInstant.equals(this.validityPeriodStart))) {
                throw new ModelVerificationException("La date de fin de validité d'une affiliation doit être strictement postérieure à sa date de début");
            }

            this.validityPeriodEnd = validityPeriodEndInstant;
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
            if (!obj.has("personId")) {
                throw new StringParserException("Le champ 'personId' est manquant");
            }
            if (!obj.has("clubId")) {
                throw new StringParserException("Le champ 'clubId' est manquant");
            }
            if (!obj.has("affiliationNumber")) {
                throw new StringParserException("Le champ 'affiliationNumber' est manquant");
            }
            if (!obj.has("validityPeriodStart")) {
                throw new StringParserException("Le champ 'validityPeriodStart' est manquant");
            }
            if (!obj.has("validityPeriodEnd")) {
                throw new StringParserException("Le champ 'validityPeriodEnd' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setPersonId(obj.get("personId").getAsString());
                this.setClubId(obj.get("clubId").getAsString());
                this.setAffiliationNumber(obj.get("affiliationNumber").getAsString());
                this.setValidityPeriodStart(obj.get("validityPeriodStart").getAsString());
                this.setValidityPeriodEnd(obj.get("validityPeriodEnd").getAsString());
            } catch (ModelException e) {
                throw new ParserException(e);
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
