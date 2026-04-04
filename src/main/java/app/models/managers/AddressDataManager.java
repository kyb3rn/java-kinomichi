package app.models.managers;

import app.models.Address;
import app.models.Model;
import app.models.ModelException;
import app.models.NoResultForPrimaryKeyException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.List;

public class AddressDataManager extends DataManager<AddressDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Address> addresses = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Address> getAddresses() {
        return this.addresses;
    }

    // ─── Utility methods ─── //

    public Address getAddress(Integer id) {
        return this.addresses.get(id);
    }

    public Address getAddressWithExceptions(int id) throws NoResultForPrimaryKeyException {
        Address address = this.getAddress(id);

        if (address == null) {
            throw new NoResultForPrimaryKeyException("Aucune des adresses enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return address;
    }

    public void addAddress(Address address) throws ModelException, DataManagerException {
        if (!address.isValid()) {
            throw new ModelException("L'adresse qui a voulue être ajoutée n'est pas valide");
        }

        if (this.addresses.containsKey(address.getId())) {
            throw new DataManagerException("Une adresse portant l'identifiant '%d' existe déjà".formatted(address.getId()));
        }

        this.addresses.put(address.getId(), address);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public Address addAddress(Address.Data addressData) throws ModelException, DataManagerException {
        Address address = new Address();
        address.hydrate(addressData);
        DataManagers.resolveModelReferences(address);

        int maxId = this.addresses.values().stream().mapToInt(Address::getId).max().orElse(0);
        address.setId(maxId + 1);

        this.addAddress(address);

        return address;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Address> getModels() {
        return this.addresses.values();
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
        return this.addresses.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.pendingModels = new ArrayList<>();

        for (Address.Data addressData : dataObject.addresses) {
            Address address = new Address();
            address.hydrate(addressData);
            this.pendingModels.add(address);
        }
    }

    @Override
    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {
        if (!(model instanceof Address address)) {
            throw new ModelException("Le manager '%s' attend un objet de type Address, mais a reçu '%s'".formatted(this.getClass().getSimpleName(), model.getClass().getSimpleName()));
        }

        this.addAddress(address);
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Address.Data> addresses = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(AddressDataManager addressManager) throws ModelException {
            for (Address address : addressManager.getAddresses().values()) {
                this.addresses.add(address.dehydrate());
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

            if (!obj.has("addresses")) {
                throw new StringParserException("Le champ 'addresses' est manquant");
            } else if (!obj.get("addresses").isJsonArray()) {
                throw new StringParserException("Le champ 'addresses' doit être un tableau");
            }

            JsonArray addressesArray = obj.getAsJsonArray("addresses");
            for (JsonElement element : addressesArray) {
                Address.Data addressData = new Address.Data();
                addressData.parseJson(element.toString());
                this.addresses.add(addressData);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
