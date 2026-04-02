package app.views.camps;

import app.events.*;
import app.models.Address;
import app.models.Camp;
import app.models.ModelException;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.ThrowingConsumer;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.KinomichiStandardMenu;
import app.views.View;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.util.*;

public class AddCampView extends View {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Camp.Data campData = new Camp.Data();
        Address.Data campAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un stage")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<Field, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.NAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Nom", campData::setName));
        fieldHandlers.put(Field.ADDRESS_COUNTRY_ISO3, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Pays (ISO 3)", input -> {
            campAddressData.setCountryIso3(input);
            String iso3 = campAddressData.getCountryIso3();

            CountryDataManager countryDataManager;
            try {
                countryDataManager = DataManagers.get(CountryDataManager.class);
            } catch (DataManagerException | ModelException e) {
                throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3), e);
            }

            countryDataManager.getCountryWithExceptions(iso3);
        }));
        fieldHandlers.put(Field.ADDRESS_ZIP_CODE, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Code postal", campAddressData::setZipCode));
        fieldHandlers.put(Field.ADDRESS_CITY, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Ville", campAddressData::setCity));
        fieldHandlers.put(Field.ADDRESS_STREET, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Rue", campAddressData::setStreet));
        fieldHandlers.put(Field.ADDRESS_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Numéro", campAddressData::setNumber));
        fieldHandlers.put(Field.ADDRESS_BOX_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Numéro de boîte " + TextFormatter.italic("(optionnel)"), campAddressData::setBoxNumber));
        fieldHandlers.put(Field.TIME_SLOT_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Date de début " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), campData::setTimeSlotStart));
        fieldHandlers.put(Field.TIME_SLOT_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Date de fin " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), campData::setTimeSlotEnd));

        try {
            this.promptField(scanner, fieldHandlers, Field.NAME);

            System.out.println();
            System.out.print(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");

            this.promptField(scanner, fieldHandlers, Field.ADDRESS_COUNTRY_ISO3);
            this.promptField(scanner, fieldHandlers, Field.ADDRESS_ZIP_CODE);
            this.promptField(scanner, fieldHandlers, Field.ADDRESS_CITY);
            this.promptField(scanner, fieldHandlers, Field.ADDRESS_STREET);
            this.promptField(scanner, fieldHandlers, Field.ADDRESS_NUMBER);
            this.promptField(scanner, fieldHandlers, Field.ADDRESS_BOX_NUMBER);
            this.promptField(scanner, fieldHandlers, Field.TIME_SLOT_START);
            this.promptField(scanner, fieldHandlers, Field.TIME_SLOT_END);

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter ce stage ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            // Treat M
            do {
                SimpleBox summarySimpleBox = new SimpleBox();
                summarySimpleBox.addLine(TextFormatter.bold("Nom") + " : " + campData.getName());
                summarySimpleBox.addLine(TextFormatter.bold("Pays (ISO 3)") + " : " + campAddressData.getCountryIso3());
                summarySimpleBox.addLine(TextFormatter.bold("Code postal") + " : " + campAddressData.getZipCode());
                summarySimpleBox.addLine(TextFormatter.bold("Ville") + " : " + campAddressData.getCity());
                summarySimpleBox.addLine(TextFormatter.bold("Rue") + " : " + campAddressData.getStreet());
                summarySimpleBox.addLine(TextFormatter.bold("Numéro") + " : " + campAddressData.getNumber());
                summarySimpleBox.addLine(TextFormatter.bold("N° boîte") + " : " + (campAddressData.getBoxNumber() != null ? campAddressData.getBoxNumber() : "-"));
                summarySimpleBox.addLine(TextFormatter.bold("Date de début") + " : " + campData.getTimeSlotStart());
                summarySimpleBox.addLine(TextFormatter.bold("Date de fin") + " : " + campData.getTimeSlotEnd());
                summarySimpleBox.display();
                confirmationSimpleBox.display();

                while (true) {
                    input = scanner.nextLine();

                    if (validOptions.contains(input.toUpperCase())) {
                        input = input.toUpperCase();
                        break;
                    } else {
                        System.out.println(Functions.styleAsErrorMessage("L'entrée '%s' est invalide. Veuillez entrer 'O', 'N' ou 'M'."));
                    }
                }

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
                        this.promptField(scanner, fieldHandlers, field);
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
            return new FormResultEvent<>(new AddCampFormData(campData, campAddressData));
        } catch (CommandResponseException commandResponseException) {
            Object response = commandResponseException.getResponse();

            if (response instanceof ExitCommand) {
                return new ExitProgramEvent();
            } else if (response instanceof BackCommand) {
                return new GoBackEvent();
            }

            System.out.println(Functions.styleAsErrorMessage("Il y a eu un problème durant le processus de gestion des commandes."));
            return new CallUrlEvent("/");
        } catch (UnimplementedFieldException e) {
            System.out.println(Functions.styleAsErrorMessage("Le champ '%s' n'a pas été implémenté.".formatted(e.getField())));
            return new CallUrlEvent("/");
        }
    }

    private void promptField(Scanner scanner, HashMap<Field, FieldHandler> fieldHandlers, Field field) throws CommandResponseException, UnimplementedFieldException {
        FieldHandler fieldHandler = fieldHandlers.get(field);
        if (fieldHandler != null) {
            System.out.println();
            System.out.println(fieldHandler.label);
            KinomichiFunctions.promptFieldWithExitAndBackCommands(scanner, fieldHandler.inputConsumer);
        } else {
            throw new UnimplementedFieldException(field);
        }
    }

    private enum Field {
        NAME, ADDRESS_COUNTRY_ISO3, ADDRESS_ZIP_CODE, ADDRESS_CITY, ADDRESS_STREET, ADDRESS_NUMBER, ADDRESS_BOX_NUMBER, TIME_SLOT_START, TIME_SLOT_END
    }

    private record FieldHandler(String label, ThrowingConsumer<String> inputConsumer) {}

    private static class UnimplementedFieldException extends Exception {

        private final Field field;

        public UnimplementedFieldException(Field field) {
            this.field = field;
        }

        public Field getField() {
            return field;
        }

    }

}
