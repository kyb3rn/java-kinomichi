import app.AppState;
import app.views.menus.MainMenu;
import app.views.menus.camps.*;
import app.views.menus.clubs.AddClubMenu;
import app.views.menus.clubs.ManageClubsMenu;
import app.views.menus.data_managers.ReInitDataManagersMenu;
import app.views.menus.data_managers.SaveDataManagersMenu;
import app.views.menus.explore.ExploreDataMenu;
import app.models.managers.*;
import app.views.utils.ModelTableMenu;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;
import utils.io.menus.UnloadableMenuException;

void main() {
    DataManagers.initAndResolveReferencesOf(
        CampDataManager.class,
        PersonDataManager.class,
        AffiliatedDataManager.class
    );

    HashMap<String, Supplier<MenuStage>> menus = new HashMap<>();

    // Dynamic menus (rebuilt each time to refresh counts)
    // Main
    menus.put("main", MainMenu::new);

    // Explore
    menus.put("explore", ExploreDataMenu::new);

    // Camps
    menus.put("camps.manage", ManageCampsMenu::new);
    menus.put("camps.manage.camp", ManageCampMenu::new);

    // Clubs
    menus.put("clubs.manage", ManageClubsMenu::new);

    // Static menus (same instance reused)
    // Camps
    AddCampMenu addCampMenu = new AddCampMenu();
    SelectCampMenu selectCampMenu = new SelectCampMenu();
    menus.put("camps.add", () -> addCampMenu);
    menus.put("camps.select", () -> selectCampMenu);

    // Clubs
    AddClubMenu addClubMenu = new AddClubMenu();
    menus.put("clubs.add", () -> addClubMenu);

    // Model table menus (list views)
    ModelTableMenu listCampsMenu = new ModelTableMenu(CampDataManager.class);
    ModelTableMenu listCountriesMenu = new ModelTableMenu(CountryDataManager.class);
    ModelTableMenu listAddressesMenu = new ModelTableMenu(AddressDataManager.class);
    ModelTableMenu listClubsMenu = new ModelTableMenu(ClubDataManager.class);
    ModelTableMenu listPersonsMenu = new ModelTableMenu(PersonDataManager.class);
    ModelTableMenu listAffiliatedsMenu = new ModelTableMenu(AffiliatedDataManager.class);
    menus.put("camps.list", () -> listCampsMenu);
    menus.put("countries.list", () -> listCountriesMenu);
    menus.put("addresses.list", () -> listAddressesMenu);
    menus.put("clubs.list", () -> listClubsMenu);
    menus.put("persons.list", () -> listPersonsMenu);
    menus.put("affiliateds.list", () -> listAffiliatedsMenu);

    // DataManagers
    ReInitDataManagersMenu reinitDataManagersMenu = new ReInitDataManagersMenu();
    SaveDataManagersMenu saveDataManagersMenu = new SaveDataManagersMenu();
    menus.put("data_managers.reinit", () -> reinitDataManagersMenu);
    menus.put("data_managers.save", () -> saveDataManagersMenu);

    // Rooting
    String nextMenuRoute = "main";

    while (nextMenuRoute != null) {
        Supplier<MenuStage> menuSupplier = menus.get(nextMenuRoute);

        try {
            MenuStage nextMenuStage = menuSupplier != null ? menuSupplier.get() : null;

            if (nextMenuStage != null) {
                AppState.navigationHistory.push(new MenuLeadTo(nextMenuRoute));
                MenuLeadTo menuLeadTo = nextMenuStage.use();
                nextMenuRoute = menuLeadTo != null ? menuLeadTo.getLeadTo() : null;
            } else {
                System.out.println(Functions.styleAsErrorMessage("Aucun nouveau menu ou action n'est lié à choix"));
                MenuLeadTo lastMenuLeadTo = AppState.navigationHistory.getLast();
                nextMenuRoute = lastMenuLeadTo != null ? lastMenuLeadTo.getLeadTo() : null;
            }
        } catch (UnloadableMenuException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible de naviguer dans le menu '%s'. Retour au menu précédent."));
            MenuLeadTo previousMenuLeadTo = AppState.navigationHistory.goBack();
            nextMenuRoute = previousMenuLeadTo != null ? previousMenuLeadTo.getLeadTo() : null;
        }
    }

    System.out.println();
    System.out.println(TextFormatter.bold(TextFormatter.italic("Au revoir !")));
}
