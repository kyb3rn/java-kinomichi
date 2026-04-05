package app.models;

import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.DinnerDataManager;
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

public class DinnerReservation extends IdentifiedModel implements Hydratable<DinnerReservation.Data>, ChargingElement {

    // ─── Properties ─── //

    @ModelReference(manager = PersonDataManager.class) private Person person;
    private int pendingPersonPk = -1;
    @ModelReference(manager = DinnerDataManager.class) private Dinner dinner;
    private int pendingDinnerPk = -1;
    private Instant cancellationDatetime;

    // ─── Getters ─── //

    public Person getPerson() {
        return this.person;
    }

    public Dinner getDinner() {
        return this.dinner;
    }

    public Instant getCancellationDatetime() {
        return this.cancellationDatetime;
    }

    // ─── Special getters ─── //

    public int getPersonId() throws ModelException {
        if (this.person == null) {
            throw new ModelException("La personne de référence de l'réservation de repas est nulle");
        }

        return this.person.getId();
    }

    public int getDinnerId() throws ModelException {
        if (this.dinner == null) {
            throw new ModelException("Le repas de référence de l'réservation de repas est nul");
        }

        return this.dinner.getId();
    }

    // ─── Setters ─── //

    public void setPerson(Person person) throws ModelException {
        if (person == null) {
            throw new ModelVerificationException("La personne de référence d'une réservation de repas ne peut pas être nulle");
        }

        try {
            this.person = DataManagers.get(PersonDataManager.class).getPersonWithExceptions(person.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de personne '%d'".formatted(person.getId()), e);
        }
    }

    public void setDinner(Dinner dinner) throws ModelException {
        if (dinner == null) {
            throw new ModelVerificationException("Le repas de référence d'une réservation de repas ne peut pas être nul");
        }

        try {
            this.dinner = DataManagers.get(DinnerDataManager.class).getDinnerWithExceptions(dinner.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant de repas '%d'".formatted(dinner.getId()), e);
        }
    }

    public void setCancellationDatetime(Instant cancellationDatetime) {
        this.cancellationDatetime = cancellationDatetime;
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

    public void setDinnerFromPk(int dinnerId) throws DataManagerException {
        try {
            this.dinner = DataManagers.get(DinnerDataManager.class).getDinnerWithExceptions(dinnerId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new DataManagerException("Impossible de vérifier l'identifiant de repas '%d' (les repas n'ont pas pu être chargés dans l'application)".formatted(dinnerId), e);
        }
    }

    public void setPersonFromPk(String personIdAsString) throws DataManagerException, ModelException {
        this.setPersonFromPk(Person.verifyId(personIdAsString));
    }

    public void setDinnerFromPk(String dinnerIdAsString) throws DataManagerException, ModelException {
        this.setDinnerFromPk(Dinner.verifyId(dinnerIdAsString));
    }

    // ─── Utility methods ─── //

    public static Instant verifyCancellationDatetime(String cancellationDatetime) throws ModelException {
        try {
            return Validators.validateInstant(cancellationDatetime);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("La date d'annulation d'une réservation de repas ne peut pas être vide ou nulle", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("La date d'annulation a un format de date invalide (attendu: yyyy-MM-ddTHH:mm:ssZ)", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        String personId = this.person != null ? "#" + this.person.getId() : "null";
        String dinnerId = this.dinner != null ? "#" + this.dinner.getId() : "null";
        String cancellation = this.cancellationDatetime != null ? " (annulée: %s)".formatted(this.cancellationDatetime) : "";
        return "#%d %s %s%s".formatted(this.getId(), personId, dinnerId, cancellation);
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0 && this.person != null && this.dinner != null;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingPersonPk = dataObject.getPersonId();
        this.pendingDinnerPk = dataObject.getDinnerId();
        this.cancellationDatetime = dataObject.getCancellationDatetime();
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new DinnerReservation.Data(this);
    }

    @Override
    public double getBasePrice() {
        Dinner dinner = this.getDinner();
        return dinner != null ? dinner.getPrice().getAmount() : 0;
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ── //

        private int personId = -1;
        private int dinnerId = -1;
        private Instant cancellationDatetime;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(DinnerReservation dinnerReservation) throws ModelException {
            this.setId(dinnerReservation.getId());
            this.setPersonId(dinnerReservation.getPersonId());
            this.setDinnerId(dinnerReservation.getDinnerId());
            this.cancellationDatetime = dinnerReservation.getCancellationDatetime();
        }

        // ─── Getters ─── //

        public int getPersonId() {
            return this.personId;
        }

        public int getDinnerId() {
            return this.dinnerId;
        }

        public Instant getCancellationDatetime() {
            return this.cancellationDatetime;
        }

        // ─── Setters ── //

        public void setPersonId(String personId) throws ModelException {
            this.personId = IdentifiedModel.verifyId(personId);
        }

        public void setPersonId(int personId) throws ModelException {
            this.setPersonId(String.valueOf(personId));
        }

        public void setDinnerId(String dinnerId) throws ModelException {
            this.dinnerId = IdentifiedModel.verifyId(dinnerId);
        }

        public void setDinnerId(int dinnerId) throws ModelException {
            this.setDinnerId(String.valueOf(dinnerId));
        }

        public void setCancellationDatetime(String cancellationDatetime) throws ModelException {
            if (cancellationDatetime == null || cancellationDatetime.isBlank()) {
                this.cancellationDatetime = null;
                return;
            }

            this.cancellationDatetime = verifyCancellationDatetime(cancellationDatetime);
        }

        // ─── Overrides & inheritance ── //

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
            if (!obj.has("dinnerId")) {
                throw new StringParserException("Le champ 'dinnerId' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setPersonId(obj.get("personId").getAsString());
                this.setDinnerId(obj.get("dinnerId").getAsString());

                if (obj.has("cancellationDatetime") && !obj.get("cancellationDatetime").isJsonNull()) {
                    this.setCancellationDatetime(obj.get("cancellationDatetime").getAsString());
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
