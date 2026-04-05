package app.views.sessions;

import app.events.*;
import app.models.CampScheduledItem;
import app.models.ModelException;
import app.models.Session;
import app.utils.elements.time.TimeSlot;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.KinomichiStandardMenu;
import app.views.FormView;
import utils.helpers.Functions;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.tables.SimpleBox;
import utils.io.tables.Table;
import utils.io.text_formatting.TextFormatter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ModifySessionView extends FormView {

    // ─── Properties ─── //

    private final Session session;

    // ─── Constructors ─── //

    public ModifySessionView(Session session) {
        this.session = session;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Session clonedSession = this.session.clone();
        int campId;
        try {
            campId = clonedSession.getCampId();
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Erreur interne."));
            return new GoBackEvent();
        }

        AtomicReference<Instant> timeSlotStart = new AtomicReference<>(clonedSession.getTimeSlot().getStart());
        AtomicReference<Instant> timeSlotEnd = new AtomicReference<>(clonedSession.getTimeSlot().getEnd());

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Modification d'une session")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Session sélectionnée : " + TextFormatter.bold(clonedSession.toString())));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.LABEL, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Label de la session " + TextFormatter.italic("(actuel : %s)".formatted(clonedSession.getLabel())), clonedSession::setLabel));
        fieldHandlers.put(Field.TIME_SLOT_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Date de début " + TextFormatter.italic("(actuel : %s) (format: yyyy-MM-ddTHH:mm:ssZ)".formatted(clonedSession.getTimeSlot().getFormattedStart())), input -> {
            Instant start = Session.verifyTimeSlotStart(input);
            if (timeSlotEnd.get() != null && !start.isBefore(timeSlotEnd.get())) {
                throw new ModelException("La date de début doit être strictement antérieure à la date de fin");
            }
            CampScheduledItem.validateInstantWithinCampBounds(clonedSession.getCamp(), start, "La date de début");
            timeSlotStart.set(start);
            clonedSession.setTimeSlot(new TimeSlot(start, timeSlotEnd.get()));
        }));
        fieldHandlers.put(Field.TIME_SLOT_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Date de fin " + TextFormatter.italic("(actuel : %s) (format: yyyy-MM-ddTHH:mm:ssZ)".formatted(clonedSession.getTimeSlot().getFormattedEnd())), input -> {
            Instant end = Session.verifyTimeSlotEnd(input);
            if (timeSlotStart.get() != null && !end.isAfter(timeSlotStart.get())) {
                throw new ModelException("La date de fin doit être strictement postérieure à la date de début");
            }
            CampScheduledItem.validateInstantWithinCampBounds(clonedSession.getCamp(), end, "La date de fin");
            timeSlotEnd.set(end);
            clonedSession.setTimeSlot(new TimeSlot(timeSlotStart.get(), end));
        }));

        try {
            System.out.println();
            Table sessionTable = getModelTable(clonedSession);
            if (sessionTable != null) {
                sessionTable.display();
            }

            String input;

            SimpleBox sessionSelectedConfirmationSimpleBox = new SimpleBox();
            sessionSelectedConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous apporter des modifications à cette session ?")));
            sessionSelectedConfirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (continuer), " + TextFormatter.bold("N") + " = Non (annuler)");
            sessionSelectedConfirmationSimpleBox.display();

            List<String> sessionSelectionConfirmationValidOptions = new ArrayList<>(List.of("O", "N"));
            AtomicReference<String> formattedInput = new AtomicReference<>();
            KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                formattedInput.set(rawInput.strip().toUpperCase());

                if (!sessionSelectionConfirmationValidOptions.contains(formattedInput.get())) {
                    throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(rawInput));
                }
            });
            input = formattedInput.get();

            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification de la session annulée."));
                return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
            }

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous enregistrer les modifications ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> updateConfirmationValidOptions = new ArrayList<>(List.of("O", "N", "M"));

            do {
                KinomichiStandardMenu editFieldMenu = new KinomichiStandardMenu("Modifier un champ", null);
                editFieldMenu.setShowGoBackOption(false);
                editFieldMenu.setShowExitOption(false);
                editFieldMenu.addOption("Label de la session", Field.LABEL);
                editFieldMenu.addOption("Date de début", Field.TIME_SLOT_START);
                editFieldMenu.addOption("Date de fin", Field.TIME_SLOT_END);
                editFieldMenu.addSectionSeparationIndex();
                editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");

                Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                if (editFieldMenuResponse instanceof Field field) {
                    promptField(scanner, fieldHandlers, field);
                }

                System.out.println();
                Table updatedSessionTable = getModelTable(clonedSession);
                if (updatedSessionTable != null) {
                    updatedSessionTable.display();
                }
                confirmationSimpleBox.display();

                KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                    formattedInput.set(rawInput.strip().toUpperCase());

                    if (!updateConfirmationValidOptions.contains(formattedInput.get())) {
                        throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O', 'N' ou 'M'".formatted(rawInput));
                    }
                });
                input = formattedInput.get();
            } while (input.equals("M"));

            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification de la session annulée."));
                return new CallUrlEvent("/camps/manage/%d/sessions".formatted(campId));
            }

            return new FormResultEvent<>(new ModifySessionFormData(clonedSession));
        } catch (CommandResponseException commandResponseException) {
            Object response = commandResponseException.getResponse();

            if (response instanceof ExitCommand) {
                return new ExitProgramEvent();
            } else if (response instanceof BackCommand) {
                return new GoBackEvent();
            } else if (response instanceof BackBackCommand) {
                return new GoBackBackEvent();
            }

            System.out.println(Functions.styleAsErrorMessage("Il y a eu un problème durant le processus de gestion des commandes."));
            return new GoBackEvent();
        } catch (UnimplementedFieldException e) {
            System.out.println(Functions.styleAsErrorMessage("Le champ '%s' n'a pas été implémenté.".formatted(e.getField())));
            return new GoBackEvent();
        }
    }

    // ─── Sub classes ─── //

    private enum Field implements FormViewField {
        LABEL, TIME_SLOT_START, TIME_SLOT_END
    }

}
