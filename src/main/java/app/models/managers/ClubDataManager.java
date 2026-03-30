package app.models.managers;

import app.models.Club;
import app.models.Model;
import app.models.ModelException;
import com.google.gson.*;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.converters.readers.JsonReader;
import utils.data_management.converters.writers.DataWriter;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ClubDataManager extends DataManager<ClubDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Club> clubs = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Club> getClubs() {
        return this.clubs;
    }

    public Club getClub(Integer id) {
        return this.clubs.get(id);
    }

    // ─── Utility methods ─── //

    public void addClub(Club club) throws ModelException {
        if (!club.isValid()) {
            throw new ModelException("L'objet Club qui a voulu être ajouté n'est pas valide");
        }

        if (this.clubs.containsKey(club.getId())) {
            throw new ModelException("Un club portant l'identifiant '%d' existe déjà".formatted(club.getId()));
        }

        this.clubs.put(club.getId(), club);

        this.unsavedChanges = true;

        try {
            this.export();
            this.unsavedChanges = false;
        } catch (DataManagerException ignored) {
        }
    }

    public Club addClub(Club.Data clubData) throws ModelException {
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
    public void init() throws LoadDataManagerDataException {
        if (!this.isInitialized()) {
            DataWriter<Data> dataWriter = new DataWriter<>();
            JsonReader<Data> csvReader = new JsonReader<>(dataWriter);

            Data modelData = new Data();
            String filePath = this.getFilePath().toString();
            try {
                csvReader.readFile(filePath, modelData);
            } catch (Exception e) {
                throw new LoadDataManagerDataException("Les données du manager '%s' n'ont pas pu être lues dans le fichier '%s'".formatted(this.getClass().getSimpleName(), filePath), e);
            }

            try {
                dataWriter.write(modelData, this);
            } catch (Exception e) {
                throw new LoadDataManagerDataException("Les données lues du manager '%s' dans le fichier '%s' n'ont pas pu être enregistrées dans le manager '%s'".formatted(this.getClass().getSimpleName(), filePath, e));
            }
        }
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
    protected void addResolvedModel(Model model) throws ModelException {
        if (!(model instanceof Club club)) {
            throw new ModelException("Le manager '%s' attend un objet de type Club, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.clubs.put(club.getId(), club);
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
