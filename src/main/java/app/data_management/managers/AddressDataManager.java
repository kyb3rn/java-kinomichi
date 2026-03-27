package app.data_management.managers;

import app.models.Address;
import app.models.ModelException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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

public class AddressDataManager extends DataManager<AddressDataManager.Data> {

    private final HashMap<Integer, Address> addresses = new HashMap<>();

    private AddressDataManager() throws LoadDataManagerDataException {
        DataWriter<AddressDataManager.Data> dataWriter = new DataWriter<>();
        JsonReader<AddressDataManager.Data> csvReader = new JsonReader<>(dataWriter);

        AddressDataManager.Data modelData = new AddressDataManager.Data();
        String filePath = this.getFilePath().toString();
        try {
            csvReader.readFile(filePath, modelData);
        } catch (Exception e) {
            throw new LoadDataManagerDataException("Les données du manager '%s' n'ont pas pu être lues dans le fichier '%s'".formatted(this.getClass().getSimpleName(), filePath));
        }

        try {
            dataWriter.write(modelData, this);
        } catch (Exception e) {
            throw new LoadDataManagerDataException("Les données lues du manager '%s' dans le fichier '%s' n'ont pas pu être enregistrées dans le manager '%s'".formatted(this.getClass().getSimpleName(), filePath, e));
        }
    }

    public HashMap<Integer, Address> getAddresses() {
        return this.addresses;
    }

    public Address getAddress(Integer id) {
        return this.addresses.get(id);
    }

    public void addAddress(Address address) throws ModelException {
        if (!address.isValid()) {
            throw new ModelException("L'objet Address qui a voulu être ajouté n'est pas valide");
        }

        if (this.addresses.containsKey(address.getId())) {
            throw new ModelException("Une adresse portant l'identifiant '%d' existe déjà".formatted(address.getId()));
        }

        this.addresses.put(address.getId(), address);
    }

    public Address addAddress(Address.Data addressData) throws ModelException {
        Address address = new Address();
        address.hydrate(addressData);

        int maxId = this.addresses.values().stream().mapToInt(Address::getId).max().orElse(0);
        address.setId(maxId + 1);

        this.addAddress(address);

        return address;
    }

    @Override
    public void export(FileType fileType) throws DataManagerException, ModelException {
        AddressDataManager.Data data = new Data(this);
        super.export(fileType, data);
    }

    @Override
    public int count() {
        return this.addresses.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        for (Address.Data addressData : dataObject.addresses) {
            Address address = new Address();
            address.hydrate(addressData);

            if (!address.isValid()) {
                throw new ModelException("Un des objets Address.Data reçu, transformé en Address, n'est pas valide");
            }

            this.addresses.put(address.getId(), address);
        }
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    public static class Data implements CustomSerializable, JsonConvertible {

        /** Properties **/

        private final List<Address.Data> addresses = new ArrayList<>();

        public Data(AddressDataManager addressManager) throws ModelException {
            for (Address address : addressManager.getAddresses().values()) {
                this.addresses.add(address.dehydrate());
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
