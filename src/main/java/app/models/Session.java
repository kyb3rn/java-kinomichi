package app.models;

import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
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

public class Session extends IdentifiedModel implements Hydratable<Session.Data>, CampScheduledItem {

    // ─── Properties ─── //

    @ModelReference(manager = CampDataManager.class) private Camp camp;
    private int pendingCampPk = -1;
    private String label;
    private TimeSlot timeSlot;

    // ─── Getters ─── //

    public Camp getCamp() {
        return this.camp;
    }

    public String getLabel() {
        return this.label;
    }

    public TimeSlot getTimeSlot() {
        return this.timeSlot;
    }

    // ─── Special getters ─── //

    public int getCampId() throws ModelException {
        if (this.camp == null) {
            throw new ModelException("Le stage de référence de la session est nul");
        }

        return this.camp.getId();
    }

    // ─── Setters ─── //

    public void setCamp(Camp camp) throws ModelException {
        if (camp == null) {
            throw new ModelVerificationException("Le stage de référence d'une session ne peut pas être nul");
        }

        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(camp.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant du stage '%d'".formatted(camp.getId()), e);
        }
    }

    public void setLabel(String label) throws ModelException {
        this.label = verifyLabel(label);
    }

    public void setTimeSlot(TimeSlot timeSlot) throws ModelException {
        if (timeSlot == null) {
            throw new ModelVerificationException("Le créneau horaire d'une session ne peut pas être nul");
        }

        CampScheduledItem.validateTimeSlotWithinCampBounds(this.camp, timeSlot);

        this.timeSlot = timeSlot;
    }

    // ─── Special setters ─── //

    public void setCampFromPk(int campId) throws DataManagerException {
        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(campId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant du stage '%d' (les stages n'ont pas pu être chargés dans l'application)".formatted(campId), e);
        }
    }

    public void setCampFromPk(String campIdAsString) throws DataManagerException, ModelException {
        this.setCampFromPk(Camp.verifyId(campIdAsString));
    }

    // ─── Utility methods ─── //

    public static String verifyLabel(String label) throws ModelException {
        return verifyNotNullOrEmpty(label, "Le label d'une session ne peut pas être vide ou nul");
    }

    public static Instant verifyTimeSlotStart(String timeSlotStart) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotStart);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de début d'une session ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de début a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    public static Instant verifyTimeSlotEnd(String timeSlotEnd) throws ModelException {
        try {
            return Validators.validateInstant(timeSlotEnd);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date de fin d'une session ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date de fin a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Session clone() {
        Session clone = new Session();

        try {
            clone.setId(this.getId());
        } catch (ModelException _) {
            // Impossible case scenario (for IdentifiedModel at least)
        }

        clone.camp = this.camp;
        clone.pendingCampPk = this.pendingCampPk;
        clone.label = this.label;
        clone.timeSlot = this.timeSlot;

        return clone;
    }

    @Override
    public String toString() {
        return "#%d %s (%s)".formatted(this.getId(), this.label, this.timeSlot);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.camp != null && this.label != null && this.timeSlot != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingCampPk = dataObject.getCampId();
        this.setLabel(dataObject.getLabel());
        this.setTimeSlot(dataObject.getTimeSlot());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int campId = -1;
        private String label;
        private Instant timeSlotStart;
        private Instant timeSlotEnd;

        // ─── Constructors ─── //

        public Data() {}

        public Data(Session session) throws ModelException {
            this.setId(session.getId());
            this.setCampId(session.getCamp().getId());
            this.setLabel(session.getLabel());
            this.setTimeSlotStart(session.getTimeSlot().getFormattedStart());
            this.setTimeSlotEnd(session.getTimeSlot().getFormattedEnd());
        }

        // ─── Getters ─── //

        public int getCampId() {
            return this.campId;
        }

        public String getLabel() {
            return this.label;
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

        public void setCampId(String campId) throws ModelException {
            this.campId = verifyId(campId);
        }

        public void setCampId(int campId) throws ModelException {
            this.setCampId(String.valueOf(campId));
        }

        public void setLabel(String label) throws ModelException {
            this.label = verifyLabel(label);
        }

        public void setTimeSlotStart(String timeSlotStart) throws ModelException {
            Instant timeSlotStartInstant = verifyTimeSlotStart(timeSlotStart);

            if (this.timeSlotEnd != null && (timeSlotStartInstant.isAfter(this.timeSlotEnd) || timeSlotStartInstant.equals(this.timeSlotEnd))) {
                throw new ModelVerificationException("La date de début d'une session doit être strictement antérieure à sa date de fin");
            }

            this.timeSlotStart = timeSlotStartInstant;
        }

        public void setTimeSlotEnd(String timeSlotEnd) throws ModelException {
            Instant timeSlotEndInstant = verifyTimeSlotEnd(timeSlotEnd);

            if (this.timeSlotStart != null && (timeSlotEndInstant.isBefore(this.timeSlotStart) || timeSlotEndInstant.equals(this.timeSlotStart))) {
                throw new ModelVerificationException("La date de fin d'une session doit être strictement postérieure à sa date de début");
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
            if (!obj.has("campId")) {
                throw new StringParserException("Le champ 'campId' est manquant");
            }
            if (!obj.has("label")) {
                throw new StringParserException("Le champ 'label' est manquant");
            }
            if (!obj.has("timeSlotStart")) {
                throw new StringParserException("Le champ 'timeSlotStart' est manquant");
            }
            if (!obj.has("timeSlotEnd")) {
                throw new StringParserException("Le champ 'timeSlotEnd' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setCampId(obj.get("campId").getAsString());
                this.setLabel(obj.get("label").getAsString());
                this.setTimeSlotStart(obj.get("timeSlotStart").getAsString());
                this.setTimeSlotEnd(obj.get("timeSlotEnd").getAsString());
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
