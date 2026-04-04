package app.models.managers;

import app.models.CampDiscount;
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

public class CampDiscountDataManager extends DataManager<CampDiscountDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, CampDiscount> campsDiscounts = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, CampDiscount> getCampsDiscounts() {
        return this.campsDiscounts;
    }

    // ─── Utility methods ─── //

    public CampDiscount getCampDiscount(Integer id) {
        return this.campsDiscounts.get(id);
    }

    public CampDiscount getCampDiscountWithExceptions(int id) throws NoResultForPrimaryKeyException {
        CampDiscount campDiscount = this.getCampDiscount(id);

        if (campDiscount == null) {
            throw new NoResultForPrimaryKeyException("Aucune des réductions enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return campDiscount;
    }

    public void addCampDiscount(CampDiscount campDiscount) throws ModelException, DataManagerException {
        if (!campDiscount.isValid()) {
            throw new ModelException("La réduction qui a voulu être ajoutée n'est pas valide");
        }

        if (this.campsDiscounts.containsKey(campDiscount.getId())) {
            throw new DataManagerException("Une réduction portant l'identifiant '%d' existe déjà".formatted(campDiscount.getId()));
        }

        this.campsDiscounts.put(campDiscount.getId(), campDiscount);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public CampDiscount addCampDiscount(CampDiscount.Data campDiscountData) throws ModelException, DataManagerException {
        CampDiscount campDiscount = new CampDiscount();
        campDiscount.hydrate(campDiscountData);
        DataManagers.resolveModelReferences(campDiscount);

        int maxId = this.campsDiscounts.values().stream().mapToInt(CampDiscount::getId).max().orElse(0);
        campDiscount.setId(maxId + 1);

        this.addCampDiscount(campDiscount);

        return campDiscount;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<CampDiscount> getModels() {
        return this.campsDiscounts.values();
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
        return this.campsDiscounts.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (CampDiscount.Data campDiscountData : dataObject.campsDiscounts) {
            CampDiscount campDiscount = new CampDiscount();
            campDiscount.hydrate(campDiscountData);
            this.pendingModels.add(campDiscount);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof CampDiscount campDiscount)) {
            throw new ModelException("Le manager '%s' attend un objet de type CampDiscount, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addCampDiscount(campDiscount);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<CampDiscount.Data> campsDiscounts = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(CampDiscountDataManager campDiscountDataManager) throws ModelException {
            for (CampDiscount campDiscount : campDiscountDataManager.getCampsDiscounts().values()) {
                this.campsDiscounts.add(campDiscount.dehydrate());
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

            if (!obj.has("campsDiscounts")) {
                throw new StringParserException("Le champ 'campsDiscounts' est manquant");
            } else if (!obj.get("campsDiscounts").isJsonArray()) {
                throw new StringParserException("Le champ 'campsDiscounts' doit être un tableau");
            }

            JsonArray campsDiscountsArray = obj.getAsJsonArray("campsDiscounts");
            for (JsonElement element : campsDiscountsArray) {
                CampDiscount.Data campDiscountData = new CampDiscount.Data();
                campDiscountData.parseJson(element.toString());
                this.campsDiscounts.add(campDiscountData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
