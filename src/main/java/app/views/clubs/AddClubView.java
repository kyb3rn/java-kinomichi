package app.views.clubs;

import app.events.*;
import app.models.Address;
import app.models.Club;
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
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.*;

public class AddClubView extends View {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Club.Data clubData = new Club.Data();
        Address.Data clubAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un club")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<Field, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.NAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Nom", clubData::setName));
        fieldHandlers.put(Field.ADDRESS_COUNTRY_ISO3, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)", input -> {
            clubAddressData.setCountryIso3(input);
            String iso3 = clubAddressData.getCountryIso3();

            CountryDataManager countryDataManager;
            try {
                countryDataManager = DataManagers.get(CountryDataManager.class);
            } catch (DataManagerException | ModelException e) {
                throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3), e);
            }

            countryDataManager.getCountryWithExceptions(iso3);
        }));
        fieldHandlers.put(Field.ADDRESS_ZIP_CODE, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal", clubAddressData::setZipCode));
        fieldHandlers.put(Field.ADDRESS_CITY, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville", clubAddressData::setCity));
        fieldHandlers.put(Field.ADDRESS_STREET, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue", clubAddressData::setStreet));
        fieldHandlers.put(Field.ADDRESS_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro", clubAddressData::setNumber));
        fieldHandlers.put(Field.ADDRESS_BOX_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de boîte " + TextFormatter.italic("(optionnel)"), clubAddressData::setBoxNumber));
        fieldHandlers.put(Field.GOOGLE_MAPS_LINK, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(optionnel)"), clubData::setGoogleMapsLink));

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
            this.promptField(scanner, fieldHandlers, Field.GOOGLE_MAPS_LINK);

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter ce club ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            // Treat M
            do {
                SimpleBox summarySimpleBox = new SimpleBox();
                summarySimpleBox.addLine(TextFormatter.bold("Nom") + " : " + clubData.getName());
                summarySimpleBox.addLine(TextFormatter.bold("Pays (ISO 3)") + " : " + clubAddressData.getCountryIso3());
                summarySimpleBox.addLine(TextFormatter.bold("Code postal") + " : " + clubAddressData.getZipCode());
                summarySimpleBox.addLine(TextFormatter.bold("Ville") + " : " + clubAddressData.getCity());
                summarySimpleBox.addLine(TextFormatter.bold("Rue") + " : " + clubAddressData.getStreet());
                summarySimpleBox.addLine(TextFormatter.bold("Numéro") + " : " + clubAddressData.getNumber());
                summarySimpleBox.addLine(TextFormatter.bold("N° boîte") + " : " + (clubAddressData.getBoxNumber() != null ? clubAddressData.getBoxNumber() : "-"));
                summarySimpleBox.addLine(TextFormatter.bold("Google Maps") + " : " + (clubData.getGoogleMapsLink() != null ? clubData.getGoogleMapsLink() : "-"));
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
                    editFieldMenu.addOption("Lien Google Maps", Field.GOOGLE_MAPS_LINK);
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
                System.out.println(Functions.styleAsErrorMessage("Ajout d'un club annulé."));
                return new CallUrlEvent("/");
            }

            // Only O left
            return new FormResultEvent<>(new AddClubFormData(clubData, clubAddressData));
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
        NAME, ADDRESS_COUNTRY_ISO3, ADDRESS_ZIP_CODE, ADDRESS_CITY, ADDRESS_STREET, ADDRESS_NUMBER, ADDRESS_BOX_NUMBER, GOOGLE_MAPS_LINK
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
