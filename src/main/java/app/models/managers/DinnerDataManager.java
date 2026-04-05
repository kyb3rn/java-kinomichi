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

public class DinnerDataManager extends DataManager<DinnerDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Dinner> dinners = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Dinner> getDinners() {
        return Collections.unmodifiableSortedMap(this.dinners);
    }

    // ─── Utility methods ─── //

    public Dinner getDinner(Integer id) throws ModelException {
        if (id == null) {
            throw new NoResultForPrimaryKeyException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        return this.dinners.get(id);
    }

    public Dinner getDinnerWithExceptions(int id) throws ModelException {
        Dinner dinner = this.getDinner(id);

        if (dinner == null) {
            throw new NoResultForPrimaryKeyException("Aucun des repas enregistrés ne porte l'identifiant '%d'".formatted(id));
        }

        return dinner;
    }

    public void addDinner(Dinner dinner) throws ModelException, DataManagerException {
        if (dinner == null) {
            throw new ModelException("Le repas à ajouter ne peut pas être nul");
        }

        if (!dinner.isValid()) {
            throw new ModelException("Le repas à ajouter n'est pas valide");
        }

        this.applyAutoIncrementIfPossible(dinner);

        if (this.dinners.containsKey(dinner.getId())) {
            throw new DataManagerException("Un repas portant l'identifiant '%d' existe déjà".formatted(dinner.getId()));
        }

        this.dinners.put(dinner.getId(), dinner);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Dinner addDinner(Dinner.Data dinnerData) throws ModelException, DataManagerException {
        if (dinnerData == null) {
            throw new ModelException("Le repas à ajouter ne peut pas être nul");
        }

        Dinner dinner = new Dinner();
        dinner.hydrate(dinnerData);
        DataManagers.resolveModelReferences(dinner);

        this.addDinner(dinner);

        return dinner;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Dinner> getModels() {
        return this.dinners.values();
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
        return this.dinners.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Dinner.Data dinnerData : dataObject.dinners) {
            Dinner dinner = new Dinner();
            dinner.hydrate(dinnerData);
            this.pendingModels.add(dinner);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Dinner dinner)) {
            throw new ModelException("Le manager '%s' attend un objet de type Dinner, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addDinner(dinner);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Dinner.Data> dinners = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(DinnerDataManager dinnerDataManager) throws ModelException {
            for (Dinner dinner : dinnerDataManager.getModels()) {
                this.dinners.add(dinner.dehydrate());
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

            if (!obj.has("dinners")) {
                throw new StringParserException("Le champ 'dinners' est manquant");
            } else if (!obj.get("dinners").isJsonArray()) {
                throw new StringParserException("Le champ 'dinners' doit être un tableau");
            }

            JsonArray dinnersArray = obj.getAsJsonArray("dinners");
            for (JsonElement element : dinnersArray) {
                Dinner.Data dinnerData = new Dinner.Data();
                dinnerData.parseJson(element.toString());
                this.dinners.add(dinnerData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
