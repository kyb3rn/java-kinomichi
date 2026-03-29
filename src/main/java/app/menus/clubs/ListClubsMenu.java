package app.menus.clubs;

import app.data_management.managers.ClubDataManager;
import app.data_management.managers.DataManagers;
import app.data_management.managers.LoadDataManagerDataException;
import app.models.Club;
import app.models.formatting.ModelTableFormatter;
import utils.io.menus.MenuStage;

import java.util.*;
import java.util.stream.Collectors;

public class ListClubsMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public String use() {
        Collection<Club> clubs;
        try {
            clubs = DataManagers.get(ClubDataManager.class).getClubs().values();
        } catch (LoadDataManagerDataException e) {
            System.out.printf("%nLes clubs n'ont pas pu être chargés dans l'application.%n%n");
            return "clubs.manage";
        }

        List<Club> sorted = clubs.stream()
                .sorted(Comparator.comparingInt(Club::getId))
                .collect(Collectors.toList());

        ModelTableFormatter.forList(sorted).display();

        return "clubs.manage";
    }

}
