import app.data_management.managers.*;
import app.menus.clubs.AddClubMenu;
import app.menus.clubs.ListClubsMenu;
import app.menus.countries.ListCountriesMenu;
import utils.io.menus.StandardMenu;
import utils.io.menus.MenuStage;

import java.util.HashMap;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws Exception {
//        DataManagers.initAll(ClubDataManager.class);

        HashMap<String, Supplier<MenuStage>> menus = new HashMap<>();

        // Dynamic menus (rebuilt each time to refresh counts)
        menus.put("main", Main::getMainMenu);
        menus.put("countries.manage", Main::getManageCountriesMenu);
        menus.put("clubs.manage", Main::getManageClubsMenu);

        // Static menus (same instance reused)
        ListCountriesMenu listCountriesMenu = new ListCountriesMenu();
        ListClubsMenu listClubsMenu = new ListClubsMenu();
        AddClubMenu addClubMenu = new AddClubMenu();
        menus.put("countries.list", () -> listCountriesMenu);
        menus.put("clubs.list", () -> listClubsMenu);
        menus.put("clubs.add", () -> addClubMenu);

        String previousMenu = null;
        String nextMenu = "main";

        while (nextMenu != null) {
            Supplier<MenuStage> menuSupplier = menus.get(nextMenu);
            MenuStage nextMenuStage = menuSupplier != null ? menuSupplier.get() : null;

            if (nextMenuStage != null) {
                previousMenu = nextMenu;
                nextMenu = nextMenuStage.use();
            } else {
                System.out.printf("%nAucun nouveau menu ou action n'est lié à choix%n%n");
                nextMenu = previousMenu;
            }
        }

        System.out.printf("%nAu revoir !%n");
    }

    private static StandardMenu getMainMenu() {
        int countriesCount = DataManagers.getCountOf(CountryDataManager.class);
        int clubsCount = DataManagers.getCountOf(ClubDataManager.class);

        StandardMenu mainMenu = new StandardMenu("Kinomichi - Menu d'administration");
        mainMenu.addOption("Parcourir les pays (%d)".formatted(countriesCount), "countries.manage");
        mainMenu.addOption("Gestion des clubs (%d)".formatted(clubsCount), "clubs.manage");
        mainMenu.addOption("Quitter", null);
        return mainMenu;
    }

    private static StandardMenu getManageCountriesMenu() {
        int countriesCount = DataManagers.getCountOf(CountryDataManager.class);

        StandardMenu manageCountries = new StandardMenu("Kinomichi - Gestion des pays (%d)".formatted(countriesCount));
        manageCountries.addOption("Liste des pays", "countries.list");
        manageCountries.addOption("Retour", "main");
        return manageCountries;
    }

    private static StandardMenu getManageClubsMenu() {
        int clubsCount = DataManagers.getCountOf(ClubDataManager.class);

        StandardMenu manageClubs = new StandardMenu("Kinomichi - Gestion des clubs (%d)".formatted(clubsCount));
        manageClubs.addOption("Liste des clubs", "clubs.list");
        manageClubs.addOption("Ajouter un club", "clubs.add");
        manageClubs.addOption("Retour", "main");
        return manageClubs;
    }

}
