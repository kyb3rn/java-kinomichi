package app.models;

import app.models.managers.AffiliationDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.tarification.EChargeableCategory;
import app.utils.tarification.ChargeableElement;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;
import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.PatternMatchingValidatorException;
import utils.helpers.validation.Validators;

public class Person extends IdentifiedModel implements Hydratable<Person>, CustomSerializable, JsonConvertible, ChargeableElement {

    // ─── Properties ─── //

    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    // ─── Getters ─── //

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getEmail() {
        return this.email;
    }

    // ─── Setters ─── //

    public void setFirstName(String firstName) throws ModelException {
        this.firstName = verifyFirstName(firstName);
    }

    public void setLastName(String lastName) throws ModelException {
        this.lastName = verifyLastName(lastName);
    }

    public void setPhone(String phone) throws ModelException {
        this.phone = verifyPhone(phone);
    }

    public void setEmail(String email) throws ModelException {
        this.email = verifyEmail(email);
    }

    // ─── Utility methods ─── //

    public static String verifyFirstName(String firstName) throws ModelException {
        return verifyNotNullOrEmpty(firstName, "Le prénom d'une personne ne peut pas être vide ou nul");
    }

    public static String verifyLastName(String lastName) throws ModelException {
        return verifyNotNullOrEmpty(lastName, "Le nom d'une personne ne peut pas être vide ou nul");
    }

    public static String verifyPhone(String phone) throws ModelException {
        return verifyNotNullOrEmpty(phone, "Le téléphone d'une personne ne peut pas être vide ou nul");
    }

    public static String verifyEmail(String email) throws ModelException {
        try {
            return Validators.validateEmail(email);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("L'email d'une personne ne peut pas être vide ou nul", e);
        } catch (PatternMatchingValidatorException e) {
            throw new ModelVerificationException("Le format de l'email est invalide", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        return "#%d %s %s".formatted(this.getId(), this.firstName, this.lastName);
    }

    @Override
    public Person clone() {
        Person clone = new Person();

        try {
            clone.setId(this.getId());
        } catch (ModelException e) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.firstName = this.firstName;
        clone.lastName = this.lastName;
        clone.phone = this.phone;
        clone.email = this.email;

        return clone;
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.firstName != null && this.lastName != null && this.phone != null && this.email != null;
    }

    @Override
    public void hydrate(Person dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.setFirstName(dataObject.getFirstName());
        this.setLastName(dataObject.getLastName());
        this.setPhone(dataObject.getPhone());
        this.setEmail(dataObject.getEmail());
    }

    @Override
    public Person dehydrate() throws Exception {
        return this.clone();
    }

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
        if (!obj.has("firstName")) {
            throw new StringParserException("Le champ 'firstName' est manquant");
        }
        if (!obj.has("lastName")) {
            throw new StringParserException("Le champ 'lastName' est manquant");
        }
        if (!obj.has("phone")) {
            throw new StringParserException("Le champ 'phone' est manquant");
        }
        if (!obj.has("email")) {
            throw new StringParserException("Le champ 'email' est manquant");
        }

        try {
            this.setId(obj.get("id").getAsInt());
            this.setFirstName(obj.get("firstName").getAsString());
            this.setLastName(obj.get("lastName").getAsString());
            this.setPhone(obj.get("phone").getAsString());
            this.setEmail(obj.get("email").getAsString());
        } catch (ModelException e) {
            throw new ParserException(e);
        }
    }

    @Override
    public String toJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    @Override
    public ChargeableCategory getChargeableCategory() {
        Affiliation affiliation = null;
        try {
            affiliation = DataManagers.get(AffiliationDataManager.class).getAffiliation(this.getId());
        } catch (DataManagerException | ModelException _) {
        }

        if (affiliation != null) {
            return ChargeableCategory.AFFILIATED;
        }

        return ChargeableCategory.NORMAL;
    }

    public enum ChargeableCategory implements EChargeableCategory {

        NORMAL("Personne normale"),
        AFFILIATED("Personne affiliée"),
        TRAINER("Personne ayant donné cours"),
        INVITED("Personne invitée");

        // ─── Properties ─── //

        private final String label;

        // ─── Constructors ─── //

        ChargeableCategory(String label) {
            this.label = label;
        }

        // ─── Overrides & inheritance ─── //

        @Override
        public String getLabel() {
            return this.label;
        }

    }

}
