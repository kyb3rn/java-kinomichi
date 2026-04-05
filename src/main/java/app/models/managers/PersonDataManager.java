package app.models.managers;

import app.models.IdentifiedModel;
import app.models.ModelException;
import app.models.NoResultForPrimaryKeyException;
import app.models.Person;
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

public class PersonDataManager extends DataManager<PersonDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<Integer, Person> persons = new TreeMap<>();

    // ─── Getters ─── //

    public SortedMap<Integer, Person> getPersons() {
        return Collections.unmodifiableSortedMap(this.persons);
    }

    // ─── Utility methods ─── //

    public Person getPerson(Integer id) throws ModelException {
        if (id == null) {
            throw new NoResultForPrimaryKeyException("L'identifiant de recherche parmi les données enregistrées ne peut pas être nul");
        }

        id = IdentifiedModel.verifyId(id);

        return this.persons.get(id);
    }

    public Person getPersonWithExceptions(int id) throws ModelException {
        Person person = this.getPerson(id);

        if (person == null) {
            throw new NoResultForPrimaryKeyException("Aucune des personnes enregistrées ne porte l'identifiant '%d'".formatted(id));
        }

        return person;
    }

    public void addPerson(Person person) throws ModelException, DataManagerException {
        if (person == null) {
            throw new ModelException("La personne à ajouter ne peut pas être nulle");
        }

        if (!person.isValid()) {
            throw new ModelException("La personne à ajouter n'est pas valide");
        }

        this.applyAutoIncrementIfPossible(person);

        if (this.persons.containsKey(person.getId())) {
            throw new DataManagerException("Une personne portant l'identifiant '%d' existe déjà".formatted(person.getId()));
        }

        this.persons.put(person.getId(), person);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    public void updatePerson(int personId, Person modifiedPerson) throws ModelException, DataManagerException {
        personId = IdentifiedModel.verifyId(personId);

        if (modifiedPerson == null) {
            throw new ModelException("La personne modifiée ne peut pas être nulle");
        }

        if (!modifiedPerson.isValid()) {
            throw new ModelException("La personne modifiée reçue n'est pas valide");
        }

        Person personToModify = this.getPersonWithExceptions(personId);

        if (personToModify.getId() != modifiedPerson.getId()) {
            throw new ModelException("Modifier l'identifiant d'un modèle n'est pas autorisé");
        }

        personToModify.hydrate(modifiedPerson);

        if (this.isInitialized()) {
            this.unsavedChanges = true;
            this.export();
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Person> getModels() {
        return this.persons.values();
    }

    @Override
    public void init() throws LoadDataManagerDataException {
        this.defaultJsonInit(new Data());
    }

    @Override
    public void export() throws DataManagerException {
        Data data = new Data(this);
        super.export(data);
    }

    @Override
    public void export(FileType fileType) throws DataManagerException {
        Data data = new Data(this);
        super.export(fileType, data);
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
            this.persons.addAll(personDataManager.getModels());
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
