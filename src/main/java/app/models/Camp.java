package app.models;

import app.models.managers.AddressDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.elements.money.Currency;
import app.utils.elements.money.Price;
import app.utils.elements.money.PriceException;
import app.utils.elements.money.exceptions.NotACurrencyException;
import app.utils.elements.money.exceptions.UnknownCurrencyException;
import com.google.gson.*;
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

public class Camp extends IdentifiedModel implements Hydratable<Camp.Data> {

    // ─── Properties ─── //

    private String name;
    @ModelReference(manager = AddressDataManager.class) private Address address;
    private int pendingAddressPk = -1;
    private TimeSlot timeSlot;
    private Price sessionsPricePerHour;

    // ─── Getters ─── //

    public String getName() {
        return this.name;
    }

    public Address getAddress() {
        return this.address;
    }

    public TimeSlot getTimeSlot() {
        return this.timeSlot;
    }

    public Price getSessionsPricePerHour() {
        return this.sessionsPricePerHour;
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
        this.name = verifyName(name);
    }

    public void setAddress(Address address) throws ModelException {
        if (address == null) {
            throw new ModelVerificationException("L'adresse de réference ne peut pas être nulle");
        }

        try {
            this.address = DataManagers.get(AddressDataManager.class).getAddressWithExceptions(address.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant d'adresse '%d'".formatted(address.getId()), e);
        }
    }

    public void setTimeSlot(TimeSlot timeSlot) throws ModelException {
        if (timeSlot == null) {
            throw new ModelVerificationException("La période de temps d'un stage ne peut pas être nulle");
        }

        this.timeSlot = timeSlot;
    }

    public void setSessionsPricePerHour(Price sessionsPricePerHour) throws ModelException {
        if (sessionsPricePerHour == null) {
            throw new ModelVerificationException("Le tarif horaire des sessions ne peut pas être nul");
        }

        this.sessionsPricePerHour = sessionsPricePerHour;
    }

    // ─── Special setters ─── //

    public void setAddressFromPk(int addressId) throws DataManagerException {
        try {
            this.address = DataManagers.get(AddressDataManager.class).getAddressWithExceptions(addressId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant d'adresse '%d' (les adresses n'ont pas pu être chargées dans l'application)".formatted(addressId), e);
        }
    }

    public void setAddressFromPk(String addressIdAsString) throws DataManagerException, ModelException {
        this.setAddressFromPk(Address.verifyId(addressIdAsString));
    }

    // ─── Utility methods ─── //

    public static String verifyName(String name) throws ModelException {
        return verifyNotNullOrEmpty(name, "Le nom d'un stage ne peut pas être vide ou nul");
    }

    public static Instant verifyTimeSlotStart(String timeSlotStart) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotStart);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de début d'un stage ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de début a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Instant verifyTimeSlotEnd(String timeSlotEnd) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotEnd);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de fin d'un stage ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de fin a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Currency verifySessionsPricePerHourCurrency(String priceCurrencyName) throws ModelException {
        try {
            priceCurrencyName = Validators.validateNotNullOrBlank(priceCurrencyName);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La monnaie du tarif horaire des sessions ne peut pas être vide ou nulle", e);
        }

        try {
            return Currency.convert(priceCurrencyName);
        } catch (UnknownCurrencyException e) {
            throw new ModelVerificationException("La monnaie du tarif horaire renseignée est inconnue", e);
        } catch (NotACurrencyException e) {
            throw new ModelVerificationException("La monnaie du tarif horaire des sessions ne peut pas être vide ou nulle", e);
        }
    }

    public static double verifySessionsPricePerHourAmount(String priceAmount) throws ModelException {
        try {
            priceAmount = Validators.validateNotNullOrBlank(priceAmount);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le montant du tarif horaire des sessions ne peut pas être vide ou nul", e);
        }

        double priceAmountAsDouble;
        try {
            priceAmountAsDouble = Double.parseDouble(priceAmount);
        } catch (NumberFormatException e) {
            throw new ModelVerificationException("Le montant du tarif horaire des sessions doit être un entier, ou un nombre à décimales, positif", e);
        }

        if (priceAmountAsDouble < 0) {
            throw new ModelVerificationException("Le montant du tarif horaire des sessions doit être un entier, ou un nombre à décimales, positif");
        }

        return priceAmountAsDouble;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Camp clone() {
        Camp clone = new Camp();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.name = this.name;
        clone.address = this.address;
        clone.pendingAddressPk = this.pendingAddressPk;
        clone.timeSlot = this.timeSlot;
        clone.sessionsPricePerHour = this.sessionsPricePerHour;

        return clone;
    }

    @Override
    public String toString() {
        return "#%d %s (%s)".formatted(this.getId(), this.name, this.timeSlot);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.name != null && this.address != null && this.timeSlot != null && this.sessionsPricePerHour != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.setName(dataObject.getName());
        this.pendingAddressPk = dataObject.getAddressId();
        this.setTimeSlot(dataObject.getTimeSlot());

        try {
            this.setSessionsPricePerHour(dataObject.getSessionsPricePerHour());
        } catch (NotACurrencyException e) {
            throw new ModelException("Le tarif horaire des sessions reçu à hydrater n'a pas une monnaie valide", e);
        } catch (PriceException e) {
            throw new ModelException("Le montant du tarif horaire des sessions reçu à hydrater n'est pas valide", e);
        }
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Camp.Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private String name;
        private int addressId = -1;
        private Instant timeSlotStart;
        private Instant timeSlotEnd;
        private Currency sessionsPricePerHourCurrency;
        private double sessionsPricePerHourAmount;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(Camp camp) throws ModelException {
            this.setId(camp.getId());
            this.setName(camp.getName());
            this.setAddressId(camp.getAddress().getId());
            this.setTimeSlotStart(camp.getTimeSlot().getFormattedStart());
            this.setTimeSlotEnd(camp.getTimeSlot().getFormattedEnd());
            this.setSessionsPricePerHourCurrency(camp.getSessionsPricePerHour());
            this.setSessionsPricePerHourAmount(camp.getSessionsPricePerHour());
        }

        // ─── Getters ─── //

        public String getName() {
            return this.name;
        }

        public int getAddressId() {
            return this.addressId;
        }

        public Instant getTimeSlotStart() {
            return this.timeSlotStart;
        }

        public Instant getTimeSlotEnd() {
            return this.timeSlotEnd;
        }

        public Currency getSessionsPricePerHourCurrency() {
            return this.sessionsPricePerHourCurrency;
        }

        public double getSessionsPricePerHourAmount() {
            return this.sessionsPricePerHourAmount;
        }

        // ─── Special getters ─── //

        public TimeSlot getTimeSlot() {
            return new TimeSlot(this.timeSlotStart, this.timeSlotEnd);
        }

        public Price getSessionsPricePerHour() throws NotACurrencyException, PriceException {
            return new Price(this.sessionsPricePerHourCurrency, this.sessionsPricePerHourAmount);
        }

        // ─── Setters ─── //

        public void setAddressId(String addressId) throws ModelException {
            this.addressId = verifyId(addressId);
        }

        public void setAddressId(int addressId) throws ModelException {
            this.setAddressId(String.valueOf(addressId));
        }

        public void setName(String name) throws ModelException {
            this.name = verifyName(name);
        }

        public void setTimeSlotStart(String timeSlotStart) throws ModelException {
            Instant timeSlotStartInstant = verifyTimeSlotStart(timeSlotStart);

            if (this.timeSlotEnd != null && (timeSlotStartInstant.isAfter(this.timeSlotEnd) || timeSlotStartInstant.equals(this.timeSlotEnd))) {
                throw new ModelVerificationException("La date de début d'un stage doit être strictement antérieure à sa date de fin");
            }

            this.timeSlotStart = timeSlotStartInstant;
        }

        public void setTimeSlotEnd(String timeSlotEnd) throws ModelException {
            Instant timeSlotEndInstant = verifyTimeSlotEnd(timeSlotEnd);

            if (this.timeSlotStart != null && (timeSlotEndInstant.isBefore(this.timeSlotStart) || timeSlotEndInstant.equals(this.timeSlotStart))) {
                throw new ModelVerificationException("La date de fin d'un stage doit être strictement postérieure à sa date de début");
            }

            this.timeSlotEnd = timeSlotEndInstant;
        }

        private void setSessionsPricePerHourCurrency(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("La monnaie du tarif horaire des sessions ne peut pas être nulle");
            }

            this.sessionsPricePerHourCurrency = price.getCurrency();
        }

        private void setSessionsPricePerHourCurrency(String priceCurrencyName) throws ModelException {
            this.sessionsPricePerHourCurrency = verifySessionsPricePerHourCurrency(priceCurrencyName);
        }

        private void setSessionsPricePerHourAmount(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("Le montant du tarif horaire des sessions ne peut pas être nul");
            }

            this.sessionsPricePerHourAmount = price.getAmount();
        }

        private void setSessionsPricePerHourAmount(String priceAmount) throws ModelException {
            this.sessionsPricePerHourAmount = verifySessionsPricePerHourAmount(priceAmount);
        }

        private void setSessionsPricePerHourAmount(Double priceAmount) throws ModelException {
            this.setSessionsPricePerHourAmount(String.valueOf(priceAmount));
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
            if (!obj.has("timeSlotStart")) {
                throw new StringParserException("Le champ 'timeSlotStart' est manquant");
            }
            if (!obj.has("timeSlotEnd")) {
                throw new StringParserException("Le champ 'timeSlotEnd' est manquant");
            }
            if (!obj.has("sessionsPricePerHourCurrency")) {
                throw new StringParserException("Le champ 'sessionsPricePerHourCurrency' est manquant");
            }
            if (!obj.has("sessionsPricePerHourAmount")) {
                throw new StringParserException("Le champ 'sessionsPricePerHourAmount' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setName(obj.get("name").getAsString());
                this.setAddressId(obj.get("addressId").getAsString());
                this.setTimeSlotStart(obj.get("timeSlotStart").getAsString());
                this.setTimeSlotEnd(obj.get("timeSlotEnd").getAsString());
                this.setSessionsPricePerHourCurrency(obj.get("sessionsPricePerHourCurrency").getAsString());
                this.setSessionsPricePerHourAmount(obj.get("sessionsPricePerHourAmount").getAsString());
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
