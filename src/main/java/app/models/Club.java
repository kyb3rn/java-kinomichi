package app.models;

import app.data_management.managers.AddressDataManager;
import app.data_management.managers.DataManagers;
import app.data_management.managers.LoadDataManagerDataException;
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

public class Club extends Model implements Hydratable<Club.Data> {

    /**
     * Properties
     **/

    private int id = -1;
    private String name;
    private Address address;
    private String googleMapsPositionLink;

    /**
     * Getters
     **/

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Address getAddress() {
        return this.address;
    }

    public String getGoogleMapsPositionLink() {
        return this.googleMapsPositionLink;
    }

    /**
     * Setters
     **/

    public void setId(int id) throws ModelException {
        if (id <= 0 && id != -1) {
            throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)");
        }

        this.id = id;
    }

    public void setName(String name) throws ModelException {
        if (name == null || name.isBlank()) {
            throw new ModelException("Le nom d'un club ne peut pas être vide");
        }

        this.name = name.strip();
    }

    public void setAddress(Address address) throws ModelException {
        if (address == null) {
            throw new ModelException("L'adresse est requise pour un club (valeur null reçue)");
        }

        this.address = address;
    }

    public void setGoogleMapsPositionLink(String googleMapsPositionLink) throws ModelException {
        if (googleMapsPositionLink == null || googleMapsPositionLink.isEmpty()) {
            this.googleMapsPositionLink = null;
        } else {
            if (googleMapsPositionLink.isBlank()) {
                throw new ModelException("Le lien Google Maps d'un club ne peut pas être vide (null accepté)");
            }

            googleMapsPositionLink = googleMapsPositionLink.strip();

            Pattern googleMapsLinkPattern = Pattern.compile("^https://maps\\.app\\.goo\\.gl/[A-Za-z0-9]{17}$");
            if (!googleMapsLinkPattern.matcher(googleMapsPositionLink).matches()) {
                throw new ModelException("Le format du lien Google Maps est invalide");
            }

            this.googleMapsPositionLink = googleMapsPositionLink;
        }
    }

    // ─── Special setters ─── //

    public void setAddressFromId(int addressId) throws ModelException {
        Address address;
        try {
            address = DataManagers.get(AddressDataManager.class).getAddress(addressId);
        } catch (LoadDataManagerDataException e) {
            throw new ModelException("Impossible de vérifier l'identifiant d'adresse '%d' (les adresses n'ont pas pu être chargées dans l'application)".formatted(addressId));
        }

        if (address == null) {
            throw new ModelException("Aucune des adresses enregistrées ne porte l'identifiant '%d'".formatted(addressId));
        }

        this.address = address;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.setName(dataObject.getName());
        this.setAddressFromId(dataObject.getAddressId());
        this.setGoogleMapsPositionLink(dataObject.getGoogleMapsPositionLink());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this.getId(), this.getName(), this.getAddress().getId(), this.getGoogleMapsPositionLink());
    }

    @Override
    public boolean isValid() {
        return this.id > 0 && this.name != null;
    }

    public static class Data implements CustomSerializable, JsonConvertible {
        private int id = -1;
        private String name;
        private int addressId = -1;
        private String googleMapsPositionLink;

        public Data() {}

        public Data(int id, String name, int addressId, String googleMapsPositionLink) throws ModelException {
            this.setId(id);
            this.setName(name);
            this.setAddressId(addressId);
            this.setGoogleMapsPositionLink(googleMapsPositionLink);
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public int getAddressId() {
            return this.addressId;
        }

        public String getGoogleMapsPositionLink() {
            return this.googleMapsPositionLink;
        }

        public void setId(int id) throws ModelException {
            if (id <= 0 && id != -1) {
                throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)");
            }

            this.id = id;
        }

        public void setId(String id) throws ModelException {
            int idAsInt;
            try {
                idAsInt = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)", e);
            }

            this.setId(idAsInt);
        }

        public void setName(String name) throws ModelException {
            if (name == null || name.isBlank()) {
                throw new ModelException("Le nom d'un club ne peut pas être vide");
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

        public void setGoogleMapsPositionLink(String googleMapsPositionLink) throws ModelException {
            if (googleMapsPositionLink == null || googleMapsPositionLink.isEmpty()) {
                this.googleMapsPositionLink = null;
            } else {
                if (googleMapsPositionLink.isBlank()) {
                    throw new ModelException("Le lien Google Maps d'un club ne peut pas être vide (null accepté)");
                }

                this.googleMapsPositionLink = googleMapsPositionLink.strip();
            }
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

                if (obj.has("googleMapsPositionLink") && !obj.get("googleMapsPositionLink").isJsonNull()) {
                    this.setGoogleMapsPositionLink(obj.get("googleMapsPositionLink").getAsString());
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
