package app.menus.countries;

import app.models.managers.CountryDataManager;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import app.models.Country;
import app.models.formatting.ModelTableFormatter;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ListCountriesMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        Collection<Country> countries;
        CountryDataManager countryDataManager;
        try {
            countryDataManager = DataManagers.initAndGet(CountryDataManager.class);
            countries = countryDataManager.getCountries().values();
        } catch (LoadDataManagerDataException e) {
            System.out.printf("%nLes pays n'ont pas pu être chargés dans l'application.%n%n");
            return new MenuLeadTo("countries.manage");
        }

        List<Country> sorted = countries.stream().sorted(Comparator.comparing(Country::getName)).toList();

        ModelTableFormatter.forList(sorted).display();

        if (countryDataManager.hasUnsavedChanges()) {
            System.out.printf(TextFormatter.italic(TextFormatter.yellow(TextFormatter.bold("ATTENTION !") + " Des modifications dans cette liste n'ont pas encore été sauvegardées. Rendez-vous dans le menu principal pour résoudre ce problème.%n%n")));
        }

        return new MenuLeadTo("countries.manage");
    }

}
