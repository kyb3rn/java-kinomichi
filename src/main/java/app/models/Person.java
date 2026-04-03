package app.models;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

public class Person extends IdentifiedModel implements CustomSerializable, JsonConvertible {

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
        if (firstName == null || firstName.isBlank()) {
            throw new ModelException("Le prénom d'une personne ne peut pas être vide ou nul");
        }

        this.firstName = firstName.strip();
    }

    public void setLastName(String lastName) throws ModelException {
        if (lastName == null || lastName.isBlank()) {
            throw new ModelException("Le nom d'une personne ne peut pas être vide ou nul");
        }

        this.lastName = lastName.strip();
    }

    public void setPhone(String phone) throws ModelException {
        if (phone == null || phone.isBlank()) {
            throw new ModelException("Le téléphone d'une personne ne peut pas être vide ou nul");
        }

        this.phone = phone.strip();
    }

    public void setEmail(String email) throws ModelException {
        if (email == null || email.isBlank()) {
            throw new ModelException("L'email d'une personne ne peut pas être vide ou nul");
        }

        this.email = email.strip();
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        return "#%d %s %s".formatted(this.getId(), this.firstName, this.lastName);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.firstName != null && this.lastName != null && this.phone != null && this.email != null;
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

}
