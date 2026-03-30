import app.models.managers.AddressDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManagers;
import app.menus.MainMenu;
import app.menus.clubs.AddClubMenu;
import app.menus.clubs.ListClubsMenu;
import app.menus.clubs.ManageClubsMenu;
import app.menus.countries.ListCountriesMenu;
import app.menus.countries.ManageCountriesMenu;
import app.menus.data_managers.ReinitDataManagersMenu;
import utils.io.menus.MenuStage;

import java.util.HashMap;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws Exception {
        DataManagers.initAll(
            CountryDataManager.class,
            AddressDataManager.class,
            ClubDataManager.class
        );

        HashMap<String, Supplier<MenuStage>> menus = new HashMap<>();

        // Dynamic menus (rebuilt each time to refresh counts)
        menus.put("main", MainMenu::new);
        menus.put("countries.manage", ManageCountriesMenu::new);
        menus.put("clubs.manage", ManageClubsMenu::new);

        // Static menus (same instance reused)
        ReinitDataManagersMenu reinitDataManagersMenu = new ReinitDataManagersMenu();
        menus.put("data_managers.reinit", () -> reinitDataManagersMenu);

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

}
