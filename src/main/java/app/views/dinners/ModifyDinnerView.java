package app.views.dinners;

import app.events.*;
import app.models.CampScheduledItem;
import app.models.Dinner;
import app.models.ModelException;
import app.utils.elements.money.Currency;
import app.utils.elements.money.Price;
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

public class ModifyDinnerView extends FormView {

    // ─── Properties ─── //

    private final Dinner dinner;

    // ─── Constructors ─── //

    public ModifyDinnerView(Dinner dinner) {
        this.dinner = dinner;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Dinner clonedDinner = this.dinner.clone();
        int campId;
        try {
            campId = clonedDinner.getCampId();
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Erreur interne."));
            return new GoBackEvent();
        }

        AtomicReference<Instant> timeSlotStart = new AtomicReference<>(clonedDinner.getTimeSlot().getStart());
        AtomicReference<Instant> timeSlotEnd = new AtomicReference<>(clonedDinner.getTimeSlot().getEnd());

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Modification d'un repas")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Repas sélectionné : " + TextFormatter.bold(clonedDinner.toString())));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.LABEL, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Nom du repas " + TextFormatter.italic("(actuel : %s)".formatted(clonedDinner.getLabel())), clonedDinner::setLabel));
        fieldHandlers.put(Field.TIME_SLOT_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Date de début " + TextFormatter.italic("(actuel : %s) (format: yyyy-MM-ddTHH:mm:ssZ)".formatted(clonedDinner.getTimeSlot().getFormattedStart())), input -> {
            Instant start = Dinner.verifyTimeSlotStart(input);
            if (timeSlotEnd.get() != null && !start.isBefore(timeSlotEnd.get())) {
                throw new ModelException("La date de début doit être strictement antérieure à la date de fin");
            }
            CampScheduledItem.validateInstantWithinCampBounds(clonedDinner.getCamp(), start, "La date de début");
            timeSlotStart.set(start);
            clonedDinner.setTimeSlot(new TimeSlot(start, timeSlotEnd.get()));
        }));
        fieldHandlers.put(Field.TIME_SLOT_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Date de fin " + TextFormatter.italic("(actuel : %s) (format: yyyy-MM-ddTHH:mm:ssZ)".formatted(clonedDinner.getTimeSlot().getFormattedEnd())), input -> {
            Instant end = Dinner.verifyTimeSlotEnd(input);
            if (timeSlotStart.get() != null && !end.isAfter(timeSlotStart.get())) {
                throw new ModelException("La date de fin doit être strictement postérieure à la date de début");
            }
            CampScheduledItem.validateInstantWithinCampBounds(clonedDinner.getCamp(), end, "La date de fin");
            timeSlotEnd.set(end);
            clonedDinner.setTimeSlot(new TimeSlot(timeSlotStart.get(), end));
        }));
        fieldHandlers.put(Field.PRICE_AMOUNT, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Prix " + TextFormatter.italic("(actuel : %s) (en €)".formatted(clonedDinner.getPrice().getAmount())), input -> {
            double priceAmount = Dinner.verifyPriceAmount(input);
            clonedDinner.setPrice(new Price(Currency.EURO, priceAmount));
        }));

        try {
            // Show selected dinner details
            System.out.println();
            Table dinnerTable = getModelTable(clonedDinner);
            if (dinnerTable != null) {
                dinnerTable.display();
            }

            String input;

            SimpleBox dinnerSelectedConfirmationSimpleBox = new SimpleBox();
            dinnerSelectedConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous apporter des modifications à ce repas ?")));
            dinnerSelectedConfirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (continuer), " + TextFormatter.bold("N") + " = Non (annuler)");
            dinnerSelectedConfirmationSimpleBox.display();

            List<String> dinnerSelectionConfirmationValidOptions = new ArrayList<>(List.of("O", "N"));
            AtomicReference<String> formattedInput = new AtomicReference<>();
            KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                formattedInput.set(rawInput.strip().toUpperCase());

                if (!dinnerSelectionConfirmationValidOptions.contains(formattedInput.get())) {
                    throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(rawInput));
                }
            });
            input = formattedInput.get();

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification du repas annulée."));
                return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
            }

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous enregistrer les modifications ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> updateConfirmationValidOptions = new ArrayList<>(List.of("O", "N", "M"));

            // Treat M
            do {
                KinomichiStandardMenu editFieldMenu = new KinomichiStandardMenu("Modifier un champ", null);
                editFieldMenu.setShowGoBackOption(false);
                editFieldMenu.setShowExitOption(false);
                editFieldMenu.addOption("Nom du repas", Field.LABEL);
                editFieldMenu.addOption("Date de début", Field.TIME_SLOT_START);
                editFieldMenu.addOption("Date de fin", Field.TIME_SLOT_END);
                editFieldMenu.addOption("Prix", Field.PRICE_AMOUNT);
                editFieldMenu.addSectionSeparationIndex();
                editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");

                Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                if (editFieldMenuResponse instanceof Field field) {
                    promptField(scanner, fieldHandlers, field);
                }

                // Show updated dinner details
                System.out.println();
                Table updatedDinnerTable = getModelTable(clonedDinner);
                if (updatedDinnerTable != null) {
                    updatedDinnerTable.display();
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

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification du repas annulée."));
                return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
            }

            // Only O left
            return new FormResultEvent<>(new ModifyDinnerFormData(clonedDinner));
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
        LABEL, TIME_SLOT_START, TIME_SLOT_END, PRICE_AMOUNT
    }

}
