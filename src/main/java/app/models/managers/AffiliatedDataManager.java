package app.models.managers;

import app.models.Affiliated;
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
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class AffiliatedDataManager extends DataManager<AffiliatedDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Affiliated> affiliateds = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Affiliated> getAffiliateds() {
        return this.affiliateds;
    }

    public Affiliated getAffiliated(Integer id) {
        return this.affiliateds.get(id);
    }

    // ─── Utility methods ─── //

    public void addAffiliated(Affiliated affiliated) throws ModelException, DataManagerException {
        if (!affiliated.isValid()) {
            throw new ModelException("L'objet Affiliated qui a voulu être ajouté n'est pas valide");
        }

        if (this.affiliateds.containsKey(affiliated.getId())) {
            throw new DataManagerException("Un affilié portant l'identifiant '%d' existe déjà".formatted(affiliated.getId()));
        }

        this.affiliateds.put(affiliated.getId(), affiliated);

        if (this.isInitialized()) {
            this.unsavedChanges = true;

            try {
                this.export();
            } catch (DataManagerException _) {
            }
        }
    }

    public Affiliated addAffiliated(Affiliated.Data affiliatedData) throws ModelException, DataManagerException {
        Affiliated affiliated = new Affiliated();
        affiliated.hydrate(affiliatedData);
        DataManagers.resolveModelReferences(affiliated);

        int maxId = this.affiliateds.values().stream().mapToInt(Affiliated::getId).max().orElse(0);
        affiliated.setId(maxId + 1);

        this.addAffiliated(affiliated);

        return affiliated;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Affiliated> getModels() {
        return this.affiliateds.values();
    }

    @Override
    public void init() throws LoadDataManagerDataException {
        if (!this.isInitialized()) {
            DataWriter<Data> dataWriter = new DataWriter<>();
            JsonReader<Data> jsonReader = new JsonReader<>(dataWriter);

            Data modelData = new Data();
            String filePath = this.getFilePath().toString();
            try {
                jsonReader.readFile(filePath, modelData);
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
        return this.affiliateds.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Affiliated.Data affiliatedData : dataObject.affiliateds) {
            Affiliated affiliated = new Affiliated();
            affiliated.hydrate(affiliatedData);
            this.pendingModels.add(affiliated);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Affiliated affiliated)) {
            throw new ModelException("Le manager '%s' attend un objet de type Affiliated, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addAffiliated(affiliated);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Affiliated.Data> affiliateds = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(AffiliatedDataManager affiliatedDataManager) throws ModelException {
            for (Affiliated affiliated : affiliatedDataManager.getAffiliateds().values()) {
                this.affiliateds.add(affiliated.dehydrate());
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

            if (!obj.has("affiliateds")) {
                throw new StringParserException("Le champ 'affiliateds' est manquant");
            } else if (!obj.get("affiliateds").isJsonArray()) {
                throw new StringParserException("Le champ 'affiliateds' doit être un tableau");
            }

            JsonArray affiliatedsArray = obj.getAsJsonArray("affiliateds");
            for (JsonElement element : affiliatedsArray) {
                Affiliated.Data affiliatedData = new Affiliated.Data();
                affiliatedData.parseJson(element.toString());
                this.affiliateds.add(affiliatedData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
