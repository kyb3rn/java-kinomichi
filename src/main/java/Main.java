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
        SessionController sessionController = new SessionController();
        DinnerController dinnerController = new DinnerController();
        LodgingController lodgingController = new LodgingController();
        DataManagerController dataManagerController = new DataManagerController();


        // Main
        router.register(new Route("main", "/", mainController::index));

        // Explore
        router.register(new Route("explore", "/explore", exploreController::index));
        router.register(new Route("explore.camps", "/explore/camps(?:/sort/(?<sort>.+))?", exploreController::listCamps));
        router.register(new Route("explore.persons", "/explore/persons(?:/sort/(?<sort>.+))?", exploreController::listPersons));
        router.register(new Route("explore.affiliations", "/explore/affiliations(?:/sort/(?<sort>.+))?", exploreController::listAffiliations));
        router.register(new Route("explore.dinners", "/explore/dinners(?:/sort/(?<sort>.+))?", exploreController::listDinners));
        router.register(new Route("explore.lodgings", "/explore/lodgings(?:/sort/(?<sort>.+))?", exploreController::listLodgings));
        router.register(new Route("explore.invitations", "/explore/invitations(?:/sort/(?<sort>.+))?", exploreController::listInvitations));
        router.register(new Route("explore.dinner_reservations", "/explore/dinner-reservations(?:/sort/(?<sort>.+))?", exploreController::listDinnerReservations));
        router.register(new Route("explore.lodging_reservations", "/explore/lodging-reservations(?:/sort/(?<sort>.+))?", exploreController::listLodgingReservations));
        router.register(new Route("explore.sessions", "/explore/sessions(?:/sort/(?<sort>.+))?", exploreController::listSessions));
        router.register(new Route("explore.session_trainers", "/explore/session-trainers(?:/sort/(?<sort>.+))?", exploreController::listSessionTrainers));
        router.register(new Route("explore.session_registrations", "/explore/session-registrations(?:/sort/(?<sort>.+))?", exploreController::listSessionRegistrations));
        router.register(new Route("explore.clubs", "/explore/clubs(?:/sort/(?<sort>.+))?", exploreController::listClubs));
        router.register(new Route("explore.addresses", "/explore/addresses(?:/sort/(?<sort>.+))?", exploreController::listAddresses));
        router.register(new Route("explore.countries", "/explore/countries(?:/sort/(?<sort>.+))?", exploreController::listCountries));

        // Persons
        router.register(new Route("persons.dashboard", "/persons/dashboard", personController::dashboard));
        router.register(new Route("persons.list", "/persons/list(?:/sort/(?<sort>.+))?", personController::list));
        router.register(new Route("persons.add", "/persons/add", personController::add));
        router.register(new Route("persons.modify.select", "/persons/modify/select(?:/sort/(?<sort>.+))?", personController::modifySelect));
        router.register(new Route("persons.modify", "/persons/modify/(?<id>\\d+)", personController::modify));
        router.register(new Route("persons.delete.select", "/persons/delete/select(?:/sort/(?<sort>.+))?", personController::delete));

        // Camps
        router.register(new Route("camps.add", "/camps/add", campController::add));
        router.register(new Route("camps.select", "/camps/select(?:/sort/(?<sort>.+))?", campController::select));
        router.register(new Route("camps.modify", "/camps/modify/(?<id>\\d+)", campController::modify));
        router.register(new Route("camps.delete", "/camps/delete/(?<id>\\d+)", campController::delete));
        router.register(new Route("camps.manage.camp", "/camps/manage/(?<id>\\d+)", campController::manageCamp));
        router.register(new Route("camps.invitations", "/camps/manage/(?<campId>\\d+)/invitations", campController::manageInvitations));
        router.register(new Route("camps.invitations.list", "/camps/manage/(?<campId>\\d+)/invitations/list(?:/sort/(?<sort>.+))?", campController::listInvitations));
        router.register(new Route("camps.invitations.add", "/camps/manage/(?<campId>\\d+)/invitations/add", campController::addInvitation));
        router.register(new Route("camps.invitations.delete", "/camps/manage/(?<campId>\\d+)/invitations/delete(?:/sort/(?<sort>.+))?", campController::deleteInvitation));

        // Sessions (camp-scoped)
        router.register(new Route("camps.sessions", "/camps/manage/(?<campId>\\d+)/sessions", sessionController::manageSessions));
        router.register(new Route("camps.sessions.list", "/camps/manage/(?<campId>\\d+)/sessions/list(?:/sort/(?<sort>.+))?", sessionController::listSessions));
        router.register(new Route("camps.sessions.add", "/camps/manage/(?<campId>\\d+)/sessions/add", sessionController::addSession));
        router.register(new Route("camps.sessions.modify.select", "/camps/manage/(?<campId>\\d+)/sessions/modify/select(?:/sort/(?<sort>.+))?", sessionController::modifySelect));
        router.register(new Route("camps.sessions.modify", "/camps/manage/(?<campId>\\d+)/sessions/modify/(?<sessionId>\\d+)", sessionController::modify));
        router.register(new Route("camps.sessions.delete.select", "/camps/manage/(?<campId>\\d+)/sessions/delete/select(?:/sort/(?<sort>.+))?", sessionController::deleteSelect));
        router.register(new Route("camps.sessions.trainers.select", "/camps/manage/(?<campId>\\d+)/sessions/trainers/select(?:/sort/(?<sort>.+))?", sessionController::selectSessionForTrainers));
        router.register(new Route("camps.sessions.trainers", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/trainers", sessionController::manageSessionTrainers));
        router.register(new Route("camps.sessions.trainers.list", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/trainers/list(?:/sort/(?<sort>.+))?", sessionController::listSessionTrainers));
        router.register(new Route("camps.sessions.trainers.add", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/trainers/add", sessionController::addSessionTrainer));
        router.register(new Route("camps.sessions.trainers.delete", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/trainers/delete(?:/sort/(?<sort>.+))?", sessionController::deleteSessionTrainer));
        router.register(new Route("camps.sessions.registrations.select", "/camps/manage/(?<campId>\\d+)/sessions/registrations/select(?:/sort/(?<sort>.+))?", sessionController::selectSessionForRegistrations));
        router.register(new Route("camps.sessions.registrations", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/registrations", sessionController::manageSessionRegistrations));
        router.register(new Route("camps.sessions.registrations.list", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/registrations/list(?:/sort/(?<sort>.+))?", sessionController::listSessionRegistrations));
        router.register(new Route("camps.sessions.registrations.add", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/registrations/add", sessionController::addSessionRegistration));
        router.register(new Route("camps.sessions.registrations.delete", "/camps/manage/(?<campId>\\d+)/sessions/(?<sessionId>\\d+)/registrations/delete(?:/sort/(?<sort>.+))?", sessionController::deleteSessionRegistration));

        // Dinners (camp-scoped)
        router.register(new Route("camps.dinners", "/camps/manage/(?<campId>\\d+)/dinners", dinnerController::manageDinners));
        router.register(new Route("camps.dinners.list", "/camps/manage/(?<campId>\\d+)/dinners/list(?:/sort/(?<sort>.+))?", dinnerController::listDinners));
        router.register(new Route("camps.dinners.add", "/camps/manage/(?<campId>\\d+)/dinners/add", dinnerController::addDinner));
        router.register(new Route("camps.dinners.modify.select", "/camps/manage/(?<campId>\\d+)/dinners/modify/select(?:/sort/(?<sort>.+))?", dinnerController::modifySelect));
        router.register(new Route("camps.dinners.modify", "/camps/manage/(?<campId>\\d+)/dinners/modify/(?<dinnerId>\\d+)", dinnerController::modify));
        router.register(new Route("camps.dinners.delete.select", "/camps/manage/(?<campId>\\d+)/dinners/delete/select(?:/sort/(?<sort>.+))?", dinnerController::deleteSelect));
        router.register(new Route("camps.dinners.reservations.select", "/camps/manage/(?<campId>\\d+)/dinners/reservations/select(?:/sort/(?<sort>.+))?", dinnerController::selectDinnerForReservations));
        router.register(new Route("camps.dinners.reservations", "/camps/manage/(?<campId>\\d+)/dinners/(?<dinnerId>\\d+)/reservations", dinnerController::manageDinnerReservations));
        router.register(new Route("camps.dinners.reservations.list", "/camps/manage/(?<campId>\\d+)/dinners/(?<dinnerId>\\d+)/reservations/list(?:/sort/(?<sort>.+))?", dinnerController::listDinnerReservations));
        router.register(new Route("camps.dinners.reservations.add", "/camps/manage/(?<campId>\\d+)/dinners/(?<dinnerId>\\d+)/reservations/add", dinnerController::addDinnerReservation));
        router.register(new Route("camps.dinners.reservations.delete", "/camps/manage/(?<campId>\\d+)/dinners/(?<dinnerId>\\d+)/reservations/delete(?:/sort/(?<sort>.+))?", dinnerController::deleteDinnerReservation));

        // Lodgings (camp-scoped)
        router.register(new Route("camps.lodgings", "/camps/manage/(?<campId>\\d+)/lodgings", lodgingController::manageLodgings));
        router.register(new Route("camps.lodgings.list", "/camps/manage/(?<campId>\\d+)/lodgings/list(?:/sort/(?<sort>.+))?", lodgingController::listLodgings));
        router.register(new Route("camps.lodgings.add", "/camps/manage/(?<campId>\\d+)/lodgings/add", lodgingController::addLodging));
        router.register(new Route("camps.lodgings.modify.select", "/camps/manage/(?<campId>\\d+)/lodgings/modify/select(?:/sort/(?<sort>.+))?", lodgingController::modifySelect));
        router.register(new Route("camps.lodgings.modify", "/camps/manage/(?<campId>\\d+)/lodgings/modify/(?<lodgingId>\\d+)", lodgingController::modify));
        router.register(new Route("camps.lodgings.delete.select", "/camps/manage/(?<campId>\\d+)/lodgings/delete/select(?:/sort/(?<sort>.+))?", lodgingController::deleteSelect));
        router.register(new Route("camps.lodgings.reservations.select", "/camps/manage/(?<campId>\\d+)/lodgings/reservations/select(?:/sort/(?<sort>.+))?", lodgingController::selectLodgingForReservations));
        router.register(new Route("camps.lodgings.reservations", "/camps/manage/(?<campId>\\d+)/lodgings/(?<lodgingId>\\d+)/reservations", lodgingController::manageLodgingReservations));
        router.register(new Route("camps.lodgings.reservations.list", "/camps/manage/(?<campId>\\d+)/lodgings/(?<lodgingId>\\d+)/reservations/list(?:/sort/(?<sort>.+))?", lodgingController::listLodgingReservations));
        router.register(new Route("camps.lodgings.reservations.add", "/camps/manage/(?<campId>\\d+)/lodgings/(?<lodgingId>\\d+)/reservations/add", lodgingController::addLodgingReservation));
        router.register(new Route("camps.lodgings.reservations.delete", "/camps/manage/(?<campId>\\d+)/lodgings/(?<lodgingId>\\d+)/reservations/delete(?:/sort/(?<sort>.+))?", lodgingController::deleteLodgingReservation));

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
