package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Camp;
import app.models.ModelException;
import app.models.Session;
import app.models.SessionRegistration;
import app.models.SessionTrainer;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.SessionDataManager;
import app.models.managers.SessionRegistrationDataManager;
import app.models.managers.SessionTrainerDataManager;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.sessions.AddSessionRegistrationView;
import app.views.sessions.AddSessionTrainerView;
import app.views.sessions.AddSessionView;
import app.views.sessions.DeleteSessionRegistrationView;
import app.views.sessions.DeleteSessionTrainerView;
import app.views.sessions.DeleteSessionView;
import app.views.sessions.ManageSessionRegistrationsView;
import app.views.sessions.ManageSessionTrainersView;
import app.views.sessions.ManageSessionsView;
import app.views.sessions.ModifySessionFormData;
import app.views.sessions.ModifySessionView;
import app.views.sessions.SelectSessionView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class SessionController extends Controller {

    // ─── Utility methods ─── //

    public Event manageSessions(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        ManageSessionsView manageSessionsView = new ManageSessionsView(campId);
        return manageSessionsView.render();
    }

    public Event listSessions(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        SessionDataManager sessionDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Session> campSessions;
        try {
            campSessions = sessionDataManager.getCampSessions(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Session> sortedSessions;

        try {
            sortedSessions = this.sortModels(campSessions, Session.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        ModelListView<Session> sessionListView = new ModelListView<>(Session.class, sortedSessions, sessionDataManager.hasUnsavedChanges(), "/camps/manage/%d/sessions/list".formatted(campId), "/camps/manage/%d/sessions".formatted(campId));
        return sessionListView.render();
    }

    public Event addSession(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        AddSessionView addSessionView = new AddSessionView(campId);
        Event event = addSessionView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Session sessionToAdd) {
            try {
                SessionDataManager sessionDataManager = DataManagers.get(SessionDataManager.class);
                sessionDataManager.addSession(sessionToAdd);

                SimpleBox sessionAddedSimpleBox = new SimpleBox();
                sessionAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Session ajoutée")));
                sessionAddedSimpleBox.addLine(TextFormatter.italic("La session a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + sessionToAdd.getId())));

                System.out.println();
                sessionAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        return event;
    }

    public Event modifySelect(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        SessionDataManager sessionDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Session> campSessions;
        try {
            campSessions = sessionDataManager.getCampSessions(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Session> sortedSessions;

        try {
            sortedSessions = this.sortModels(campSessions, Session.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        SelectSessionView selectSessionView = new SelectSessionView(campId, sortedSessions, sessionDataManager);
        Event event = selectSessionView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer sessionId) {
            return new CallUrlEvent("/camps/manage/%d/sessions/modify/%d".formatted(campId, sessionId));
        }

        return event;
    }

    public Event modify(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        SessionDataManager sessionDataManager;
        try {
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des sessions n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        Session session;
        try {
            session = sessionDataManager.getSessionWithExceptions(sessionId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        ModifySessionView modifySessionView = new ModifySessionView(session);
        Event event = modifySessionView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof ModifySessionFormData modifySessionFormData) {
            try {
                sessionDataManager.updateSession(sessionId, modifySessionFormData.modifiedSession());

                SimpleBox sessionModifiedSimpleBox = new SimpleBox();
                sessionModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Session modifiée")));
                sessionModifiedSimpleBox.addLine(TextFormatter.italic("La session " + TextFormatter.bold("#" + sessionId) + " a bien été modifiée"));

                System.out.println();
                sessionModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        return event;
    }

    public Event deleteSelect(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        SessionDataManager sessionDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Session> campSessions;
        try {
            campSessions = sessionDataManager.getCampSessions(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Session> sortedSessions;

        try {
            sortedSessions = this.sortModels(campSessions, Session.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        DeleteSessionView deleteSessionView = new DeleteSessionView(campId, sortedSessions, sessionDataManager);
        Event event = deleteSessionView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer deleteSessionId) {
            try {
                Session sessionToDelete = sessionDataManager.getSessionWithExceptions(deleteSessionId);
                sessionDataManager.deleteSession(deleteSessionId);

                SimpleBox sessionDeletedSimpleBox = new SimpleBox();
                sessionDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Session supprimée")));
                sessionDeletedSimpleBox.addLine(TextFormatter.italic("La session " + TextFormatter.bold(sessionToDelete.toString()) + " a bien été supprimée."));

                System.out.println();
                sessionDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        return event;
    }

    public Event selectSessionForTrainers(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        SessionDataManager sessionDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Session> campSessions;
        try {
            campSessions = sessionDataManager.getCampSessions(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Session> sortedSessions;

        try {
            sortedSessions = this.sortModels(campSessions, Session.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        SelectSessionView selectSessionView = new SelectSessionView(campId, sortedSessions, sessionDataManager);
        Event event = selectSessionView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer sessionId) {
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        return event;
    }

    public Event manageSessionTrainers(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));
        ManageSessionTrainersView manageSessionTrainersView = new ManageSessionTrainersView(campId, sessionId);
        return manageSessionTrainersView.render();
    }

    public Event listSessionTrainers(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        SessionDataManager sessionDataManager;
        SessionTrainerDataManager sessionTrainerDataManager;
        try {
            sessionDataManager = DataManagers.get(SessionDataManager.class);
            sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        Session session;
        try {
            session = sessionDataManager.getSessionWithExceptions(sessionId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        List<SessionTrainer> sessionTrainers;
        try {
            sessionTrainers = sessionTrainerDataManager.getSessionSessionTrainers(session);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<SessionTrainer> sortedSessionTrainers;

        try {
            sortedSessionTrainers = this.sortModels(sessionTrainers, SessionTrainer.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        ModelListView<SessionTrainer> sessionTrainerListView = new ModelListView<>(SessionTrainer.class, sortedSessionTrainers, sessionTrainerDataManager.hasUnsavedChanges(), "/camps/manage/%d/sessions/%d/trainers/list".formatted(campId, sessionId), "/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        return sessionTrainerListView.render();
    }

    public Event addSessionTrainer(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        AddSessionTrainerView addSessionTrainerView = new AddSessionTrainerView(campId, sessionId);
        Event event = addSessionTrainerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof SessionTrainer sessionTrainerToAdd) {
            try {
                SessionTrainerDataManager sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
                sessionTrainerDataManager.addSessionTrainer(sessionTrainerToAdd);

                SimpleBox sessionTrainerAddedSimpleBox = new SimpleBox();
                sessionTrainerAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Formateur ajouté")));
                sessionTrainerAddedSimpleBox.addLine(TextFormatter.italic("Le formateur a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + sessionTrainerToAdd.getId())));

                System.out.println();
                sessionTrainerAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        return event;
    }

    public Event deleteSessionTrainer(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        SessionDataManager sessionDataManager;
        SessionTrainerDataManager sessionTrainerDataManager;
        try {
            sessionDataManager = DataManagers.get(SessionDataManager.class);
            sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        Session session;
        try {
            session = sessionDataManager.getSessionWithExceptions(sessionId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        List<SessionTrainer> sessionTrainers;
        try {
            sessionTrainers = sessionTrainerDataManager.getSessionSessionTrainers(session);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<SessionTrainer> sortedSessionTrainers;

        try {
            sortedSessionTrainers = this.sortModels(sessionTrainers, SessionTrainer.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        DeleteSessionTrainerView deleteSessionTrainerView = new DeleteSessionTrainerView(campId, sessionId, sortedSessionTrainers, sessionTrainerDataManager);
        Event event = deleteSessionTrainerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer sessionTrainerId) {
            try {
                SessionTrainer sessionTrainerToDelete = sessionTrainerDataManager.getSessionTrainerWithExceptions(sessionTrainerId);
                sessionTrainerDataManager.deleteSessionTrainer(sessionTrainerId);

                SimpleBox sessionTrainerDeletedSimpleBox = new SimpleBox();
                sessionTrainerDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Formateur retiré")));
                sessionTrainerDeletedSimpleBox.addLine(TextFormatter.italic("Le formateur " + TextFormatter.bold(sessionTrainerToDelete.toString()) + " a bien été retiré."));

                System.out.println();
                sessionTrainerDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers".formatted(campId, sessionId));
        }

        return event;
    }

    public Event selectSessionForRegistrations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        SessionDataManager sessionDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Session> campSessions;
        try {
            campSessions = sessionDataManager.getCampSessions(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Session> sortedSessions;

        try {
            sortedSessions = this.sortModels(campSessions, Session.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        SelectSessionView selectSessionView = new SelectSessionView(campId, sortedSessions, sessionDataManager);
        Event event = selectSessionView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer sessionId) {
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        return event;
    }

    public Event manageSessionRegistrations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));
        ManageSessionRegistrationsView manageSessionRegistrationsView = new ManageSessionRegistrationsView(campId, sessionId);
        return manageSessionRegistrationsView.render();
    }

    public Event listSessionRegistrations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        SessionDataManager sessionDataManager;
        SessionRegistrationDataManager sessionRegistrationDataManager;
        try {
            sessionDataManager = DataManagers.get(SessionDataManager.class);
            sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        Session session;
        try {
            session = sessionDataManager.getSessionWithExceptions(sessionId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        List<SessionRegistration> sessionRegistrations;
        try {
            sessionRegistrations = sessionRegistrationDataManager.getSessionSessionRegistrations(session);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<SessionRegistration> sortedSessionRegistrations;

        try {
            sortedSessionRegistrations = this.sortModels(sessionRegistrations, SessionRegistration.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        ModelListView<SessionRegistration> sessionRegistrationListView = new ModelListView<>(SessionRegistration.class, sortedSessionRegistrations, sessionRegistrationDataManager.hasUnsavedChanges(), "/camps/manage/%d/sessions/%d/registrations/list".formatted(campId, sessionId), "/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        return sessionRegistrationListView.render();
    }

    public Event addSessionRegistration(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        AddSessionRegistrationView addSessionRegistrationView = new AddSessionRegistrationView(campId, sessionId);
        Event event = addSessionRegistrationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof SessionRegistration sessionRegistrationToAdd) {
            try {
                SessionRegistrationDataManager sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
                sessionRegistrationDataManager.addSessionRegistration(sessionRegistrationToAdd);

                SimpleBox sessionRegistrationAddedSimpleBox = new SimpleBox();
                sessionRegistrationAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Inscription à la session ajoutée")));
                sessionRegistrationAddedSimpleBox.addLine(TextFormatter.italic("L'inscription a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + sessionRegistrationToAdd.getId())));

                System.out.println();
                sessionRegistrationAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        return event;
    }

    public Event deleteSessionRegistration(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));

        SessionDataManager sessionDataManager;
        SessionRegistrationDataManager sessionRegistrationDataManager;
        try {
            sessionDataManager = DataManagers.get(SessionDataManager.class);
            sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        Session session;
        try {
            session = sessionDataManager.getSessionWithExceptions(sessionId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
        }

        List<SessionRegistration> sessionRegistrations;
        try {
            sessionRegistrations = sessionRegistrationDataManager.getSessionSessionRegistrations(session);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<SessionRegistration> sortedSessionRegistrations;

        try {
            sortedSessionRegistrations = this.sortModels(sessionRegistrations, SessionRegistration.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        DeleteSessionRegistrationView deleteSessionRegistrationView = new DeleteSessionRegistrationView(campId, sessionId, sortedSessionRegistrations, sessionRegistrationDataManager);
        Event event = deleteSessionRegistrationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer sessionRegistrationId) {
            try {
                SessionRegistration sessionRegistrationToDelete = sessionRegistrationDataManager.getSessionRegistrationWithExceptions(sessionRegistrationId);
                sessionRegistrationDataManager.deleteSessionRegistration(sessionRegistrationId);

                SimpleBox sessionRegistrationDeletedSimpleBox = new SimpleBox();
                sessionRegistrationDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Inscription supprimée")));
                sessionRegistrationDeletedSimpleBox.addLine(TextFormatter.italic("L'inscription " + TextFormatter.bold(sessionRegistrationToDelete.toString()) + " a bien été supprimée."));

                System.out.println();
                sessionRegistrationDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations".formatted(campId, sessionId));
        }

        return event;
    }

}
