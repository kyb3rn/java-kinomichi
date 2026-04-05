package app.models.managers;

import app.models.*;
import com.google.gson.*;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;

import java.util.Collection;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class CampDataManager extends DataManager<CampDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Camp> camps = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Camp> getCamps() {
        return Collections.unmodifiableSortedMap(this.camps);
    }

    // ─── Utility methods ─── //

    public Camp getCamp(Integer id) throws ModelException {
        if (id == null) {
            throw new NoResultForPrimaryKeyException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        return this.camps.get(id);
    }

    public Camp getCampWithExceptions(int id) throws ModelException {
        Camp camp = this.getCamp(id);

        if (camp == null) {
            throw new NoResultForPrimaryKeyException("Aucun des stages enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return camp;
    }

    public void addCamp(Camp camp) throws ModelException, DataManagerException {
        if (camp == null) {
            throw new ModelException("Le stage à ajouter ne peut pas être nul");
        }

        if (!camp.isValid()) {
            throw new ModelException("Le stage à ajouter n'est pas valide");
        }

        this.applyAutoIncrementIfPossible(camp);

        if (this.camps.containsKey(camp.getId())) {
            throw new DataManagerException("Un stage portant l'identifiant '%d' existe déjà".formatted(camp.getId()));
        }

        this.camps.put(camp.getId(), camp);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Camp addCamp(Camp.Data campData) throws ModelException, DataManagerException {
        if (campData == null) {
            throw new ModelException("Le stage à ajouter ne peut pas être nul");
        }

        Camp camp = new Camp();
        camp.hydrate(campData);
        DataManagers.resolveModelReferences(camp);

        this.addCamp(camp);

        return camp;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Camp> getModels() {
        return this.camps.values();
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
        return this.camps.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Camp.Data campData : dataObject.camps) {
            Camp camp = new Camp();
            camp.hydrate(campData);
            this.pendingModels.add(camp);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Camp camp)) {
            throw new ModelException("Le manager '%s' attend un objet de type Camp, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addCamp(camp);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Camp.Data> camps = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(CampDataManager campDataManager) throws ModelException {
            for (Camp camp : campDataManager.getModels()) {
                this.camps.add(camp.dehydrate());
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

            if (!obj.has("camps")) {
                throw new StringParserException("Le champ 'camps' est manquant");
            } else if (!obj.get("camps").isJsonArray()) {
                throw new StringParserException("Le champ 'camps' doit être un tableau");
            }

            JsonArray campsArray = obj.getAsJsonArray("camps");
            for (JsonElement element : campsArray) {
                Camp.Data campData = new Camp.Data();
                campData.parseJson(element.toString());
                this.camps.add(campData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
