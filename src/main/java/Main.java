import app.menus.MainMenu;
import app.menus.addresses.ListAddressesMenu;
import app.menus.addresses.ManageAddressesMenu;
import app.menus.camps.*;
import app.menus.clubs.AddClubMenu;
import app.menus.clubs.ListClubsMenu;
import app.menus.clubs.ManageClubsMenu;
import app.menus.countries.ListCountriesMenu;
import app.menus.countries.ManageCountriesMenu;
import app.menus.data_managers.ReInitDataManagersMenu;
import app.menus.data_managers.SaveDataManagersMenu;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

void main() {
    DataManagers.initAndResolveReferencesOf(
        CampDataManager.class
    );

    HashMap<String, Supplier<MenuStage>> menus = new HashMap<>();

    // Dynamic menus (rebuilt each time to refresh counts)
    // Main
    menus.put("main", MainMenu::new);

    // Camps
    menus.put("camps.manage", ManageCampsMenu::new);
    menus.put("camps.manage.camp", ManageCampMenu::new);

    // Countries
    menus.put("countries.manage", ManageCountriesMenu::new);

    // Addresses
    menus.put("addresses.manage", ManageAddressesMenu::new);

    // Clubs
    menus.put("clubs.manage", ManageClubsMenu::new);

    // Static menus (same instance reused)
    // Camps
    AddCampMenu addCampMenu = new AddCampMenu();
    ListCampsMenu listCampsMenu = new ListCampsMenu();
    SelectCampMenu selectCampMenu = new SelectCampMenu();
    menus.put("camps.add", () -> addCampMenu);
    menus.put("camps.list", () -> listCampsMenu);
    menus.put("camps.select", () -> selectCampMenu);

    // Countries
    ListCountriesMenu listCountriesMenu = new ListCountriesMenu();
    menus.put("countries.list", () -> listCountriesMenu);

    // Addresses
    ListAddressesMenu listAddressesMenu = new ListAddressesMenu();
    menus.put("addresses.list", () -> listAddressesMenu);

    // Clubs
    ListClubsMenu listClubsMenu = new ListClubsMenu();
    AddClubMenu addClubMenu = new AddClubMenu();
    menus.put("clubs.list", () -> listClubsMenu);
    menus.put("clubs.add", () -> addClubMenu);

    // DataManagers
    ReInitDataManagersMenu reinitDataManagersMenu = new ReInitDataManagersMenu();
    SaveDataManagersMenu saveDataManagersMenu = new SaveDataManagersMenu();
    menus.put("data_managers.reinit", () -> reinitDataManagersMenu);
    menus.put("data_managers.save", () -> saveDataManagersMenu);

    // Rooting
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
            System.out.printf(Functions.styleAsErrorMessage("Aucun nouveau menu ou action n'est lié à choix%n%n"));
            nextMenuRoute = previousMenuRoute;
        }
    }

    System.out.println(TextFormatter.bold(TextFormatter.italic("Au revoir !")));
}
