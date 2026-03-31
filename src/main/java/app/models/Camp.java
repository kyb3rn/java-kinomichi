package app.models;

import app.models.formatting.ModelKeyTextFormattingPreset;
import app.models.managers.AddressDataManager;
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
import utils.io.helpers.tables.TableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextStyle;
import utils.time.TimeSlot;

import java.time.DateTimeException;
import java.time.Instant;

public class Camp extends IdentifiedModel implements Hydratable<Camp.Data> {

    // ─── Properties ─── //

    private String name;
    @ModelReference(manager = AddressDataManager.class) private Address address;
    private int pendingAddressPk = -1;
    private TimeSlot timeSlot;

    // ─── Getters ─── //

    @TableDisplay(name = "Nom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 2)
    public String getName() {
        return this.name;
    }

    public Address getAddress() {
        return this.address;
    }

    @TableDisplay(name = "Début et fin", order = 4)
    public TimeSlot getTimeSlot() {
        return this.timeSlot;
    }

    // ─── Special getters ─── //

    @TableDisplay(name = "#& (adresse)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignement.CENTER), order = 3)
    public int getAddressId() {
        return this.address.getId();
    }

    // ─── Setters ─── //

    public void setName(String name) throws ModelException {
        if (name == null || name.isBlank()) {
            throw new ModelException("Le nom d'un stage ne peut pas être vide");
        }

        this.name = name.strip();
    }

    public void setAddress(Address address) throws ModelException {
        if (address == null) {
            throw new ModelException("L'adresse est requise pour un stage (valeur null reçue)");
        }

        this.setAddressFromPk(address.getId());
    }

    public void setTimeSlot(TimeSlot timeSlot) throws ModelException {
        if (timeSlot == null) {
            throw new ModelException("La période de temps d'un stage ne peut pas être vide");
        }

        this.timeSlot = timeSlot;
    }

    // ─── Special setters ─── //

    public void setAddressFromPk(int addressId) throws ModelException {
        Address address;
        try {
            address = DataManagers.initAndGet(AddressDataManager.class).getAddress(addressId);
        } catch (LoadDataManagerDataException e) {
            throw new ModelException("Impossible de vérifier l'identifiant d'adresse '%d' (les adresses n'ont pas pu être chargées dans l'application)".formatted(addressId));
        }

        if (address == null) {
            throw new ModelException("Aucune des adresses enregistrées ne porte l'identifiant '%d'".formatted(addressId));
        }

        this.address = address;
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
        private String timeSlotStart;
        private String timeSlotEnd;

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

        public String getTimeSlotStart() {
            return this.timeSlotStart;
        }

        public String getTimeSlotEnd() {
            return this.timeSlotEnd;
        }

        // ─── Special getters ─── //

        public TimeSlot getTimeSlot() {
            return new TimeSlot(this.timeSlotStart, this.timeSlotEnd);
        }

        // ─── Setters ─── //

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

        public void setName(String name) throws ModelException {
            if (name == null || name.isBlank()) {
                throw new ModelException("Le nom d'un stage ne peut pas être vide");
            }

            this.name = name.strip();
        }

        public void setTimeSlotStart(String timeSlotStart) throws ModelException {
            if (timeSlotStart == null || timeSlotStart.isBlank()) {
                throw new ModelException("La date de début d'un stage ne peut pas être vide");
            }

            timeSlotStart = timeSlotStart.strip();

            try {
                Instant.parse(timeSlotStart);
            } catch (DateTimeException e) {
                throw new ModelException("La date de début n'est pas un Instant valide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
            }

            this.timeSlotStart = timeSlotStart;
        }

        public void setTimeSlotEnd(String timeSlotEnd) throws ModelException {
            if (timeSlotEnd == null || timeSlotEnd.isBlank()) {
                throw new ModelException("La date de fin d'un stage ne peut pas être vide");
            }

            timeSlotEnd = timeSlotEnd.strip();

            try {
                Instant.parse(timeSlotEnd);
            } catch (DateTimeException e) {
                throw new ModelException("La date de fin n'est pas un Instant valide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
            }

            this.timeSlotEnd = timeSlotEnd;
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
