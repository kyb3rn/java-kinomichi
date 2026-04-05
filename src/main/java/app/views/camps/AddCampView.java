package app.views.camps;

import app.events.*;
import app.models.Address;
import app.models.Camp;
import app.models.ModelException;
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

public class AddCampView extends FormView {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Camp camp = new Camp();
        Address address = new Address();
        AtomicReference<Instant> timeSlotStart = new AtomicReference<>();
        AtomicReference<Instant> timeSlotEnd = new AtomicReference<>();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un stage")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.NAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Nom", camp::setName));
        fieldHandlers.put(Field.ADDRESS_COUNTRY_ISO3, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Pays (ISO 3)", address::setCountryFromPk));
        fieldHandlers.put(Field.ADDRESS_ZIP_CODE, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Code postal", input -> address.setZipCode(Address.verifyZipCode(input))));
        fieldHandlers.put(Field.ADDRESS_CITY, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Ville", address::setCity));
        fieldHandlers.put(Field.ADDRESS_STREET, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Rue", address::setStreet));
        fieldHandlers.put(Field.ADDRESS_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Numéro", address::setNumber));
        fieldHandlers.put(Field.ADDRESS_BOX_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Numéro de boîte " + TextFormatter.italic("(optionnel)"), input -> address.setBoxNumber(Address.verifyBoxNumber(input))));
        fieldHandlers.put(Field.TIME_SLOT_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Date de début " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), input -> {
            Instant start = Camp.verifyTimeSlotStart(input);
            if (timeSlotEnd.get() != null && !start.isBefore(timeSlotEnd.get())) {
                throw new ModelException("La date de début doit être strictement antérieure à la date de fin");
            }
            timeSlotStart.set(start);
            if (timeSlotEnd.get() != null) {
                camp.setTimeSlot(new TimeSlot(start, timeSlotEnd.get()));
            }
        }));
        fieldHandlers.put(Field.TIME_SLOT_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Date de fin " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), input -> {
            Instant end = Camp.verifyTimeSlotEnd(input);
            if (timeSlotStart.get() != null && !end.isAfter(timeSlotStart.get())) {
                throw new ModelException("La date de fin doit être strictement postérieure à la date de début");
            }
            timeSlotEnd.set(end);
            if (timeSlotStart.get() != null) {
                camp.setTimeSlot(new TimeSlot(timeSlotStart.get(), end));
            }
        }));

        try {
            promptField(scanner, fieldHandlers, Field.NAME);

            System.out.println();
            System.out.print(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");

            promptField(scanner, fieldHandlers, Field.ADDRESS_COUNTRY_ISO3);
            promptField(scanner, fieldHandlers, Field.ADDRESS_ZIP_CODE);
            promptField(scanner, fieldHandlers, Field.ADDRESS_CITY);
            promptField(scanner, fieldHandlers, Field.ADDRESS_STREET);
            promptField(scanner, fieldHandlers, Field.ADDRESS_NUMBER);
            promptField(scanner, fieldHandlers, Field.ADDRESS_BOX_NUMBER);
            promptField(scanner, fieldHandlers, Field.TIME_SLOT_START);
            promptField(scanner, fieldHandlers, Field.TIME_SLOT_END);

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter ce stage ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            // Treat M
            do {
                System.out.println();
                Table campTable = getModelTable(camp);
                if (campTable != null) {
                    campTable.display();
                }
                Table addressTable = getModelTable(address);
                if (addressTable != null) {
                    addressTable.display();
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

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Ajout d'un stage annulé."));
                return new CallUrlEvent("/");
            }

            // Only O left
            return new FormResultEvent<>(new AddCampFormData(camp, address));
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

    private enum Field implements FormViewField {
        NAME, ADDRESS_COUNTRY_ISO3, ADDRESS_ZIP_CODE, ADDRESS_CITY, ADDRESS_STREET, ADDRESS_NUMBER, ADDRESS_BOX_NUMBER, TIME_SLOT_START, TIME_SLOT_END
    }

}
