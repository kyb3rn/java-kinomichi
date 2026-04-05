package app.views.sessions;

import app.events.*;
import app.models.CampScheduledItem;
import app.models.ModelException;
import app.models.Session;
import app.utils.elements.time.TimeSlot;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.KinomichiStandardMenu;
import app.views.FormView;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.tables.Table;
import utils.io.text_formatting.TextFormatter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class AddSessionView extends FormView {

    // ─── Properties ─── //

    private final int campId;

    // ─── Constructors ─── //

    public AddSessionView(int campId) {
        this.campId = campId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Session session = new Session();
        AtomicReference<Instant> timeSlotStart = new AtomicReference<>();
        AtomicReference<Instant> timeSlotEnd = new AtomicReference<>();

        try {
            session.setCampFromPk(this.campId);
        } catch (Exception e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible de charger le stage #%d : %s".formatted(this.campId, e.getMessage())));
            return new GoBackEvent();
        }

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'une session au stage " + TextFormatter.bold("#%d".formatted(this.campId)))));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.LABEL, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Label de la session", session::setLabel));
        fieldHandlers.put(Field.TIME_SLOT_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Date de début " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), input -> {
            Instant start = Session.verifyTimeSlotStart(input);
            if (timeSlotEnd.get() != null && !start.isBefore(timeSlotEnd.get())) {
                throw new ModelException("La date de début doit être strictement antérieure à la date de fin");
            }
            CampScheduledItem.validateInstantWithinCampBounds(session.getCamp(), start, "La date de début");
            timeSlotStart.set(start);
            if (timeSlotEnd.get() != null) {
                session.setTimeSlot(new TimeSlot(start, timeSlotEnd.get()));
            }
        }));
        fieldHandlers.put(Field.TIME_SLOT_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Date de fin " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), input -> {
            Instant end = Session.verifyTimeSlotEnd(input);
            if (timeSlotStart.get() != null && !end.isAfter(timeSlotStart.get())) {
                throw new ModelException("La date de fin doit être strictement postérieure à la date de début");
            }
            CampScheduledItem.validateInstantWithinCampBounds(session.getCamp(), end, "La date de fin");
            timeSlotEnd.set(end);
            if (timeSlotStart.get() != null) {
                session.setTimeSlot(new TimeSlot(timeSlotStart.get(), end));
            }
        }));

        try {
            promptField(scanner, fieldHandlers, Field.LABEL);
            promptField(scanner, fieldHandlers, Field.TIME_SLOT_START);
            promptField(scanner, fieldHandlers, Field.TIME_SLOT_END);

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter cette session ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            do {
                System.out.println();
                Table sessionTable = getModelTable(session);
                if (sessionTable != null) {
                    sessionTable.display();
                }
                confirmationSimpleBox.display();

                AtomicReference<String> formattedInput = new AtomicReference<>();
                KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                    formattedInput.set(rawInput.strip().toUpperCase());

                    if (!validOptions.contains(formattedInput.get())) {
                        throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O', 'N' ou 'M'".formatted(rawInput));
                    }
                });
                input = formattedInput.get();

                if (input.equals("M")) {
                    KinomichiStandardMenu editFieldMenu = new KinomichiStandardMenu("Modifier un champ", null);
                    editFieldMenu.setShowGoBackOption(false);
                    editFieldMenu.setShowExitOption(false);
                    editFieldMenu.addOption("Label de la session", Field.LABEL);
                    editFieldMenu.addOption("Date de début", Field.TIME_SLOT_START);
                    editFieldMenu.addOption("Date de fin", Field.TIME_SLOT_END);
                    editFieldMenu.addSectionSeparationIndex();
                    editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");
                    editFieldMenu.addOption("Annuler l'ajout", "CANCEL_ADD");

                    Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                    if (editFieldMenuResponse instanceof Field field) {
                        promptField(scanner, fieldHandlers, field);
                    } else if (editFieldMenuResponse instanceof String cancelOption) {
                        if (cancelOption.equals("CANCEL_ADD")) {
                            input = "N";
                        }
                    }
                }
            } while (input.equals("M"));

            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Ajout d'une session annulé."));
                return new GoBackEvent();
            }

            return new FormResultEvent<>(session);
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

    private enum Field implements FormViewField {
        LABEL, TIME_SLOT_START, TIME_SLOT_END
    }

}
