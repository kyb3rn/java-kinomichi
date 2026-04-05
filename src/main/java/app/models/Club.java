package app.models;

import app.models.managers.AddressDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
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
import utils.helpers.validation.StrictlyEmptyOrNullValueValidatorException;
import utils.helpers.validation.Validators;

import java.util.regex.Pattern;

public class Club extends IdentifiedModel implements Hydratable<Club.Data> {

    // ─── Properties ─── //

    private String name;
    @ModelReference(manager = AddressDataManager.class) private Address address;
    private int pendingAddressPk = -1;
    private String googleMapsLink;

    // ─── Getters ─── //

    public String getName() {
        return this.name;
    }

    public Address getAddress() {
        return this.address;
    }

    public String getGoogleMapsLink() {
        return this.googleMapsLink;
    }

    // ─── Special getters ─── //

    public int getAddressId() throws ModelException {
        if (this.address == null) {
            throw new ModelException("L'adresse de référence du club est nulle");
        }

        return this.address.getId();
    }

    // ─── Setters ─── //

    public void setName(String name) throws ModelException {
        this.name = verifyName(name);
    }

    public void setAddress(Address address) throws ModelException {
        if (address == null) {
            throw new ModelVerificationException("L'adresse de réference d'un club ne peut pas être nulle");
        }

        try {
            this.address = DataManagers.get(AddressDataManager.class).getAddressWithExceptions(address.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant d'adresse '%d'".formatted(address.getId()), e);
        }
    }

    public void setGoogleMapsLink(String googleMapsLink) throws ModelException {
        this.googleMapsLink = verifyGoogleMapsLink(googleMapsLink);
    }

    // ─── Special setters ─── //

    public void setAddressFromPk(int addressId) throws ModelException {
        try {
            this.address = DataManagers.get(AddressDataManager.class).getAddressWithExceptions(addressId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new ModelException("Impossible de vérifier l'identifiant d'adresse '%d' (les adresses n'ont pas pu être chargées dans l'application)".formatted(addressId), e);
        }
    }

    // ─── Utility methods ─── //

    public static String verifyName(String name) throws ModelException {
        return verifyNotNullOrEmpty(name, "Le nom d'un club ne peut pas être vide ou nul");
    }

    public static String verifyGoogleMapsLink(String googleMapsLink) throws ModelException {
        try {
            googleMapsLink = Validators.validateNotNullOrStrictlyEmpty(googleMapsLink);
        } catch (StrictlyEmptyOrNullValueValidatorException e) {
            return null;
        }

        if (googleMapsLink.isBlank()) {
            throw new ModelVerificationException("Le lien Google Maps d'un club ne peut pas être vide (null accepté)");
        }

        googleMapsLink = googleMapsLink.strip();

        Pattern googleMapsLinkPattern = Pattern.compile("^https://maps\\.app\\.goo\\.gl/[A-Za-z0-9]{17}$");
        if (!googleMapsLinkPattern.matcher(googleMapsLink).matches()) {
            throw new ModelVerificationException("Le format du lien Google Maps est invalide");
        }

        return googleMapsLink;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Club clone() {
        Club clone = new Club();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.name = this.name;
        clone.address = this.address;
        clone.pendingAddressPk = this.pendingAddressPk;
        clone.googleMapsLink = this.googleMapsLink;

        return clone;
    }

    @Override
    public String toString() {
        return "#%d %s".formatted(this.getId(), this.name);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.address != null && this.name != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.setName(dataObject.getName());
        this.pendingAddressPk = dataObject.getAddressId();
        this.setGoogleMapsLink(dataObject.getGoogleMapsLink());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private String name;
        private int addressId = -1;
        private String googleMapsLink;

        // ─── Constructors ─── //

        public Data() {}

        public Data(Club club) throws ModelException {
            this.setId(club.getId());
            this.setName(club.getName());
            this.setAddressId(club.getAddress().getId());
            this.setGoogleMapsLink(club.getGoogleMapsLink());
        }

        // ─── Getters ─── //

        public String getName() {
            return this.name;
        }

        public int getAddressId() {
            return this.addressId;
        }

        public String getGoogleMapsLink() {
            return this.googleMapsLink;
        }

        // ─── Setters ─── //

        public void setName(String name) throws ModelException {
            this.name = verifyName(name);
        }

        public void setAddressId(String addressId) throws ModelException {
            this.addressId = verifyId(addressId);
        }

        public void setAddressId(int addressId) throws ModelException {
            this.setAddressId(String.valueOf(addressId));
        }

        public void setGoogleMapsLink(String googleMapsLink) throws ModelException {
            this.googleMapsLink = verifyGoogleMapsLink(googleMapsLink);
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
            if (!obj.has("name")) {
                throw new StringParserException("Le champ 'name' est manquant");
            }
            if (!obj.has("addressId")) {
                throw new StringParserException("Le champ 'addressId' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setName(obj.get("name").getAsString());
                this.setAddressId(obj.get("addressId").getAsString());

                if (obj.has("googleMapsLink") && !obj.get("googleMapsLink").isJsonNull()) {
                    this.setGoogleMapsLink(obj.get("googleMapsLink").getAsString());
                }
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
