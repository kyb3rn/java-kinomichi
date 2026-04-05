package app.models.managers;

import app.models.*;
import com.google.gson.*;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class InvitationDataManager extends DataManager<InvitationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Invitation> invitations = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Invitation> getInvitations() {
        return Collections.unmodifiableSortedMap(this.invitations);
    }

    // ─── Utility methods ─── //

    public Invitation getInvitationWithExceptions(Integer id) throws DataManagerException, ModelException {
        if (id == null) {
            throw new DataManagerException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        Invitation camp = this.invitations.get(id);

        if (camp == null) {
            throw new NoResultForPrimaryKeyException("Aucune des invitations enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return camp;
    }

    public List<Invitation> getPersonInvitations(Person person) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.invitations.values().stream().filter(inv -> {
            try {
                return inv.getPersonId() == person.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public List<Invitation> getCampInvitations(Camp camp) throws DataManagerException {
        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.invitations.values().stream().filter(invitation -> {
            try {
                return invitation.getCampId() == camp.getId();
            } catch (ModelException e) {
                return false;
            }
        }).toList();
    }

    public void addInvitation(Invitation invitation) throws ModelException, DataManagerException {
        if (invitation == null) {
            throw new DataManagerException("L'invitation à ajouter ne peut pas être nulle");
        }

        this.applyAutoIncrementIfPossible(invitation);

        if (!invitation.isValid()) {
            throw new ModelException("L'invitation à ajouter n'est pas valide");
        }

        if (this.invitations.containsKey(invitation.getId())) {
            throw new DataManagerException("Une invitation portant l'identifiant '%d' existe déjà".formatted(invitation.getId()));
        }

        this.invitations.put(invitation.getId(), invitation);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Invitation addInvitation(Invitation.Data invitationData) throws ModelException, DataManagerException {
        if (invitationData == null) {
            throw new DataManagerException("L'invitation à ajouter ne peut pas être nulle");
        }

        Invitation invitation = new Invitation();
        invitation.hydrate(invitationData);
        DataManagers.resolveModelReferences(invitation);

        this.addInvitation(invitation);

        return invitation;
    }

    public void deleteInvitation(int invitationId) throws ModelException, DataManagerException {
        Invitation invitationToDelete = this.getInvitationWithExceptions(invitationId);

        Camp camp = invitationToDelete.getCamp();
        if (camp.getTimeSlot().getEnd().isBefore(Instant.now())) {
            throw new DataManagerException("Impossible de supprimer une invitation liée à un stage déjà terminé");
        }

        this.invitations.remove(invitationToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public boolean isPersonUsed(Person personToCheck) throws DataManagerException {
        if (personToCheck == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        return this.getModels().stream().anyMatch(invitation -> {
            try {
                return invitation.getPersonId() == personToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isCampUsed(Camp campToCheck) throws DataManagerException {
        if (campToCheck == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.getModels().stream().anyMatch(invitation -> {
            try {
                return invitation.getCampId() == campToCheck.getId();
            } catch (ModelException _) {
                return false;
            }
        });
    }

    public boolean isPersonInvited(Person person, Camp camp) throws DataManagerException {
        if (person == null) {
            throw new DataManagerException("La personne sur laquelle effectuer la recherche ne peut pas être nulle");
        }

        if (camp == null) {
            throw new DataManagerException("Le stage sur lequel effectuer la recherche ne peut pas être nul");
        }

        return this.getPersonInvitations(person).stream().anyMatch(invitation -> {
            try {
                return invitation.getCampId() == camp.getId();
            } catch (ModelException e) {
                return false;
            }
        });
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Invitation> getModels() {
        return this.invitations.values();
    }

    @Override
    public void init() throws LoadDataManagerDataException {
        this.defaultJsonInit(new Data());
    }

    @Override
    public void export() throws DataManagerException, ModelException {
        Data data = new Data(this);
        super.export(data);
    }

    @Override
    public void export(FileType fileType) throws DataManagerException, ModelException {
        Data data = new Data(this);
        super.export(fileType, data);
    }

    @Override
    public int count() {
        return this.invitations.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Invitation.Data invitationData : dataObject.invitations) {
            Invitation invitation = new Invitation();
            invitation.hydrate(invitationData);
            this.pendingModels.add(invitation);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Invitation invitation)) {
            throw new ModelException("Le manager '%s' attend un objet de type Invitation, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addInvitation(invitation);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Invitation.Data> invitations = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(InvitationDataManager invitationDataManager) throws ModelException {
            for (Invitation invitation : invitationDataManager.getModels()) {
                this.invitations.add(invitation.dehydrate());
            }
        }

        public Data() {}

        // ─── Overrides & inheritance ─── //

        @Override
        public void parseJson(String json) throws ParserException {
            JsonObject obj;
            try {
                JsonElement parsed = JsonParser.parseString(json);

                if (parsed.isJsonNull()) {
                    return;
                }

                obj = parsed.getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                throw new StringParserException("Le JSON reçu n'est pas un objet valide (%s)".formatted(e.getMessage()), e);
            }

            if (!obj.has("invitations")) {
                throw new StringParserException("Le champ 'invitations' est manquant");
            } else if (!obj.get("invitations").isJsonArray()) {
                throw new StringParserException("Le champ 'invitations' doit être un tableau");
            }

            JsonArray invitationsArray = obj.getAsJsonArray("invitations");
            for (JsonElement element : invitationsArray) {
                Invitation.Data invitationData = new Invitation.Data();
                invitationData.parseJson(element.toString());
                this.invitations.add(invitationData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
