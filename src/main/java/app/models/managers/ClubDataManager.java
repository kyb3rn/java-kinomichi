package app.models.managers;

import app.models.*;
import com.google.gson.*;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClubDataManager extends DataManager<ClubDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Club> clubs = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Club> getClubs() {
        return Collections.unmodifiableSortedMap(this.clubs);
    }

    // ─── Utility methods ─── //

    public Club getClub(Integer id) throws ModelException {
        if (id == null) {
            throw new NoResultForPrimaryKeyException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        return this.clubs.get(id);
    }

    public Club getClubWithExceptions(int id) throws ModelException {
        Club club = this.getClub(id);

        if (club == null) {
            throw new NoResultForPrimaryKeyException("Aucun des clubs enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return club;
    }

    public void addClub(Club club) throws ModelException, DataManagerException {
        if (club == null) {
            throw new ModelException("Le club à ajouter ne peut pas être nul");
        }

        if (!club.isValid()) {
            throw new ModelException("Le club à ajouter n'est pas valide");
        }

        this.applyAutoIncrementIfPossible(club);

        if (this.clubs.containsKey(club.getId())) {
            throw new DataManagerException("Un club portant l'identifiant '%d' existe déjà".formatted(club.getId()));
        }

        this.clubs.put(club.getId(), club);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Club addClub(Club.Data clubData) throws ModelException, DataManagerException {
        if (clubData == null) {
            throw new ModelException("Le club à ajouter ne peut pas être nul");
        }

        Club club = new Club();
        club.hydrate(clubData);
        DataManagers.resolveModelReferences(club);

        this.addClub(club);

        return club;
    }

    public void updateClub(int clubId, Club modifiedClub) throws ModelException, DataManagerException {
        clubId = IdentifiedModel.verifyId(clubId);

        if (modifiedClub == null) {
            throw new ModelException("Le club modifié ne peut pas être nul");
        }

        if (!modifiedClub.isValid()) {
            throw new ModelException("Le club modifié reçu n'est pas valide");
        }

        Club clubToModify = this.getClubWithExceptions(clubId);

        if (clubToModify.getId() != modifiedClub.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        if (clubToModify.getAddressId() != modifiedClub.getAddressId()) {
            throw new ModelException("Modifier l'identifiant de référence de l'adresse d'un club n'est pas autorisé");
        }

        clubToModify.hydrate(modifiedClub.dehydrate());
        DataManagers.resolveModelReferences(clubToModify);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void deleteClub(int clubId) throws ModelException, DataManagerException {
        Club clubToDelete = this.getClubWithExceptions(clubId);

        if (DataManagers.get(AffiliationDataManager.class).isClubUsed(clubToDelete)) {
            throw new DeletingReferencedDataManagerDataException("Ce club est référencé par au moins une affiliation et est donc impossible à supprimer.");
        }

        this.clubs.remove(clubToDelete.getId());

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Club> getModels() {
        return this.clubs.values();
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
        return this.clubs.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Club.Data clubData : dataObject.clubs) {
            Club club = new Club();
            club.hydrate(clubData);
            this.pendingModels.add(club);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Club club)) {
            throw new ModelException("Le manager '%s' attend un objet de type Club, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addClub(club);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Club.Data> clubs = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(ClubDataManager clubManager) throws ModelException {
            for (Club club : clubManager.getModels()) {
                this.clubs.add(club.dehydrate());
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

            if (!obj.has("clubs")) {
                throw new StringParserException("Le champ 'clubs' est manquant");
            } else if (!obj.get("clubs").isJsonArray()) {
                throw new StringParserException("Le champ 'clubs' doit être un tableau");
            }

            JsonArray clubsArray = obj.getAsJsonArray("clubs");
            for (JsonElement element : clubsArray) {
                Club.Data clubData = new Club.Data();
                clubData.parseJson(element.toString());
                this.clubs.add(clubData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
