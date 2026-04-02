import app.AppState;
import app.controllers.*;
import app.events.CallUrlEvent;
import app.events.Event;
import app.events.ExitProgramEvent;
import app.events.GoBackEvent;
import app.models.managers.AffiliatedDataManager;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.rooting.Route;
import app.rooting.RouteNotFoundException;
import app.rooting.Router;
import utils.io.commands.CommandManager;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;

public class Main {

    public static void main(String[] args) {
        DataManagers.initAndResolveReferencesOf(
            CampDataManager.class,
            PersonDataManager.class,
            AffiliatedDataManager.class
        );

        CommandManager.loadCommands();

        Router router = new Router();

        MainController mainController = new MainController();
        ExploreController exploreController = new ExploreController();
        PersonController personController = new PersonController();
        CampController campController = new CampController();
        ClubController clubController = new ClubController();
        AffiliatedController affiliatedController = new AffiliatedController();
        AddressController addressController = new AddressController();
        CountryController countryController = new CountryController();
        DataManagerController dataManagerController = new DataManagerController();


        // Main
        router.register(new Route("main", "/", mainController::index));

        // Explore
        router.register(new Route("explore", "/explore", exploreController::index));

        // Persons
        router.register(new Route("persons.list", "/persons(?:/sort/(?<sort>.+))?", personController::list));

        // Camps
        router.register(new Route("camps.list", "/camps/list(?:/sort/(?<sort>.+))?", campController::list));
        router.register(new Route("camps.add", "/camps/add", campController::add));
        router.register(new Route("camps.select", "/camps/select(?:/sort/(?<sort>.+))?", campController::select));
        router.register(new Route("camps.manage.camp", "/camps/manage/(?<id>\\d+)", campController::manageCamp));

        // Clubs
        router.register(new Route("clubs.list", "/clubs/list(?:/sort/(?<sort>.+))?", clubController::list));
        router.register(new Route("clubs.add", "/clubs/add", clubController::add));
        router.register(new Route("clubs.dashboard", "/clubs/dashboard", clubController::dashboard));

        // Affiliateds
        router.register(new Route("affiliateds.list", "/affiliateds/list(?:/sort/(?<sort>.+))?", affiliatedController::list));

        // Addresses
        router.register(new Route("addresses.list", "/addresses/list(?:/sort/(?<sort>.+))?", addressController::list));

        // Countries
        router.register(new Route("countries.list", "/countries/list(?:/sort/(?<sort>.+))?", countryController::list));

        // Data Managers
        router.register(new Route("data_managers.reinit", "/data-managers/reinit(?:/(?<manager>.+))?", dataManagerController::reinit));
        router.register(new Route("data_managers.save", "/data-managers/save(?:/(?<manager>.+))?", dataManagerController::save));

        String nextPath = "/";

        while (nextPath != null) {
            try {
                AppState.navigationHistory.push(nextPath);
                Event event = router.dispatch(nextPath);

                nextPath = switch (event) {
                    case CallUrlEvent callUrlEvent -> callUrlEvent.getUrl();
                    case GoBackEvent _ -> AppState.navigationHistory.goBack();
                    case ExitProgramEvent _ -> null;
                    default -> null;
                };
            } catch (RouteNotFoundException routeNotFoundException) {
                System.out.println(Functions.styleAsErrorMessage("Route introuvable : '%s'".formatted(nextPath)));
                nextPath = "/";
            }
        }

        System.out.println();
        System.out.println(TextFormatter.bold(TextFormatter.italic("Au revoir !")));
    }

}
