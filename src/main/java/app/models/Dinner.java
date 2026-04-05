package app.models;

import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.elements.money.Currency;
import app.utils.elements.money.Price;
import app.utils.elements.money.exceptions.NotACurrencyException;
import app.utils.elements.money.exceptions.UnknownCurrencyException;
import app.utils.tarification.ChargingElement;
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

public class Dinner extends IdentifiedModel implements Hydratable<Dinner.Data>, CampScheduledItem {

    // ─── Properties ─── //

    @ModelReference(manager = CampDataManager.class) private Camp camp;
    private int pendingCampPk = -1;
    private String label;
    private TimeSlot timeSlot;
    private Price price;

    // ─── Getters ─── //

    public String getLabel() {
        return this.label;
    }

    public Camp getCamp() {
        return this.camp;
    }

    public TimeSlot getTimeSlot() {
        return this.timeSlot;
    }

    public Price getPrice() {
        return this.price;
    }

    // ─── Special getters ─── //

    public int getCampId() throws ModelException {
        if (this.camp == null) {
            throw new ModelException("Le stage de référence est nul");
        }

        return this.camp.getId();
    }

    // ─── Setters ─── //

    public void setLabel(String label) throws ModelException {
        this.label = verifyLabel(label);
    }

    public void setCamp(Camp camp) throws ModelException {
        if (camp == null) {
            throw new ModelVerificationException("Le camp de référence ne peut pas être nul");
        }

        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(camp.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant du stage '%d'".formatted(camp.getId()), e);
        }
    }

    public void setTimeSlot(TimeSlot timeSlot) throws ModelException {
        if (timeSlot == null) {
            throw new ModelVerificationException("La période de temps d'un repas ne peut pas être nulle");
        }

        CampScheduledItem.validateTimeSlotWithinCampBounds(this.camp, timeSlot);

        this.timeSlot = timeSlot;
    }

    public void setPrice(Price price) throws ModelException {
        if (price == null) {
            throw new ModelVerificationException("Le prix d'un repas ne peut pas être nul");
        }

        this.price = price;
    }

    // ─── Special setters ─── //

    public void setCampFromPk(int campId) throws DataManagerException {
        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(campId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant du stage '%d' (les stages n'ont pas pu être chargées dans l'application)".formatted(campId), e);
        }
    }

    public void setCampFromPk(String campIdAsString) throws DataManagerException, ModelException {
        this.setCampFromPk(Camp.verifyId(campIdAsString));
    }

    // ─── Utility methods ─── //

    public static String verifyLabel(String label) throws ModelException {
        return verifyNotNullOrEmpty(label, "Le nom d'un repas ne peut pas être vide ou nul");
    }

    public static Instant verifyTimeSlotStart(String timeSlotStart) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotStart);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de début d'un repas ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de début a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Instant verifyTimeSlotEnd(String timeSlotEnd) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotEnd);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de fin d'un repas ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de fin a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Currency verifyPriceCurrency(String priceCurrencyName) throws ModelException {
        try {
            priceCurrencyName = Validators.validateNotNullOrBlank(priceCurrencyName);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La monnaie du prix d'un repas ne peut pas être vide ou nul", e);
        }

        try {
            return Currency.convert(priceCurrencyName);
        } catch (UnknownCurrencyException e) {
            throw new ModelVerificationException("La monnaie du prix renseignée est inconnue", e);
        } catch (NotACurrencyException e) {
            throw new ModelVerificationException("La monnaie du prix d'un repas ne peut pas être vide ou nul", e);
        }
    }

    public static double verifyPriceAmount(String priceAmount) throws ModelException {
        try {
            priceAmount = Validators.validateNotNullOrBlank(priceAmount);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le montant du prix d'un repas ne peut pas être vide ou nul", e);
        }

        double priceAmountAsDouble;
        try {
            priceAmountAsDouble = Double.parseDouble(priceAmount);
        } catch (NumberFormatException e) {
            throw new ModelVerificationException("Le montant du prix d'un repas doit être un entier, ou un nombre à décimales, positif", e);
        }

        if (priceAmountAsDouble < 0) {
            throw new ModelVerificationException("Le montant du prix d'un repas doit être un entier, ou un nombre à décimales, positif");
        }

        return priceAmountAsDouble;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Dinner clone() {
        Dinner clone = new Dinner();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.camp = this.camp;
        clone.pendingCampPk = this.pendingCampPk;
        clone.label = this.label;
        clone.timeSlot = this.timeSlot;
        clone.price = this.price;

        return clone;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingCampPk = dataObject.getCampId();
        this.setLabel(dataObject.getName());
        this.setTimeSlot(dataObject.getTimeSlot());

        try {
            this.setPrice(dataObject.getPrice());
        } catch (NotACurrencyException e) {
            throw new ModelException("Le prix reçu à hydrater pour ce repas n'a pas une monnaie valide", e);
        }
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    @Override
    public String toString() {
        return "#%d %s (%s)".formatted(this.getId(), this.label, this.timeSlot);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.camp != null && this.label != null && this.timeSlot != null && this.price != null;
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private String name;
        private int campId = -1;
        private Instant timeSlotStart;
        private Instant timeSlotEnd;
        private Currency priceCurrency;
        private double priceAmount;

        // ─── Constructors ─── //

        public Data() {}

        public Data(Dinner dinner) throws ModelException {
            this.setId(dinner.getId());
            this.setName(dinner.getLabel());
            this.setCampId(dinner.getCamp().getId());
            this.setTimeSlotStart(dinner.getTimeSlot().getFormattedStart());
            this.setTimeSlotEnd(dinner.getTimeSlot().getFormattedEnd());
            this.setPriceCurrency(dinner.getPrice());
            this.setPriceAmount(dinner.getPrice());
        }

        // ─── Getters ─── //

        public String getName() {
            return this.name;
        }

        public int getCampId() {
            return this.campId;
        }

        public Instant getTimeSlotStart() {
            return this.timeSlotStart;
        }

        public Instant getTimeSlotEnd() {
            return this.timeSlotEnd;
        }

        public Currency getPriceCurrency() {
            return this.priceCurrency;
        }

        public double getPriceAmount() {
            return this.priceAmount;
        }

        // ─── Special getters ─── //

        public TimeSlot getTimeSlot() {
            return new TimeSlot(this.timeSlotStart, this.timeSlotEnd);
        }

        public Price getPrice() throws NotACurrencyException {
            return new Price(this.priceCurrency, this.priceAmount);
        }

        // ─── Setters ─── //

        public void setName(String name) throws ModelException {
            this.name = verifyLabel(name);
        }

        public void setCampId(String campId) throws ModelException {
            this.campId = verifyId(campId);
        }

        public void setCampId(int campId) throws ModelException {
            this.setCampId(String.valueOf(campId));
        }

        public void setTimeSlotStart(String timeSlotStart) throws ModelException {
            Instant timeSlotStartInstant = verifyTimeSlotStart(timeSlotStart);

            if (this.timeSlotEnd != null && (timeSlotStartInstant.isAfter(this.timeSlotEnd) || timeSlotStartInstant.equals(this.timeSlotEnd))) {
                throw new ModelVerificationException("La date de début d'un repas doit être strictement antérieure à sa date de fin");
            }

            this.timeSlotStart = timeSlotStartInstant;
        }

        public void setTimeSlotEnd(String timeSlotEnd) throws ModelException {
            Instant timeSlotEndInstant = verifyTimeSlotEnd(timeSlotEnd);

            if (this.timeSlotStart != null && (timeSlotEndInstant.isBefore(this.timeSlotStart) || timeSlotEndInstant.equals(this.timeSlotStart))) {
                throw new ModelVerificationException("La date de fin d'un repas doit être strictement postérieure à sa date de début");
            }

            this.timeSlotEnd = timeSlotEndInstant;
        }

        private void setPriceCurrency(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("La monnaie du prix d'un repas ne peut pas être nul");
            }

            this.priceCurrency = price.getCurrency();
        }

        private void setPriceCurrency(String priceCurrencyName) throws ModelException {
            this.priceCurrency = verifyPriceCurrency(priceCurrencyName);
        }

        private void setPriceAmount(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("Le montant du prix d'un repas ne peut pas être nul");
            }

            this.priceAmount = price.getAmount();
        }

        private void setPriceAmount(String priceAmount) throws ModelException {
            this.priceAmount = verifyPriceAmount(priceAmount);
        }

        private void setPriceAmount(Double priceAmount) throws ModelException {
            this.setPriceAmount(String.valueOf(priceAmount));
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
            if (!obj.has("campId")) {
                throw new StringParserException("Le champ 'campId' est manquant");
            }
            if (!obj.has("name")) {
                throw new StringParserException("Le champ 'name' est manquant");
            }
            if (!obj.has("timeSlotStart")) {
                throw new StringParserException("Le champ 'timeSlotStart' est manquant");
            }
            if (!obj.has("timeSlotEnd")) {
                throw new StringParserException("Le champ 'timeSlotEnd' est manquant");
            }
            if (!obj.has("priceCurrency")) {
                throw new StringParserException("Le champ 'priceCurrency' est manquant");
            }
            if (!obj.has("priceAmount")) {
                throw new StringParserException("Le champ 'priceAmount' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setCampId(obj.get("campId").getAsString());
                this.setName(obj.get("name").getAsString());
                this.setTimeSlotStart(obj.get("timeSlotStart").getAsString());
                this.setTimeSlotEnd(obj.get("timeSlotEnd").getAsString());
                this.setPriceCurrency(obj.get("priceCurrency").getAsString());
                this.setPriceAmount(obj.get("priceAmount").getAsString());
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
