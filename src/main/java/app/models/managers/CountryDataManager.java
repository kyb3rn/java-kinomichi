package app.models.managers;

import app.models.Model;
import app.models.ModelException;
import app.models.NotResultForPrimaryKeyException;
import utils.data_management.FileType;
import app.models.Country;
import utils.data_management.converters.CustomSerializable;

import java.util.Collection;
import utils.data_management.converters.convertibles.CsvConvertible;
import utils.data_management.converters.readers.CsvReader;
import utils.data_management.converters.writers.*;
import utils.data_management.parsing.ParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class CountryDataManager extends DataManager<CountryDataManager.Data> {

    // ─── Properties ─── //

    private final TreeMap<String, Country> countries = new TreeMap<>();

    // ─── Getters ─── //

    public TreeMap<String, Country> getCountries() {
        return this.countries;
    }

    // ─── Utility methods ─── //

    public Country getCountry(String iso3) {
        return this.countries.get(iso3);
    }

    public void addCountry(Country country) throws ModelException, DataManagerException {
        if (!country.isValid()) {
            throw new ModelException("L'objet Country qui a voulu être ajouté n'est pas valide");
        }

        if (this.countries.containsKey(country.getIso3())) {
            throw new DataManagerException("Un pays portant l'ISO3 '%s' existe déjà".formatted(country.getIso3()));
        }

        this.countries.put(country.getIso3(), country);

        if (this.isInitialized()) {
            this.unsavedChanges = true;

            try {
                this.export();
            } catch (DataManagerException _) {
            }
        }
    }

    public Country getCountryWithExceptions(String iso3) throws NotResultForPrimaryKeyException {
        Country country;
        country = this.getCountry(iso3);

        if (country == null) {
            throw new NotResultForPrimaryKeyException("Aucun des pays enregistrés ne porte l'ISO3 '%s'".formatted(iso3));
        }

        return country;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Collection<Country> getModels() {
        return this.countries.values();
    }

    @Override
    public void init() throws LoadDataManagerDataException {
        if (!this.isInitialized()) {
            this.defaultFileType = FileType.CSV;

            DataWriter<Data> dataWriter = new DataWriter<>();
            CsvReader<Data> csvReader = new CsvReader<>(dataWriter);

            Data modelData = new Data();
            String filePath = this.getFilePath().toString();
            try {
                csvReader.readFile(filePath, modelData, true);
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
        return this.countries.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException, DataManagerException {
        for (Country country : dataObject.countries) {
            this.addCountry(country);
        }

        this.initialized = true;
    }

    @Override
    public Data dehydrate() {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data implements CustomSerializable, CsvConvertible {

        // ─── Properties ─── //

        private final List<Country> countries = new ArrayList<>();

        // ─── Constructors ─── //

        public Data(CountryDataManager countryManager) {
            this.countries.addAll(countryManager.getCountries().values());
        }

        public Data() {}

        // ─── Overrides & inheritance ─── //

        @Override
        public void parseLine(String[] columns) throws ParserException {
            Country country = new Country();
            country.parseLine(columns);
            this.countries.add(country);
        }

        @Override
        public List<String[]> toLines() {
            List<String[]> lines = new ArrayList<>();
            for (Country country : this.countries) {
                lines.add(new String[]{
                        country.getName(),
                        country.getIso2(),
                        country.getIso3()
                });
            }
            return lines;
        }

    }

}
