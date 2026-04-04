package app.models.managers;

import app.models.Club;
import app.models.Model;
import app.models.ModelException;
import app.models.NoResultForPrimaryKeyException;
import com.google.gson.*;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class ClubDataManager extends DataManager<ClubDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Club> clubs = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Club> getClubs() {
        return this.clubs;
    }

    // ─── Utility methods ─── //

    public Club getClub(Integer id) {
        return this.clubs.get(id);
    }

    public Club getClubWithExceptions(int id) throws NoResultForPrimaryKeyException {
        Club club = this.getClub(id);

        if (club == null) {
            throw new NoResultForPrimaryKeyException("Aucun des clubs enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return club;
    }

    public void addClub(Club club) throws ModelException, DataManagerException {
        if (!club.isValid()) {
            throw new ModelException("Le club qui a voulu être ajouté n'est pas valide");
        }

        if (this.clubs.containsKey(club.getId())) {
            throw new DataManagerException("Un club portant l'identifiant '%d' existe déjà".formatted(club.getId()));
        }

        this.clubs.put(club.getId(), club);

        if (this.isInitialized()) {
            this.unsavedChanges = true;

            try {
                this.export();
            } catch (DataManagerException _) {
            }
        }
    }

    public Club addClub(Club.Data clubData) throws ModelException, DataManagerException {
        Club club = new Club();
        club.hydrate(clubData);
        DataManagers.resolveModelReferences(club);

        int maxId = this.clubs.values().stream().mapToInt(Club::getId).max().orElse(0);
        club.setId(maxId + 1);

        this.addClub(club);

        return club;
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
        if (this.isInitialized()) {
            Data data = new Data(this);
            super.export(data);
            this.unsavedChanges = false;
        }
    }

    @Override
    public void export(FileType fileType) throws DataManagerException, ModelException {
        if (this.isInitialized()) {
            Data data = new Data(this);
            super.export(fileType, data);
        }
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
            for (Club club : clubManager.getClubs().values()) {
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
