import app.AppState;
import app.controllers.MainController;
import app.controllers.PersonController;
import app.events.CallUrlEvent;
import app.events.Event;
import app.events.ExitProgramEvent;
import app.events.GoBackEvent;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.rooting.Route;
import app.rooting.RouteNotFoundException;
import app.rooting.Router;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;

public class Main {

    public static void main(String[] args) {
        DataManagers.initAndResolveReferencesOf(
            CampDataManager.class,
            PersonDataManager.class
        );

        Router router = new Router();
        MainController mainController = new MainController();
        PersonController personController = new PersonController();

        router.register(new Route("main", "/", mainController::index));
        router.register(new Route("persons.list", "/persons(?:/sort/(?<sort>.+))?", personController::list));

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
                nextPath = AppState.navigationHistory.goBack();
            }
        }

        System.out.println();
        System.out.println(TextFormatter.bold(TextFormatter.italic("Au revoir !")));
    }

}
