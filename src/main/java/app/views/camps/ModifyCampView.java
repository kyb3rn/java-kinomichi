package app.views.camps;

import app.events.*;
import app.models.Address;
import app.models.Camp;
import app.models.ModelException;
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

public class ModifyCampView extends FormView {

    // ─── Properties ─── //

    private final Camp camp;

    // ─── Constructors ─── //

    public ModifyCampView(Camp camp) {
        this.camp = camp;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Camp clonedCamp = this.camp.clone();
        Address clonedAddress = clonedCamp.getAddress().clone();

        AtomicReference<Instant> timeSlotStart = new AtomicReference<>(clonedCamp.getTimeSlot().getStart());
        AtomicReference<Instant> timeSlotEnd = new AtomicReference<>(clonedCamp.getTimeSlot().getEnd());

        String currentCountryIso3;
        try {
            currentCountryIso3 = clonedAddress.getCountryIso3();
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Erreur interne."));
            return new CallUrlEvent("/camps/select");
        }

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Modification d'un stage")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Stage sélectionné : " + TextFormatter.bold(clonedCamp.toString())));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.NAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Nom " + TextFormatter.italic("(actuel : %s)".formatted(clonedCamp.getName())), clonedCamp::setName));
        fieldHandlers.put(Field.ADDRESS_COUNTRY_ISO3, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3) " + TextFormatter.italic("(actuel : %s)".formatted(currentCountryIso3)), clonedAddress::setCountryFromPk));
        fieldHandlers.put(Field.ADDRESS_ZIP_CODE, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getZipCode())), input -> clonedAddress.setZipCode(Address.verifyZipCode(input))));
        fieldHandlers.put(Field.ADDRESS_CITY, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getCity())), clonedAddress::setCity));
        fieldHandlers.put(Field.ADDRESS_STREET, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getStreet())), clonedAddress::setStreet));
        fieldHandlers.put(Field.ADDRESS_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getNumber())), clonedAddress::setNumber));
        fieldHandlers.put(Field.ADDRESS_BOX_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de boîte " + TextFormatter.italic("(actuel : %s) (optionnel)".formatted(clonedAddress.getBoxNumber() != null ? clonedAddress.getBoxNumber() : "-")), input -> clonedAddress.setBoxNumber(Address.verifyBoxNumber(input))));
        fieldHandlers.put(Field.TIME_SLOT_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Date de début " + TextFormatter.italic("(actuel : %s) (format: yyyy-MM-ddTHH:mm:ssZ)".formatted(clonedCamp.getTimeSlot().getFormattedStart())), input -> {
            Instant start = Camp.verifyTimeSlotStart(input);
            if (timeSlotEnd.get() != null && !start.isBefore(timeSlotEnd.get())) {
                throw new ModelException("La date de début doit être strictement antérieure à la date de fin");
            }
            timeSlotStart.set(start);
            clonedCamp.setTimeSlot(new TimeSlot(start, timeSlotEnd.get()));
        }));
        fieldHandlers.put(Field.TIME_SLOT_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Date de fin " + TextFormatter.italic("(actuel : %s) (format: yyyy-MM-ddTHH:mm:ssZ)".formatted(clonedCamp.getTimeSlot().getFormattedEnd())), input -> {
            Instant end = Camp.verifyTimeSlotEnd(input);
            if (timeSlotStart.get() != null && !end.isAfter(timeSlotStart.get())) {
                throw new ModelException("La date de fin doit être strictement postérieure à la date de début");
            }
            timeSlotEnd.set(end);
            clonedCamp.setTimeSlot(new TimeSlot(timeSlotStart.get(), end));
        }));

        try {
            // Show selected camp + address details
            System.out.println();
            this.displayCampAndAddressTables(clonedCamp, clonedAddress);

            String input;

            SimpleBox campSelectedConfirmationSimpleBox = new SimpleBox();
            campSelectedConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous apporter des modifications à ce stage ?")));
            campSelectedConfirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (continuer), " + TextFormatter.bold("N") + " = Non (annuler)");
            campSelectedConfirmationSimpleBox.display();

            List<String> campSelectionConfirmationValidOptions = new ArrayList<>(List.of("O", "N"));
            AtomicReference<String> formattedInput = new AtomicReference<>();
            KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                formattedInput.set(rawInput.strip().toUpperCase());

                if (!campSelectionConfirmationValidOptions.contains(formattedInput.get())) {
                    throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(rawInput));
                }
            });
            input = formattedInput.get();

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification du stage annulée."));
                return new CallUrlEvent("/camps/select");
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
                editFieldMenu.addOption("Nom", Field.NAME);
                editFieldMenu.addOption("Adresse - Pays (ISO 3)", Field.ADDRESS_COUNTRY_ISO3);
                editFieldMenu.addOption("Adresse - Code postal", Field.ADDRESS_ZIP_CODE);
                editFieldMenu.addOption("Adresse - Ville", Field.ADDRESS_CITY);
                editFieldMenu.addOption("Adresse - Rue", Field.ADDRESS_STREET);
                editFieldMenu.addOption("Adresse - Numéro", Field.ADDRESS_NUMBER);
                editFieldMenu.addOption("Adresse - Numéro de boîte", Field.ADDRESS_BOX_NUMBER);
                editFieldMenu.addOption("Date de début", Field.TIME_SLOT_START);
                editFieldMenu.addOption("Date de fin", Field.TIME_SLOT_END);
                editFieldMenu.addSectionSeparationIndex();
                editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");

                Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                if (editFieldMenuResponse instanceof Field field) {
                    promptField(scanner, fieldHandlers, field);
                }

                // Show updated camp + address details
                System.out.println();
                this.displayCampAndAddressTables(clonedCamp, clonedAddress);
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
                System.out.println(Functions.styleAsErrorMessage("Modification du stage annulée."));
                return new CallUrlEvent("/camps/select");
            }

            // Only O left
            return new FormResultEvent<>(new ModifyCampFormData(clonedCamp, clonedAddress));
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
            return new CallUrlEvent("/");
        } catch (UnimplementedFieldException e) {
            System.out.println(Functions.styleAsErrorMessage("Le champ '%s' n'a pas été implémenté.".formatted(e.getField())));
            return new CallUrlEvent("/");
        }
    }

    // ─── Utility methods ─── //

    private void displayCampAndAddressTables(Camp camp, Address address) {
        Table campTable = getModelTable(camp);
        if (campTable != null) {
            campTable.display();
        }

        Table addressTable = getModelTable(address);
        if (addressTable != null) {
            addressTable.display();
        }
    }

    // ─── Sub classes ─── //

    private enum Field implements FormViewField {
        NAME, ADDRESS_COUNTRY_ISO3, ADDRESS_ZIP_CODE, ADDRESS_CITY, ADDRESS_STREET, ADDRESS_NUMBER, ADDRESS_BOX_NUMBER, TIME_SLOT_START, TIME_SLOT_END
    }

}
