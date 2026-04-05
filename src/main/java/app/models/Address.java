package app.models;

import app.models.managers.CountryDataManager;
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
import utils.helpers.validation.*;

import java.util.stream.Collectors;

public class Address extends IdentifiedModel implements Hydratable<Address.Data> {

    // ─── Properties ─── //

    @ModelReference(manager = CountryDataManager.class) private Country country;
    private String pendingCountryPk;
    private int zipCode;
    private String city;
    private String street;
    private String number;
    private Integer boxNumber;

    // ─── Getters ─── //

    public Country getCountry() {
        return this.country;
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

    // ─── Special getters ─── //

    public String getCountryIso3() throws ModelException {
        if (this.country == null) {
            throw new ModelException("Le pays de référence de l'adresse est nul");
        }

        return this.country.getIso3();
    }

    // ─── Setters ─── //

    public void setCountry(Country country) throws ModelException {
        if (country == null) {
            throw new ModelVerificationException("Le pays de réference d'une adresse ne peut pas être nul");
        }

        try {
            this.country = DataManagers.get(CountryDataManager.class).getCountryWithExceptions(country.getIso3());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'ISO3 '%s'".formatted(country.getIso3()), e);
        }
    }

    public void setZipCode(int zipCode) throws ModelException {
        this.zipCode = verifyZipCode(String.valueOf(zipCode));
    }

    public void setCity(String city) throws ModelException {
        this.city = verifyCity(city);
    }

    public void setStreet(String street) throws ModelException {
        this.street = verifyStreet(street);
    }

    public void setNumber(String number) throws ModelException {
        this.number = verifyNumber(number);
    }

    public void setBoxNumber(Integer boxNumber) throws ModelException {
        this.boxNumber = verifyBoxNumber(boxNumber == null ? null : boxNumber.toString());
    }

    // ─── Special setters ─── //

    public void setCountryFromPk(String iso3) throws DataManagerException {
        try {
            this.country = DataManagers.get(CountryDataManager.class).getCountryWithExceptions(iso3);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3), e);
        }
    }

    // ─── Utility methods ─── //

    public static int verifyZipCode(String zipCode) throws ModelException {
        int zipCodeAsInt;
        try {
            zipCodeAsInt = Validators.validateStrictlyPositiveInt(zipCode);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le code postal d'une adresse ne peut pas être vide ou nul", e);
        } catch (ParsingValidatorException | BelowBoundaryValidatorException e) {
            throw new ModelVerificationException("Le code postal d'une adresse doit être un entier strictement positif");
        }

        if (zipCodeAsInt > 99999) {
            throw new ModelVerificationException("Le code postal d'une adresse doit contenir au maximum 5 chiffres");
        }

        return zipCodeAsInt;
    }

    public static String verifyCity(String city) throws ModelException {
        return verifyNotNullOrEmpty(city, "Le nom de la ville d'une adresse ne peut pas être vide ou nul");
    }

    public static String verifyStreet(String street) throws ModelException {
        return verifyNotNullOrEmpty(street, "Le nom de la rue d'une adresse ne peut pas être vide ou nul");
    }

    public static String verifyNumber(String number) throws ModelException {
        return verifyNotNullOrEmpty(number, "Le numéro d'une adresse ne peut pas être vide ou nul");
    }

    public static Integer verifyBoxNumber(String boxNumber) throws ModelException {
        try {
            boxNumber = Validators.validateNotNullOrStrictlyEmpty(boxNumber);
        } catch (StrictlyEmptyOrNullValueValidatorException e) {
            return null;
        }

        if (boxNumber.isBlank()) {
            throw new ModelVerificationException("Le numéro de boîte d'une adresse ne peut pas être vide (null accepté)");
        }

        boxNumber = boxNumber.strip();

        try {
            return Validators.validateStrictlyPositiveInteger(boxNumber);
        } catch (BlankOrNullValueValidatorException | ParsingValidatorException | BelowBoundaryValidatorException e) {
            throw new ModelVerificationException("Le numéro de boîte d'une adresse doit être un entier strictement positif", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Address clone() {
        Address clone = new Address();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.country = this.country;
        clone.pendingCountryPk = this.pendingCountryPk;
        clone.zipCode = this.zipCode;
        clone.city = this.city;
        clone.street = this.street;
        clone.number = this.number;
        clone.boxNumber = this.boxNumber;

        return clone;
    }

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
        return this.getId() > 0 && this.country != null && this.zipCode > 0 && this.city != null && this.street != null && this.number != null;
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

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

        public void setCountryIso3(String countryIso3) throws ModelException {
            this.countryIso3 = Country.verifyIso3(countryIso3);
        }

        public void setZipCode(String zipCode) throws ModelException {
            this.zipCode = verifyZipCode(zipCode);
        }

        public void setZipCode(int zipCode) throws ModelException {
            this.setZipCode(String.valueOf(zipCode));
        }

        public void setCity(String city) throws ModelException {
            this.city = verifyCity(city);
        }

        public void setStreet(String street) throws ModelException {
            this.street = verifyStreet(street);
        }

        public void setNumber(String number) throws ModelException {
            this.number = verifyNumber(number);
        }

        public void setBoxNumber(String boxNumber) throws ModelException {
            this.boxNumber = verifyBoxNumber(boxNumber);
        }

        public void setBoxNumber(Integer boxNumber) throws ModelException {
            this.setBoxNumber(boxNumber == null ? null : boxNumber.toString());
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
