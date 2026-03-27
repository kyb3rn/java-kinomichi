package app.menus.countries;

import app.data_management.managers.CountryDataManager;
import app.data_management.managers.DataManagers;
import app.data_management.managers.LoadDataManagerDataException;
import app.models.Country;
import utils.io.helpers.tables.TableFormatter;
import utils.io.helpers.texts.aligning.TextAlignement;
import utils.io.menus.MenuStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class ListCountriesMenu extends MenuStage {

    /** Overrides & inheritance **/

    @Override
    public String use() {
        Collection<Country> countries;
        try {
            countries = DataManagers.get(CountryDataManager.class).getCountries().values();
        } catch (LoadDataManagerDataException e) {
            System.out.printf("%nLes pays n'ont pas pu être chargés dans l'application.%n%n");
            return "countries.manage";
        }

        ArrayList<TableFormatter.Column> columns = new ArrayList<>();
        columns.add(new TableFormatter.Column("Nom", TextAlignement.LEFT));
        columns.add(new TableFormatter.Column("ISO 2", TextAlignement.CENTER));
        columns.add(new TableFormatter.Column("ISO 3", TextAlignement.CENTER));

        TableFormatter tableFormatter = new TableFormatter(columns);

        countries.stream()
            .sorted(Comparator.comparing(Country::getName))
            .forEach(country -> {
                tableFormatter.addColumnValue(0, country.getName());
                tableFormatter.addColumnValue(1, country.getIso2());
                tableFormatter.addColumnValue(2, country.getIso3());
            }
        );

        tableFormatter.display();

        return "countries.manage";
    }

}
