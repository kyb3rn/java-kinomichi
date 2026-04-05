package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.*;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.*;
import app.routing.Request;
import app.views.ModelListView;
import app.views.explore.ExploreDataView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;

import java.util.List;
import java.util.LinkedHashMap;

public class ExploreController extends Controller {

    // ─── Utility methods ─── //

    public Event index(Request request) {
        ExploreDataView exploreDataView = new ExploreDataView();
        return exploreDataView.render();
    }

    public Event listCamps(Request request) {
        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des stages n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Camp> sortedCamps;

        try {
            sortedCamps = this.sortModels(campDataManager.getModels(), Camp.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Camp> campListView = new ModelListView<>(Camp.class, sortedCamps, campDataManager.hasUnsavedChanges(), "/explore/camps", "/explore");
        return campListView.render();
    }

    public Event listPersons(Request request) {
        PersonDataManager personDataManager;
        try {
            personDataManager = DataManagers.get(PersonDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des personnes n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Person> sortedPersons;

        try {
            sortedPersons = this.sortModels(personDataManager.getModels(), Person.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Person> personListView = new ModelListView<>(Person.class, sortedPersons, personDataManager.hasUnsavedChanges(), "/explore/persons", "/explore");
        return personListView.render();
    }

    public Event listAffiliations(Request request) {
        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliations n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Affiliation> sortedAffiliations;

        try {
            sortedAffiliations = this.sortModels(affiliationDataManager.getModels(), Affiliation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Affiliation> affiliationListView = new ModelListView<>(Affiliation.class, sortedAffiliations, affiliationDataManager.hasUnsavedChanges(), "/explore/affiliations", "/explore");
        return affiliationListView.render();
    }

    public Event listDinners(Request request) {
        DinnerDataManager dinnerDataManager;
        try {
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des repas n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Dinner> sortedDinners;

        try {
            sortedDinners = this.sortModels(dinnerDataManager.getModels(), Dinner.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Dinner> dinnerListView = new ModelListView<>(Dinner.class, sortedDinners, dinnerDataManager.hasUnsavedChanges(), "/explore/dinners", "/explore");
        return dinnerListView.render();
    }

    public Event listSessions(Request request) {
        SessionDataManager sessionDataManager;
        try {
            sessionDataManager = DataManagers.get(SessionDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des sessions n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Session> sortedSessions;

        try {
            sortedSessions = this.sortModels(sessionDataManager.getModels(), Session.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Session> sessionListView = new ModelListView<>(Session.class, sortedSessions, sessionDataManager.hasUnsavedChanges(), "/explore/sessions", "/explore");
        return sessionListView.render();
    }

    public Event listSessionTrainers(Request request) {
        SessionTrainerDataManager sessionTrainerDataManager;
        try {
            sessionTrainerDataManager = DataManagers.get(SessionTrainerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des formateurs de session n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<SessionTrainer> sortedSessionTrainers;

        try {
            sortedSessionTrainers = this.sortModels(sessionTrainerDataManager.getModels(), SessionTrainer.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<SessionTrainer> sessionTrainerListView = new ModelListView<>(SessionTrainer.class, sortedSessionTrainers, sessionTrainerDataManager.hasUnsavedChanges(), "/explore/session-trainers", "/explore");
        return sessionTrainerListView.render();
    }

    public Event listSessionRegistrations(Request request) {
        SessionRegistrationDataManager sessionRegistrationDataManager;
        try {
            sessionRegistrationDataManager = DataManagers.get(SessionRegistrationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des inscriptions aux sessions n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<SessionRegistration> sortedSessionRegistrations;

        try {
            sortedSessionRegistrations = this.sortModels(sessionRegistrationDataManager.getModels(), SessionRegistration.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<SessionRegistration> sessionRegistrationListView = new ModelListView<>(SessionRegistration.class, sortedSessionRegistrations, sessionRegistrationDataManager.hasUnsavedChanges(), "/explore/session-registrations", "/explore");
        return sessionRegistrationListView.render();
    }

    public Event listInvitations(Request request) {
        InvitationDataManager invitationDataManager;
        try {
            invitationDataManager = DataManagers.get(InvitationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des invitations n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Invitation> sortedInvitations;

        try {
            sortedInvitations = this.sortModels(invitationDataManager.getModels(), Invitation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Invitation> invitationListView = new ModelListView<>(Invitation.class, sortedInvitations, invitationDataManager.hasUnsavedChanges(), "/explore/invitations", "/explore");
        return invitationListView.render();
    }

    public Event listDinnerReservations(Request request) {
        DinnerReservationDataManager dinnerReservationDataManager;
        try {
            dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des réservations de repas n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<DinnerReservation> sortedDinnerReservations;

        try {
            sortedDinnerReservations = this.sortModels(dinnerReservationDataManager.getModels(), DinnerReservation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<DinnerReservation> dinnerReservationListView = new ModelListView<>(DinnerReservation.class, sortedDinnerReservations, dinnerReservationDataManager.hasUnsavedChanges(), "/explore/dinner-reservations", "/explore");
        return dinnerReservationListView.render();
    }

    public Event listLodgings(Request request) {
        LodgingDataManager lodgingDataManager;
        try {
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des hébergements n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Lodging> sortedLodgings;

        try {
            sortedLodgings = this.sortModels(lodgingDataManager.getModels(), Lodging.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Lodging> lodgingListView = new ModelListView<>(Lodging.class, sortedLodgings, lodgingDataManager.hasUnsavedChanges(), "/explore/lodgings", "/explore");
        return lodgingListView.render();
    }

    public Event listLodgingReservations(Request request) {
        LodgingReservationDataManager lodgingReservationDataManager;
        try {
            lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des réservations d'hébergement n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<LodgingReservation> sortedLodgingReservations;

        try {
            sortedLodgingReservations = this.sortModels(lodgingReservationDataManager.getModels(), LodgingReservation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<LodgingReservation> lodgingReservationListView = new ModelListView<>(LodgingReservation.class, sortedLodgingReservations, lodgingReservationDataManager.hasUnsavedChanges(), "/explore/lodging-reservations", "/explore");
        return lodgingReservationListView.render();
    }

    public Event listClubs(Request request) {
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des clubs n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Club> sortedClubs;

        try {
            sortedClubs = this.sortModels(clubDataManager.getModels(), Club.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Club> clubListView = new ModelListView<>(Club.class, sortedClubs, clubDataManager.hasUnsavedChanges(), "/explore/clubs", "/explore");
        return clubListView.render();
    }

    public Event listAddresses(Request request) {
        AddressDataManager addressDataManager;
        try {
            addressDataManager = DataManagers.get(AddressDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des adresses n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Address> sortedAddresses;

        try {
            sortedAddresses = this.sortModels(addressDataManager.getModels(), Address.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Address> addressListView = new ModelListView<>(Address.class, sortedAddresses, addressDataManager.hasUnsavedChanges(), "/explore/addresses", "/explore");
        return addressListView.render();
    }

    public Event listCountries(Request request) {
        CountryDataManager countryDataManager;
        try {
            countryDataManager = DataManagers.get(CountryDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des pays n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Country> sortedCountries;

        try {
            sortedCountries = this.sortModels(countryDataManager.getModels(), Country.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Country> countryListView = new ModelListView<>(Country.class, sortedCountries, countryDataManager.hasUnsavedChanges(), "/explore/countries", "/explore");
        return countryListView.render();
    }

}
