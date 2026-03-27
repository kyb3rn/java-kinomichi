package app.data_management.managers;

import app.models.Address;
import app.models.ModelException;
import utils.data_management.FileType;
import app.models.Country;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.CsvConvertible;
import utils.data_management.converters.readers.CsvReader;
import utils.data_management.converters.writers.*;
import utils.data_management.parsing.ParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class CountryDataManager extends DataManager<CountryDataManager.Data> {

    private final HashMap<String, Country> countries = new HashMap<>();

    private CountryDataManager() throws LoadDataManagerDataException {
        this.defaultFileType = FileType.CSV;

        DataWriter<CountryDataManager.Data> dataWriter = new DataWriter<>();
        CsvReader<CountryDataManager.Data> csvReader = new CsvReader<>(dataWriter);

        CountryDataManager.Data modelData = new CountryDataManager.Data();
        String filePath = this.getFilePath().toString();
        try {
            csvReader.readFile(filePath, modelData, true);
        } catch (Exception e) {
            throw new LoadDataManagerDataException("Les données du manager '%s' n'ont pas pu être lues dans le fichier '%s'".formatted(this.getClass().getSimpleName(), filePath));
        }

        try {
            dataWriter.write(modelData, this);
        } catch (Exception e) {
            throw new LoadDataManagerDataException("Les données lues du manager '%s' dans le fichier '%s' n'ont pas pu être enregistrées dans le manager '%s'".formatted(this.getClass().getSimpleName(), filePath, e));
        }
    }

    public HashMap<String, Country> getCountries() {
        return this.countries;
    }

    public Country getCountry(String iso3) {
        return this.countries.get(iso3);
    }

    public void addCountry(Country country) throws ModelException {
        if (!country.isValid()) {
            throw new ModelException("L'objet Country qui a voulu être ajouté n'est pas valide");
        }

        if (this.countries.containsKey(country.getIso3())) {
            throw new ModelException("Un pays portant l'ISO3 '%s' existe déjà".formatted(country.getIso3()));
        }

        this.countries.put(country.getIso3(), country);
    }

    @Override
    public void export(FileType fileType) throws DataManagerException {
        CountryDataManager.Data data = new Data(this);
        super.export(fileType, data);
    }

    @Override
    public int count() {
        return this.countries.size();
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        for (Country country : dataObject.countries) {
            this.addCountry(country);
        }
    }

    @Override
    public Data dehydrate() {
        return new Data(this);
    }

    public static class Data implements CustomSerializable, CsvConvertible {

        /** Properties **/

        private final List<Country> countries = new ArrayList<>();

        public Data(CountryDataManager countryManager) {
            this.countries.addAll(countryManager.getCountries().values());
        }

        public Data() {}

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
