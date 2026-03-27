package app.data_management.managers;

import app.models.Club;
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
import java.util.HashMap;
import java.util.List;

public class ClubDataManager extends DataManager<ClubDataManager.Data> {

    private final HashMap<Integer, Club> clubs = new HashMap<>();

    private ClubDataManager() throws LoadDataManagerDataException {
        DataWriter<ClubDataManager.Data> dataWriter = new DataWriter<>();
        JsonReader<ClubDataManager.Data> csvReader = new JsonReader<>(dataWriter);

        ClubDataManager.Data modelData = new ClubDataManager.Data();
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

    public HashMap<Integer, Club> getClubs() {
        return this.clubs;
    }

    public Club getClub(Integer id) {
        return this.clubs.get(id);
    }

    public void addClub(Club club) throws ModelException {
        if (!club.isValid()) {
            throw new ModelException("L'objet Club qui a voulu être ajouté n'est pas valide");
        }

        if (this.clubs.containsKey(club.getId())) {
            throw new ModelException("Un club portant l'identifiant '%d' existe déjà".formatted(club.getId()));
        }

        this.clubs.put(club.getId(), club);
    }

    public Club addClub(Club.Data clubData) throws ModelException {
        Club club = new Club();
        club.hydrate(clubData);

        int maxId = this.clubs.values().stream().mapToInt(Club::getId).max().orElse(0);
        club.setId(maxId + 1);

        this.addClub(club);

        return club;
    }

    @Override
    public void export(FileType fileType) throws DataManagerException, ModelException {
        ClubDataManager.Data data = new Data(this);
        super.export(fileType, data);
    }

    @Override
    public int count() {
        return this.clubs.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        for (Club.Data clubData : dataObject.clubs) {
            this.addClub(clubData);
        }
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    public static class Data implements CustomSerializable, JsonConvertible {

        /** Properties **/

        private final List<Club.Data> clubs = new ArrayList<>();

        public Data(ClubDataManager clubManager) throws ModelException {
            for (Club club : clubManager.getClubs().values()) {
                this.clubs.add(club.dehydrate());
            }
        }

        public Data() {}

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
