package app.models;

import app.models.managers.CountryDataManager;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import utils.io.helpers.tables.TableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignement;

import java.util.regex.Pattern;

public class Address extends Model implements Hydratable<Address.Data> {

    // ─── Properties ─── //

    private int id = -1;
    @ModelReference(manager = CountryDataManager.class) private Country country;
    private String pendingCountryPk;
    private int zipCode;
    private String city;
    private String street;
    private String number;
    private Integer boxNumber;

    // ─── Getters ─── //

    @TableDisplay(name = "#", format = @TableDisplayFormattingOptions(preset = ModelPrimaryKeyTextFormattingPreset.class, alignment = TextAlignement.RIGHT), order = 1)
    public int getId() {
        return this.id;
    }

    public Country getCountry() {
        return this.country;
    }

    @TableDisplay(name = "Code postal", order = 3)
    public int getZipCode() {
        return this.zipCode;
    }

    @TableDisplay(name = "Ville", order = 4)
    public String getCity() {
        return this.city;
    }

    @TableDisplay(name = "Rue", order = 5)
    public String getStreet() {
        return this.street;
    }

    @TableDisplay(name = "Numéro", order = 6)
    public String getNumber() {
        return this.number;
    }

    @TableDisplay(name = "Boîte", order = 7)
    public Integer getBoxNumber() {
        return this.boxNumber;
    }

    // ─── Setters ─── //

    public void setId(int id) throws ModelException {
        if (id <= 0 && id != -1) {
            throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)");
        }

        this.id = id;
    }

    public void setCountry(Country country) throws ModelException {
        if (country == null) {
            throw new ModelException("Le pays est requis pour une adresse (valeur null reçue)");
        }

        this.setCountryFromPk(country.getIso3());
    }

    public void setZipCode(int zipCode) throws ModelException {
        if (zipCode <= 0) {
            throw new ModelException("Le code postal doit être un entier strictement positif");
        } else if (zipCode > 99999) {
            throw new ModelException("Le code postal doit contenir au maximum 5 chiffres");
        }

        this.zipCode = zipCode;
    }

    public void setCity(String city) throws ModelException {
        if (city == null || city.isBlank()) {
            throw new ModelException("Le nom de la ville ne peut pas être vide");
        }

        this.city = city.strip();
    }

    public void setStreet(String street) throws ModelException {
        if (street == null || street.isBlank()) {
            throw new ModelException("Le nom de la rue ne peut pas être vide");
        }

        this.street = street.strip();
    }

    public void setNumber(String number) throws ModelException {
        if (number == null || number.isBlank()) {
            throw new ModelException("Le numéro ne peut pas être vide");
        }

        this.number = number.strip();
    }

    public void setBoxNumber(Integer boxNumber) throws ModelException {
        if (boxNumber == null) {
            this.boxNumber = null;
        } else {
            if (boxNumber <= 0) {
                throw new ModelException("Le numéro de bôite doit être un entier strictement positif");
            }

            this.boxNumber = boxNumber;
        }
    }

    // ─── Special setters ─── //

    public void setCountryFromPk(String iso3) throws ModelException {
        iso3 = verifyCountryIso3(iso3);

        try {
            this.country = DataManagers.initAndGet(CountryDataManager.class).getCountryWithExceptions(iso3);
        } catch (LoadDataManagerDataException e) {
            throw new ModelException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3));
        }
    }

    private static String verifyCountryIso3(String iso3) throws ModelException {
        if (iso3 == null || iso3.isBlank()) {
            throw new ModelException("L'ISO3 ne peut pas être vide");
        }

        iso3 = iso3.strip();

        if (iso3.length() != 3) {
            throw new ModelException("L'ISO3 d'un pays doit être long de 3 caractères");
        }

        iso3 = iso3.toUpperCase();

        Pattern iso3Pattern = Pattern.compile("^[A-Z]{3}$");
        if (!iso3Pattern.matcher(iso3).matches()) {
            throw new ModelException("L'ISO3 d'un pays doit être composé uniquement de lettres");
        }
        return iso3;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String result = this.street + " " + this.number;
        if (this.boxNumber != null) {
            result += "/" + this.boxNumber;
        }
        result += ", " + this.zipCode + " " + this.city + ", " + this.country;
        return result;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingCountryPk = dataObject.getCountryIso3();
        this.setZipCode(dataObject.getZipCode());
        this.setCity(dataObject.getCity());
        this.setStreet(dataObject.getStreet());
        this.setNumber(dataObject.getNumber());
        this.setBoxNumber(dataObject.getBoxNumber());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    @Override
    public boolean isValid() {
        return this.id > 0 && this.country != null && this.zipCode > 0 && this.city != null && this.street != null && this.number != null;
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int id = -1;
        private String countryIso3;
        private int zipCode;
        private String city;
        private String street;
        private String number;
        private Integer boxNumber;

        // ─── Constructors ─── //

        public Data() {}

        public Data(Address address) throws ModelException {
            this.setId(address.getId());
            this.setCountryIso3(address.getCountry().getIso3());
            this.setZipCode(address.getZipCode());
            this.setCity(address.getCity());
            this.setStreet(address.getStreet());
            this.setNumber(address.getNumber());
            this.setBoxNumber(address.getBoxNumber());
        }

        // ─── Getters ─── //

        public int getId() {
            return this.id;
        }

        public String getCountryIso3() {
            return this.countryIso3;
        }

        public int getZipCode() {
            return this.zipCode;
        }

        public String getCity() {
            return this.city;
        }

        public String getStreet() {
            return this.street;
        }

        public String getNumber() {
            return this.number;
        }

        public Integer getBoxNumber() {
            return this.boxNumber;
        }

        // ─── Setters ─── //

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

        public void setCountryIso3(String countryIso3) throws ModelException {
            countryIso3 = verifyCountryIso3(countryIso3);

            this.countryIso3 = countryIso3.strip();
        }

        public void setZipCode(int zipCode) throws ModelException {
            if (zipCode <= 0) {
                throw new ModelException("Le code postal doit être un entier strictement positif");
            } else if (zipCode > 99999) {
                throw new ModelException("Le code postal doit contenir au maximum 5 chiffres");
            }

            this.zipCode = zipCode;
        }

        public void setZipCode(String zipCode) throws ModelException {
            if (zipCode == null || zipCode.isBlank()) {
                throw new ModelException("Le code postal ne peut pas être vide");
            }

            int intZipCode;
            try {
                intZipCode = Integer.parseInt(zipCode);
            } catch (NumberFormatException e) {
                throw new ModelException("Le code postal doit être un entier strictement positif");
            }

            this.setZipCode(intZipCode);
        }

        public void setCity(String city) throws ModelException {
            if (city == null || city.isBlank()) {
                throw new ModelException("Le nom de la ville ne peut pas être vide");
            }

            this.city = city.strip();
        }

        public void setStreet(String street) throws ModelException {
            if (street == null || street.isBlank()) {
                throw new ModelException("Le nom de la rue ne peut pas être vide");
            }

            this.street = street.strip();
        }

        public void setNumber(String number) throws ModelException {
            if (number == null || number.isBlank()) {
                throw new ModelException("Le numéro ne peut pas être vide");
            }

            this.number = number.strip();
        }

        public void setBoxNumber(Integer boxNumber) throws ModelException {
            if (boxNumber == null) {
                this.boxNumber = null;
            } else {
                if (boxNumber <= 0) {
                    throw new ModelException("Le numéro de bôite doit être un entier strictement positif");
                }

                this.boxNumber = boxNumber;
            }
        }

        public void setBoxNumber(String boxNumber) throws ModelException {
            if (boxNumber == null || boxNumber.isEmpty()) {
                this.boxNumber = null;
            } else {
                if (boxNumber.isBlank()) {
                    throw new ModelException("Le numéro de bôite ne peut pas être vide");
                }

                boxNumber = boxNumber.strip();

                int intBoxNumber;
                try {
                    intBoxNumber = Integer.parseInt(boxNumber);
                } catch (NumberFormatException e) {
                    throw new ModelException("Le numéro de bôite doit être un entier strictement positif");
                }

                this.setBoxNumber(intBoxNumber);
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
            if (!obj.has("countryIso3")) {
                throw new StringParserException("Le champ 'countryIso3' est manquant");
            }
            if (!obj.has("zipCode")) {
                throw new StringParserException("Le champ 'zipCode' est manquant");
            }
            if (!obj.has("city")) {
                throw new StringParserException("Le champ 'city' est manquant");
            }
            if (!obj.has("street")) {
                throw new StringParserException("Le champ 'street' est manquant");
            }
            if (!obj.has("number")) {
                throw new StringParserException("Le champ 'number' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setCountryIso3(obj.get("countryIso3").getAsString());
                this.setZipCode(obj.get("zipCode").getAsString());
                this.setCity(obj.get("city").getAsString());
                this.setStreet(obj.get("street").getAsString());
                this.setNumber(obj.get("number").getAsString());

                if (obj.has("boxNumber") && !obj.get("boxNumber").isJsonNull()) {
                    this.setBoxNumber(obj.get("boxNumber").getAsString());
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
