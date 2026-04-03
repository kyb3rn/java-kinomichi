package app.models.managers;

import app.models.Affiliation;
import app.models.Model;
import app.models.ModelException;
import app.models.NotResultForPrimaryKeyException;
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

public class AffiliationDataManager extends DataManager<AffiliationDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Affiliation> affiliations = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Affiliation> getAffiliations() {
        return this.affiliations;
    }

    // ─── Utility methods ─── //

    public Affiliation getAffiliation(Integer personId) {
        return this.affiliations.get(personId);
    }

    public Affiliation getAffiliationWithExceptions(int personId) throws NotResultForPrimaryKeyException {
        Affiliation affiliation = this.getAffiliation(personId);

        if (affiliation == null) {
            throw new NotResultForPrimaryKeyException("Aucun des affiliés enregistrés ne porte l'identifiant '%d'".formatted(personId));
        }

        return affiliation;
    }

    public void addAffiliation(Affiliation affiliation) throws ModelException, DataManagerException {
        if (!affiliation.isValid()) {
            throw new ModelException("L'affiliation qui a voulu être ajoutée n'est pas valide");
        }

        if (this.affiliations.containsKey(affiliation.getPerson().getId())) {
            throw new DataManagerException("Un affilié portant l'identifiant '%d' existe déjà".formatted(affiliation.getPersonId()));
        }

        this.affiliations.put(affiliation.getPersonId(), affiliation);

        if (this.isInitialized()) {
            this.unsavedChanges = true;

            try {
                this.export();
            } catch (DataManagerException _) {
            }
        }
    }

    public Affiliation addAffiliation(Affiliation.Data affiliationData) throws ModelException, DataManagerException {
        Affiliation affiliation = new Affiliation();
        affiliation.hydrate(affiliationData);
        DataManagers.resolveModelReferences(affiliation);

        this.addAffiliation(affiliation);

        return affiliation;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Affiliation> getModels() {
        return this.affiliations.values();
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
        return this.affiliations.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Affiliation.Data affiliationData : dataObject.affiliations) {
            Affiliation affiliation = new Affiliation();
            affiliation.hydrate(affiliationData);
            this.pendingModels.add(affiliation);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Affiliation affiliation)) {
            throw new ModelException("Le manager '%s' attend un objet de type Affiliation, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addAffiliation(affiliation);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Affiliation.Data> affiliations = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(AffiliationDataManager affiliationDataManager) throws ModelException {
            for (Affiliation affiliation : affiliationDataManager.getAffiliations().values()) {
                this.affiliations.add(affiliation.dehydrate());
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

            if (!obj.has("affiliations")) {
                throw new StringParserException("Le champ 'affiliations' est manquant");
            } else if (!obj.get("affiliations").isJsonArray()) {
                throw new StringParserException("Le champ 'affiliations' doit être un tableau");
            }

            JsonArray affiliationsArray = obj.getAsJsonArray("affiliations");
            for (JsonElement element : affiliationsArray) {
                Affiliation.Data affiliationData = new Affiliation.Data();
                affiliationData.parseJson(element.toString());
                this.affiliations.add(affiliationData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
