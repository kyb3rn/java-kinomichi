import app.menus.MainMenu;
import app.menus.clubs.AddClubMenu;
import app.menus.clubs.ListClubsMenu;
import app.menus.clubs.ManageClubsMenu;
import app.menus.countries.ListCountriesMenu;
import app.menus.countries.ManageCountriesMenu;
import utils.io.menus.MenuStage;

import java.util.HashMap;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws Exception {
        HashMap<String, Supplier<MenuStage>> menus = new HashMap<>();

        // Dynamic menus (rebuilt each time to refresh counts)
        menus.put("main", MainMenu::new);
        menus.put("countries.manage", ManageCountriesMenu::new);
        menus.put("clubs.manage", ManageClubsMenu::new);

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

}
