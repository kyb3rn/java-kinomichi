package app.models;

import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.elements.money.Currency;
import app.utils.elements.money.Price;
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

public class Lodging extends IdentifiedModel implements Hydratable<Lodging.Data>, CampScheduledItem {

    // ─── Properties ─── //

    @ModelReference(manager = CampDataManager.class) private Camp camp;
    private int pendingCampPk = -1;
    private String label;
    private TimeSlot timeSlot;
    private Price sharedRoomPrice;
    private Price singleRoomPrice;

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

    public Price getSharedRoomPrice() {
        return this.sharedRoomPrice;
    }

    public Price getSingleRoomPrice() {
        return this.singleRoomPrice;
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
            throw new ModelVerificationException("La période de temps d'un hébergement ne peut pas être nulle");
        }

        CampScheduledItem.validateTimeSlotWithinCampBounds(this.camp, timeSlot);

        this.timeSlot = timeSlot;
    }

    public void setSharedRoomPrice(Price sharedRoomPrice) throws ModelException {
        if (sharedRoomPrice == null) {
            throw new ModelVerificationException("Le prix de la chambre partagée ne peut pas être nul");
        }

        this.sharedRoomPrice = sharedRoomPrice;
    }

    public void setSingleRoomPrice(Price singleRoomPrice) throws ModelException {
        if (singleRoomPrice == null) {
            throw new ModelVerificationException("Le prix de la chambre individuelle ne peut pas être nul");
        }

        this.singleRoomPrice = singleRoomPrice;
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
        return verifyNotNullOrEmpty(label, "Le nom d'un hébergement ne peut pas être vide ou nul");
    }

    public static Instant verifyTimeSlotStart(String timeSlotStart) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotStart);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de début d'un hébergement ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de début a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Instant verifyTimeSlotEnd(String timeSlotEnd) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotEnd);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de fin d'un hébergement ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de fin a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Currency verifyPriceCurrency(String priceCurrencyName) throws ModelException {
        try {
            priceCurrencyName = Validators.validateNotNullOrBlank(priceCurrencyName);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La monnaie du prix d'un hébergement ne peut pas être vide ou nul", e);
        }

        try {
            return Currency.convert(priceCurrencyName);
        } catch (UnknownCurrencyException e) {
            throw new ModelVerificationException("La monnaie du prix renseignée est inconnue", e);
        } catch (NotACurrencyException e) {
            throw new ModelVerificationException("La monnaie du prix d'un hébergement ne peut pas être vide ou nul", e);
        }
    }

    public static double verifySharedRoomPriceAmount(String priceAmount) throws ModelException {
        return verifyPriceAmount(priceAmount, "chambre partagée");
    }

    public static double verifySingleRoomPriceAmount(String priceAmount) throws ModelException {
        return verifyPriceAmount(priceAmount, "chambre individuelle");
    }

    private static double verifyPriceAmount(String priceAmount, String priceLabel) throws ModelException {
        try {
            priceAmount = Validators.validateNotNullOrBlank(priceAmount);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le montant du prix de la %s ne peut pas être vide ou nul".formatted(priceLabel), e);
        }

        double priceAmountAsDouble;
        try {
            priceAmountAsDouble = Double.parseDouble(priceAmount);
        } catch (NumberFormatException e) {
            throw new ModelVerificationException("Le montant du prix de la %s doit être un entier, ou un nombre à décimales, positif".formatted(priceLabel), e);
        }

        if (priceAmountAsDouble < 0) {
            throw new ModelVerificationException("Le montant du prix de la %s doit être un entier, ou un nombre à décimales, positif".formatted(priceLabel));
        }

        return priceAmountAsDouble;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Lodging clone() {
        Lodging clone = new Lodging();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.camp = this.camp;
        clone.pendingCampPk = this.pendingCampPk;
        clone.label = this.label;
        clone.timeSlot = this.timeSlot;
        clone.sharedRoomPrice = this.sharedRoomPrice;
        clone.singleRoomPrice = this.singleRoomPrice;

        return clone;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingCampPk = dataObject.getCampId();
        this.setLabel(dataObject.getName());
        this.setTimeSlot(dataObject.getTimeSlot());

        try {
            this.setSharedRoomPrice(dataObject.getSharedRoomPrice());
        } catch (NotACurrencyException e) {
            throw new ModelException("Le prix de chambre partagée reçu à hydrater pour cet hébergement n'a pas une monnaie valide", e);
        }

        try {
            this.setSingleRoomPrice(dataObject.getSingleRoomPrice());
        } catch (NotACurrencyException e) {
            throw new ModelException("Le prix de chambre individuelle reçu à hydrater pour cet hébergement n'a pas une monnaie valide", e);
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
        return this.getId() > 0 && this.camp != null && this.label != null && this.timeSlot != null && this.sharedRoomPrice != null && this.singleRoomPrice != null;
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private String name;
        private int campId = -1;
        private Instant timeSlotStart;
        private Instant timeSlotEnd;
        private Currency sharedRoomPriceCurrency;
        private double sharedRoomPriceAmount;
        private Currency singleRoomPriceCurrency;
        private double singleRoomPriceAmount;

        // ─── Constructors ─── //

        public Data() {}

        public Data(Lodging lodging) throws ModelException {
            this.setId(lodging.getId());
            this.setName(lodging.getLabel());
            this.setCampId(lodging.getCamp().getId());
            this.setTimeSlotStart(lodging.getTimeSlot().getFormattedStart());
            this.setTimeSlotEnd(lodging.getTimeSlot().getFormattedEnd());
            this.setSharedRoomPriceCurrency(lodging.getSharedRoomPrice());
            this.setSharedRoomPriceAmount(lodging.getSharedRoomPrice());
            this.setSingleRoomPriceCurrency(lodging.getSingleRoomPrice());
            this.setSingleRoomPriceAmount(lodging.getSingleRoomPrice());
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

        public Currency getSharedRoomPriceCurrency() {
            return this.sharedRoomPriceCurrency;
        }

        public double getSharedRoomPriceAmount() {
            return this.sharedRoomPriceAmount;
        }

        public Currency getSingleRoomPriceCurrency() {
            return this.singleRoomPriceCurrency;
        }

        public double getSingleRoomPriceAmount() {
            return this.singleRoomPriceAmount;
        }

        // ─── Special getters ─── //

        public TimeSlot getTimeSlot() {
            return new TimeSlot(this.timeSlotStart, this.timeSlotEnd);
        }

        public Price getSharedRoomPrice() throws NotACurrencyException {
            return new Price(this.sharedRoomPriceCurrency, this.sharedRoomPriceAmount);
        }

        public Price getSingleRoomPrice() throws NotACurrencyException {
            return new Price(this.singleRoomPriceCurrency, this.singleRoomPriceAmount);
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
                throw new ModelVerificationException("La date de début d'un hébergement doit être strictement antérieure à sa date de fin");
            }

            this.timeSlotStart = timeSlotStartInstant;
        }

        public void setTimeSlotEnd(String timeSlotEnd) throws ModelException {
            Instant timeSlotEndInstant = verifyTimeSlotEnd(timeSlotEnd);

            if (this.timeSlotStart != null && (timeSlotEndInstant.isBefore(this.timeSlotStart) || timeSlotEndInstant.equals(this.timeSlotStart))) {
                throw new ModelVerificationException("La date de fin d'un hébergement doit être strictement postérieure à sa date de début");
            }

            this.timeSlotEnd = timeSlotEndInstant;
        }

        private void setSharedRoomPriceCurrency(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("La monnaie du prix de la chambre partagée ne peut pas être nul");
            }

            this.sharedRoomPriceCurrency = price.getCurrency();
        }

        private void setSharedRoomPriceCurrency(String priceCurrencyName) throws ModelException {
            this.sharedRoomPriceCurrency = verifyPriceCurrency(priceCurrencyName);
        }

        private void setSharedRoomPriceAmount(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("Le montant du prix de la chambre partagée ne peut pas être nul");
            }

            this.sharedRoomPriceAmount = price.getAmount();
        }

        private void setSharedRoomPriceAmount(String priceAmount) throws ModelException {
            this.sharedRoomPriceAmount = verifySharedRoomPriceAmount(priceAmount);
        }

        private void setSharedRoomPriceAmount(Double priceAmount) throws ModelException {
            this.setSharedRoomPriceAmount(String.valueOf(priceAmount));
        }

        private void setSingleRoomPriceCurrency(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("La monnaie du prix de la chambre individuelle ne peut pas être nul");
            }

            this.singleRoomPriceCurrency = price.getCurrency();
        }

        private void setSingleRoomPriceCurrency(String priceCurrencyName) throws ModelException {
            this.singleRoomPriceCurrency = verifyPriceCurrency(priceCurrencyName);
        }

        private void setSingleRoomPriceAmount(Price price) throws ModelException {
            if (price == null) {
                throw new ModelVerificationException("Le montant du prix de la chambre individuelle ne peut pas être nul");
            }

            this.singleRoomPriceAmount = price.getAmount();
        }

        private void setSingleRoomPriceAmount(String priceAmount) throws ModelException {
            this.singleRoomPriceAmount = verifySingleRoomPriceAmount(priceAmount);
        }

        private void setSingleRoomPriceAmount(Double priceAmount) throws ModelException {
            this.setSingleRoomPriceAmount(String.valueOf(priceAmount));
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
            if (!obj.has("sharedRoomPriceCurrency")) {
                throw new StringParserException("Le champ 'sharedRoomPriceCurrency' est manquant");
            }
            if (!obj.has("sharedRoomPriceAmount")) {
                throw new StringParserException("Le champ 'sharedRoomPriceAmount' est manquant");
            }
            if (!obj.has("singleRoomPriceCurrency")) {
                throw new StringParserException("Le champ 'singleRoomPriceCurrency' est manquant");
            }
            if (!obj.has("singleRoomPriceAmount")) {
                throw new StringParserException("Le champ 'singleRoomPriceAmount' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setCampId(obj.get("campId").getAsString());
                this.setName(obj.get("name").getAsString());
                this.setTimeSlotStart(obj.get("timeSlotStart").getAsString());
                this.setTimeSlotEnd(obj.get("timeSlotEnd").getAsString());
                this.setSharedRoomPriceCurrency(obj.get("sharedRoomPriceCurrency").getAsString());
                this.setSharedRoomPriceAmount(obj.get("sharedRoomPriceAmount").getAsString());
                this.setSingleRoomPriceCurrency(obj.get("singleRoomPriceCurrency").getAsString());
                this.setSingleRoomPriceAmount(obj.get("singleRoomPriceAmount").getAsString());
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
