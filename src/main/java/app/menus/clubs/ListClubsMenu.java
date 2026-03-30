package app.menus.clubs;

import app.models.managers.ClubDataManager;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import app.models.Club;
import app.models.formatting.ModelTableFormatter;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

import java.util.*;

public class ListClubsMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        Collection<Club> clubs;
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.initAndGet(ClubDataManager.class);
            clubs = clubDataManager.getClubs().values();
        } catch (LoadDataManagerDataException e) {
            System.out.printf("%nLes clubs n'ont pas pu être chargés dans l'application.%n%n");
            return new MenuLeadTo("clubs.manage");
        }

        List<Club> sorted = clubs.stream().sorted(Comparator.comparingInt(Club::getId)).toList();

        ModelTableFormatter.forList(sorted).display();

        if (clubDataManager.hasUnsavedChanges()) {
            System.out.printf(TextFormatter.italic(TextFormatter.yellow(TextFormatter.bold("ATTENTION !") + " Des modifications dans cette liste n'ont pas encore été sauvegardées. Rendez-vous dans le menu principal pour résoudre ce problème.%n%n")));
        }

        return new MenuLeadTo("clubs.manage");
    }

}
