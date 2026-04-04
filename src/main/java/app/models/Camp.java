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

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        return "#%d %s (%s)".formatted(this.getId(), this.name, this.timeSlot);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.name != null && this.address != null && this.timeSlot != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.setName(dataObject.getName());
        this.pendingAddressPk = dataObject.getAddressId();
        this.setTimeSlot(dataObject.getTimeSlot());
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

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(Camp camp) throws ModelException {
            this.setId(camp.getId());
            this.setName(camp.getName());
            this.setAddressId(camp.getAddress().getId());
            this.setTimeSlotStart(camp.getTimeSlot().getFormattedStart());
            this.setTimeSlotEnd(camp.getTimeSlot().getFormattedEnd());
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

        // ─── Special getters ─── //

        public TimeSlot getTimeSlot() {
            return new TimeSlot(this.timeSlotStart, this.timeSlotEnd);
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

            try {
                this.setId(obj.get("id").getAsString());
                this.setName(obj.get("name").getAsString());
                this.setAddressId(obj.get("addressId").getAsString());
                this.setTimeSlotStart(obj.get("timeSlotStart").getAsString());
                this.setTimeSlotEnd(obj.get("timeSlotEnd").getAsString());
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
