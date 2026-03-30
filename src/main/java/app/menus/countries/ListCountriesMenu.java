package app.menus.countries;

import app.data_management.managers.CountryDataManager;
import app.data_management.managers.DataManagers;
import app.data_management.managers.LoadDataManagerDataException;
import app.models.Country;
import app.models.formatting.ModelTableFormatter;
import utils.io.menus.MenuStage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListCountriesMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public String use() {
        Collection<Country> countries;
        try {
            countries = DataManagers.initAndGet(CountryDataManager.class).getCountries().values();
        } catch (LoadDataManagerDataException e) {
            System.out.printf("%nLes pays n'ont pas pu être chargés dans l'application.%n%n");
            return "countries.manage";
        }

        List<Country> sorted = countries.stream()
                .sorted(Comparator.comparing(Country::getName))
                .collect(Collectors.toList());

        ModelTableFormatter.forList(sorted).display();

        return "countries.manage";
    }

}
