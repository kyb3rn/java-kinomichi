package app.models;

import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.LodgingDataManager;
import app.models.managers.PersonDataManager;
import app.utils.tarification.ChargingElement;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;
import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.ParsingValidatorException;
import utils.helpers.validation.Validators;

import java.time.Instant;

public class LodgingReservation extends IdentifiedModel implements Hydratable<LodgingReservation.Data>, ChargingElement {

    // ─── Properties ─── //

    @ModelReference(manager = PersonDataManager.class) private Person person;
    private int pendingPersonPk = -1;
    @ModelReference(manager = LodgingDataManager.class) private Lodging lodging;
    private int pendingLodgingPk = -1;
    private Instant cancellationDatetime;
    private boolean singleRoomOption;

    // ─── Getters ─── //

    public Person getPerson() {
        return this.person;
    }

    public Lodging getLodging() {
        return this.lodging;
    }

    public Instant getCancellationDatetime() {
        return this.cancellationDatetime;
    }

    public boolean isSingleRoomOption() {
        return this.singleRoomOption;
    }

    // ─── Special getters ─── //

    public int getPersonId() throws ModelException {
        if (this.person == null) {
            throw new ModelException("La personne de référence de la réservation d'hébergement est nulle");
        }

        return this.person.getId();
    }

    public int getLodgingId() throws ModelException {
        if (this.lodging == null) {
            throw new ModelException("L'hébergement de référence de la réservation d'hébergement est nul");
        }

        return this.lodging.getId();
    }

    // ─── Setters ─── //

    public void setPerson(Person person) throws ModelException {
        if (person == null) {
            throw new ModelVerificationException("La personne de référence d'une réservation d'hébergement ne peut pas être nulle");
        }

        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(person.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de personne '%d'".formatted(person.getId()), e);
        }
    }

    public void setLodging(Lodging lodging) throws ModelException {
        if (lodging == null) {
            throw new ModelVerificationException("L'hébergement de référence d'une réservation d'hébergement ne peut pas être nul");
        }

        try {
            this.lodging = DataManagers.get(LodgingDataManager.class).getLodgingWithExceptions(lodging.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant d'hébergement '%d'".formatted(lodging.getId()), e);
        }
    }

    public void setCancellationDatetime(Instant cancellationDatetime) {
        this.cancellationDatetime = cancellationDatetime;
    }

    public void setSingleRoomOption(boolean singleRoomOption) {
        this.singleRoomOption = singleRoomOption;
    }

    // ─── Special setters ─── //

    public void setPersonFromPk(int personId) throws DataManagerException {
        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(personId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de personne '%d' (les personnes n'ont pas pu être chargées dans l'application)".formatted(personId), e);
        }
    }

    public void setLodgingFromPk(int lodgingId) throws DataManagerException {
        try {
            this.lodging = DataManagers.get(LodgingDataManager.class).getLodgingWithExceptions(lodgingId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant d'hébergement '%d' (les hébergements n'ont pas pu être chargés dans l'application)".formatted(lodgingId), e);
        }
    }

    public void setPersonFromPk(String personIdAsString) throws DataManagerException, ModelException {
        this.setPersonFromPk(Person.verifyId(personIdAsString));
    }

    public void setLodgingFromPk(String lodgingIdAsString) throws DataManagerException, ModelException {
        this.setLodgingFromPk(Lodging.verifyId(lodgingIdAsString));
    }

    // ─── Utility methods ─── //

    public static Instant verifyCancellationDatetime(String cancellationDatetime) throws ModelException {
        try {
            return Validators.validateInstant(cancellationDatetime);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date d'annulation d'une réservation d'hébergement ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date d'annulation a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String personId = this.person != null ? "#" + this.person.getId() : "null";
        String lodgingId = this.lodging != null ? "#" + this.lodging.getId() : "null";
        String cancellation = this.cancellationDatetime != null ? " (annulée: %s)".formatted(this.cancellationDatetime) : "";
        String roomOption = this.singleRoomOption ? " [individuelle]" : " [partagée]";
        return "#%d %s %s%s%s".formatted(this.getId(), personId, lodgingId, roomOption, cancellation);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.person != null && this.lodging != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingPersonPk = dataObject.getPersonId();
        this.pendingLodgingPk = dataObject.getLodgingId();
        this.cancellationDatetime = dataObject.getCancellationDatetime();
        this.singleRoomOption = dataObject.isSingleRoomOption();
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new LodgingReservation.Data(this);
    }

    @Override
    public double getBasePrice() {
        Lodging lodging = this.getLodging();
        if (lodging == null) {
            return 0;
        }
        return this.singleRoomOption ? lodging.getSingleRoomPrice().getAmount() : lodging.getSharedRoomPrice().getAmount();
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int personId = -1;
        private int lodgingId = -1;
        private Instant cancellationDatetime;
        private boolean singleRoomOption;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(LodgingReservation lodgingReservation) throws ModelException {
            this.setId(lodgingReservation.getId());
            this.setPersonId(lodgingReservation.getPersonId());
            this.setLodgingId(lodgingReservation.getLodgingId());
            this.cancellationDatetime = lodgingReservation.getCancellationDatetime();
            this.singleRoomOption = lodgingReservation.isSingleRoomOption();
        }

        // ─── Getters ─── //

        public int getPersonId() {
            return this.personId;
        }

        public int getLodgingId() {
            return this.lodgingId;
        }

        public Instant getCancellationDatetime() {
            return this.cancellationDatetime;
        }

        public boolean isSingleRoomOption() {
            return this.singleRoomOption;
        }

        // ─── Setters ─── //

        public void setPersonId(String personId) throws ModelException {
            this.personId = IdentifiedModel.verifyId(personId);
        }

        public void setPersonId(int personId) throws ModelException {
            this.setPersonId(String.valueOf(personId));
        }

        public void setLodgingId(String lodgingId) throws ModelException {
            this.lodgingId = IdentifiedModel.verifyId(lodgingId);
        }

        public void setLodgingId(int lodgingId) throws ModelException {
            this.setLodgingId(String.valueOf(lodgingId));
        }

        public void setCancellationDatetime(String cancellationDatetime) throws ModelException {
            if (cancellationDatetime == null || cancellationDatetime.isBlank()) {
                this.cancellationDatetime = null;
                return;
            }

            this.cancellationDatetime = verifyCancellationDatetime(cancellationDatetime);
        }

        public void setSingleRoomOption(boolean singleRoomOption) {
            this.singleRoomOption = singleRoomOption;
        }

        public void setSingleRoomOption(String singleRoomOption) {
            this.singleRoomOption = Boolean.parseBoolean(singleRoomOption);
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
            if (!obj.has("personId")) {
                throw new StringParserException("Le champ 'personId' est manquant");
            }
            if (!obj.has("lodgingId")) {
                throw new StringParserException("Le champ 'lodgingId' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setPersonId(obj.get("personId").getAsString());
                this.setLodgingId(obj.get("lodgingId").getAsString());

                if (obj.has("cancellationDatetime") && !obj.get("cancellationDatetime").isJsonNull()) {
                    this.setCancellationDatetime(obj.get("cancellationDatetime").getAsString());
                }

                if (obj.has("singleRoomOption")) {
                    this.setSingleRoomOption(obj.get("singleRoomOption").getAsBoolean());
                }
            } catch (ModelException e) {
                throw new ParserException(e);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                    .create()
                    .toJson(this);
        }

    }

}
