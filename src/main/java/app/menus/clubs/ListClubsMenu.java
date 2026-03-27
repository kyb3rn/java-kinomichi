package app.menus.clubs;

import app.data_management.managers.ClubDataManager;
import app.data_management.managers.DataManagers;
import app.data_management.managers.LoadDataManagerDataException;
import app.models.Club;
import utils.io.helpers.tables.TableFormatter;
import utils.io.helpers.texts.aligning.TextAlignement;
import utils.io.menus.MenuStage;

import java.util.*;

public class ListClubsMenu extends MenuStage {

    /** Overrides & inheritance **/

    @Override
    public String use() {
        Collection<Club> clubs;
        try {
            clubs = DataManagers.get(ClubDataManager.class).getClubs().values();
        } catch (LoadDataManagerDataException e) {
            System.out.printf("%nLes pays n'ont pas pu être chargés dans l'application.%n%n");
            return "countries.manage";
        }

        ArrayList<TableFormatter.Column> columns = new ArrayList<>();
        columns.add(new TableFormatter.Column("ID", TextAlignement.CENTER));
        columns.add(new TableFormatter.Column("Nom", TextAlignement.LEFT));
        columns.add(new TableFormatter.Column("ID adresse", TextAlignement.CENTER));
        columns.add(new TableFormatter.Column("Lien Google Maps", TextAlignement.LEFT));

        TableFormatter tableFormatter = new TableFormatter(columns);

        clubs.stream()
            .sorted(Comparator.comparingInt(Club::getId))
            .forEach(country -> {
                tableFormatter.addColumnValue(0, String.valueOf(country.getId()));
                tableFormatter.addColumnValue(1, country.getName());
                tableFormatter.addColumnValue(2, String.valueOf(country.getAddress().getId()));
                tableFormatter.addColumnValue(3, country.getGoogleMapsPositionLink());
            }
        );

        tableFormatter.display();

        return "clubs.manage";
    }

}
