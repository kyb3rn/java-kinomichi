import app.menus.MainMenu;
import app.menus.clubs.AddClubMenu;
import app.menus.clubs.ListClubsMenu;
import app.menus.clubs.ManageClubsMenu;
import app.menus.countries.ListCountriesMenu;
import app.menus.countries.ManageCountriesMenu;
import app.menus.data_managers.ReInitDataManagersMenu;
import app.menus.data_managers.SaveDataManagersMenu;
import app.models.managers.AddressDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManagers;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

void main() {
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
    ReInitDataManagersMenu reinitDataManagersMenu = new ReInitDataManagersMenu();
    menus.put("data_managers.reinit", () -> reinitDataManagersMenu);

    menus.put("data_managers.save", SaveDataManagersMenu::new);

    ListCountriesMenu listCountriesMenu = new ListCountriesMenu();
    ListClubsMenu listClubsMenu = new ListClubsMenu();
    AddClubMenu addClubMenu = new AddClubMenu();
    menus.put("countries.list", () -> listCountriesMenu);
    menus.put("clubs.list", () -> listClubsMenu);
    menus.put("clubs.add", () -> addClubMenu);

    String previousMenuRoute = null;
    String nextMenuRoute = "main";

    while (nextMenuRoute != null) {
        Supplier<MenuStage> menuSupplier = menus.get(nextMenuRoute);
        MenuStage nextMenuStage = menuSupplier != null ? menuSupplier.get() : null;

        if (nextMenuStage != null) {
            previousMenuRoute = nextMenuRoute;
            MenuLeadTo menuLeadTo = nextMenuStage.use();
            nextMenuRoute = menuLeadTo != null ? menuLeadTo.getLeadTo() : null;
        } else {
            System.out.printf("%nAucun nouveau menu ou action n'est lié à choix%n%n");
            nextMenuRoute = previousMenuRoute;
        }
    }

    System.out.printf("%nAu revoir !%n");
}
