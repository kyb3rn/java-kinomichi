import app.AppState;
import app.controllers.*;
import app.events.*;
import app.models.managers.DataManagers;
import app.routing.Route;
import app.routing.RouteNotFoundException;
import app.routing.Router;
import utils.io.commands.CommandManager;
import utils.helpers.Functions;
import utils.io.text_formatting.TextFormatter;

public class Main {

    public static void main(String[] args) {
        DataManagers.initAll();

        CommandManager.loadCommands();

        Router router = new Router();

        MainController mainController = new MainController();
        ExploreController exploreController = new ExploreController();
        PersonController personController = new PersonController();
        CampController campController = new CampController();
        ClubController clubController = new ClubController();
        AffiliationController affiliationController = new AffiliationController();
        AddressController addressController = new AddressController();
        CountryController countryController = new CountryController();
        DataManagerController dataManagerController = new DataManagerController();


        // Main
        router.register(new Route("main", "/", mainController::index));

        // Explore
        router.register(new Route("explore", "/explore", exploreController::index));

        // Persons
        router.register(new Route("persons.dashboard", "/persons/dashboard", personController::dashboard));
        router.register(new Route("persons.list", "/persons/list(?:/sort/(?<sort>.+))?", personController::list));
        router.register(new Route("persons.add", "/persons/add", personController::add));
        router.register(new Route("persons.modify.select", "/persons/modify/select(?:/sort/(?<sort>.+))?", personController::modifySelect));
        router.register(new Route("persons.modify", "/persons/modify/(?<id>\\d+)", personController::modify));

        // Camps
        router.register(new Route("camps.list", "/camps/list(?:/sort/(?<sort>.+))?", campController::list));
        router.register(new Route("camps.add", "/camps/add", campController::add));
        router.register(new Route("camps.select", "/camps/select(?:/sort/(?<sort>.+))?", campController::select));
        router.register(new Route("camps.manage.camp", "/camps/manage/(?<id>\\d+)", campController::manageCamp));

        // Clubs
        router.register(new Route("clubs.list", "/clubs/list(?:/sort/(?<sort>.+))?", clubController::list));
        router.register(new Route("clubs.add", "/clubs/add", clubController::add));
        router.register(new Route("clubs.dashboard", "/clubs/dashboard", clubController::dashboard));
        router.register(new Route("clubs.modify.select", "/clubs/modify/select(?:/sort/(?<sort>.+))?", clubController::modifySelect));
        router.register(new Route("clubs.modify", "/clubs/modify/(?<id>\\d+)", clubController::modify));
        router.register(new Route("clubs.delete.select", "/clubs/delete/select(?:/sort/(?<sort>.+))?", clubController::delete));

        // Affiliations
        router.register(new Route("affiliations.dashboard", "/affiliations/dashboard", affiliationController::dashboard));
        router.register(new Route("affiliations.list", "/affiliations/list(?:/sort/(?<sort>.+))?", affiliationController::list));
        router.register(new Route("affiliations.add", "/affiliations/add", affiliationController::add));
        router.register(new Route("affiliations.modify.select", "/affiliations/modify/select(?:/sort/(?<sort>.+))?", affiliationController::modifySelect));
        router.register(new Route("affiliations.modify", "/affiliations/modify/(?<id>\\d+)", affiliationController::modify));
        router.register(new Route("affiliations.delete.select", "/affiliations/delete/select(?:/sort/(?<sort>.+))?", affiliationController::delete));

        // Addresses
        router.register(new Route("addresses.list", "/addresses/list(?:/sort/(?<sort>.+))?", addressController::list));

        // Countries
        router.register(new Route("countries.list", "/countries/list(?:/sort/(?<sort>.+))?", countryController::list));

        // Data Managers
        router.register(new Route("data_managers.reinit", "/data-managers/reinit", dataManagerController::reinit));
        router.register(new Route("data_managers.save", "/data-managers/save", dataManagerController::save));

        String nextPath = "/";

        while (nextPath != null) {
            try {
                AppState.navigationHistory.push(nextPath);
                Event event = router.dispatch(nextPath);

                nextPath = switch (event) {
                    case CallUrlEvent callUrlEvent -> callUrlEvent.getUrl();
                    case GoBackBackEvent _ -> AppState.navigationHistory.goBackUntilDifferentRoute(router);
                    case GoBackEvent _ -> AppState.navigationHistory.goBack();
                    case ExitProgramEvent _ -> null;
                    case null -> null;
                    default -> throw new UnhandledEventException(event);
                };
            } catch (RouteNotFoundException _) {
                System.out.println(Functions.styleAsErrorMessage("Route reçue ('%s') introuvable.".formatted(nextPath)));
                nextPath = "/";
            } catch (UnhandledEventException e) {
                System.out.println(Functions.styleAsErrorMessage("Aucun processus n'est lié à l'événement reçu (%s).".formatted(e.getEvent().getClass().getSimpleName())));
            }
        }

        DataManagers.exportAll();

        System.out.println();
        System.out.println(TextFormatter.bold(TextFormatter.italic("Au revoir !")));
    }

}
