package app.models.managers;

import app.models.Model;
import app.models.ModelException;
import app.models.NotResultForPrimaryKeyException;
import app.models.Person;
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

public class PersonDataManager extends DataManager<PersonDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Person> persons = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<Integer, Person> getPersons() {
        return this.persons;
    }

    // ─── Utility methods ─── //

    public Person getPerson(Integer id) {
        return this.persons.get(id);
    }

    public Person getPersonWithExceptions(int id) throws NotResultForPrimaryKeyException {
        Person person = this.getPerson(id);

        if (person == null) {
            throw new NotResultForPrimaryKeyException("Aucune des personnes enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return person;
    }

    public Person addPerson(Person person, boolean autoAssignId) throws ModelException, DataManagerException {
        if (autoAssignId) {
            int maxId = this.persons.values().stream().mapToInt(Person::getId).max().orElse(0);
            person.setId(maxId + 1);
        }

        this.addPerson(person);

        return person;
    }

    public void addPerson(Person person) throws ModelException, DataManagerException {
        if (!person.isValid()) {
            throw new ModelException("La personne qui a voulu être ajouté n'est pas valide");
        }

        if (this.persons.containsKey(person.getId())) {
            throw new DataManagerException("Une personne portant l'identifiant '%d' existe déjà".formatted(person.getId()));
        }

        this.persons.put(person.getId(), person);

        if (this.isInitialized()) {
            this.unsavedChanges = true;

            try {
                this.export();
            } catch (DataManagerException _) {
            }
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Person> getModels() {
        return this.persons.values();
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
    public void export() throws DataManagerException {
        if (this.isInitialized()) {
            Data data = new Data(this);
            super.export(data);
            this.unsavedChanges = false;
        }
    }

    @Override
    public void export(FileType fileType) throws DataManagerException {
        if (this.isInitialized()) {
            Data data = new Data(this);
            super.export(fileType, data);
        }
    }

    @Override
    public int count() {
        return this.persons.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException, DataManagerException {
        for (Person person : dataObject.persons) {
            this.addPerson(person);
        }

        this.initialized = true;
    }

    @Override
    public Data dehydrate() {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private final List<Person> persons = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(PersonDataManager personDataManager) {
            this.persons.addAll(personDataManager.getPersons().values());
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

            if (!obj.has("persons")) {
                throw new StringParserException("Le champ 'persons' est manquant");
            } else if (!obj.get("persons").isJsonArray()) {
                throw new StringParserException("Le champ 'persons' doit être un tableau");
            }

            JsonArray personsArray = obj.getAsJsonArray("persons");
            for (JsonElement element : personsArray) {
                Person person = new Person();
                person.parseJson(element.toString());
                this.persons.add(person);
            }
        }

        @Override
        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

}
