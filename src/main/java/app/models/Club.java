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
            throw new ModelException("L'adresse de référence est nulle");
        }

        return this.address.getId();
    }

    // ─── Setters ─── //

    public void setName(String name) throws ModelException {
        if (name == null || name.isBlank()) {
            throw new ModelException("Le nom d'un club ne peut pas être vide ou nul");
        }

        this.name = name.strip();
    }

    public void setAddress(Address address) throws ModelException {
        if (address == null) {
            throw new ModelException("L'adresse est requise pour un club (valeur null reçue)");
        }

        this.setAddressFromPk(address.getId());
    }

    public void setGoogleMapsLink(String googleMapsLink) throws ModelException {
        if (googleMapsLink == null || googleMapsLink.isEmpty()) {
            this.googleMapsLink = null;
        } else {
            if (googleMapsLink.isBlank()) {
                throw new ModelException("Le lien Google Maps d'un club ne peut pas être vide (null accepté)");
            }

            googleMapsLink = googleMapsLink.strip();

            Pattern googleMapsLinkPattern = Pattern.compile("^https://maps\\.app\\.goo\\.gl/[A-Za-z0-9]{17}$");
            if (!googleMapsLinkPattern.matcher(googleMapsLink).matches()) {
                throw new ModelException("Le format du lien Google Maps est invalide");
            }

            this.googleMapsLink = googleMapsLink;
        }
    }

    // ─── Special setters ─── //

    public void setAddressFromPk(int addressId) throws ModelException {
        Address address;
        try {
            address = DataManagers.get(AddressDataManager.class).getAddress(addressId);
        } catch (DataManagerException | ModelException e) {
            throw new ModelException("Impossible de vérifier l'identifiant d'adresse '%d' (les adresses n'ont pas pu être chargées dans l'application)".formatted(addressId), e);
        }

        if (address == null) {
            throw new ModelException("Aucune des adresses enregistrées ne porte l'identifiant '%d'".formatted(addressId));
        }

        this.address = address;
    }

    // ─── Overrides & inheritance ─── //

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

    @Override
    public String toString() {
        return "#%d %s".formatted(this.getId(), this.name);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.address != null && this.name != null;
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
            if (name == null || name.isBlank()) {
                throw new ModelException("Le nom d'un club ne peut pas être vide ou nul");
            }

            this.name = name.strip();
        }

        public void setAddressId(int addressId) throws ModelException {
            if (addressId <= 0 && addressId != -1) {
                throw new ModelException("L'identifiant référant à une adresse doit être un entier strictement positif (ou -1)");
            }

            this.addressId = addressId;
        }

        public void setAddressId(String addressId) throws ModelException {
            int addressIdAsInt;
            try {
                addressIdAsInt = Integer.parseInt(addressId);
            } catch (NumberFormatException e) {
                throw new ModelException("L'identifiant référant à une adresse doit être un entier strictement positif (ou -1)", e);
            }

            this.setAddressId(addressIdAsInt);
        }

        public void setGoogleMapsLink(String googleMapsLink) throws ModelException {
            if (googleMapsLink == null || googleMapsLink.isEmpty()) {
                this.googleMapsLink = null;
            } else {
                if (googleMapsLink.isBlank()) {
                    throw new ModelException("Le lien Google Maps d'un club ne peut pas être vide (null accepté)");
                }

                googleMapsLink = googleMapsLink.strip();

                Pattern googleMapsLinkPattern = Pattern.compile("^https://maps\\.app\\.goo\\.gl/[A-Za-z0-9]{17}$");
                if (!googleMapsLinkPattern.matcher(googleMapsLink).matches()) {
                    throw new ModelException("Le format du lien Google Maps est invalide");
                }

                this.googleMapsLink = googleMapsLink.strip();
            }
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
