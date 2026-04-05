package app.views.clubs;

import app.events.*;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ModifyClubView extends FormView {

    // ─── Properties ─── //

    private final Club club;

    // ─── Constructors ─── //

    public ModifyClubView(Club club) {
        this.club = club;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Club clonedClub = this.club.clone();
        Address clonedAddress = clonedClub.getAddress().clone();

        String currentCountryIso3;
        try {
            currentCountryIso3 = clonedAddress.getCountryIso3();
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Erreur interne."));
            return new CallUrlEvent("/clubs/dashboard");
        }

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Modification d'un club")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Club sélectionné : " + TextFormatter.bold(clonedClub.toString())));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.NAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Nom " + TextFormatter.italic("(actuel : %s)".formatted(clonedClub.getName())), clonedClub::setName));
        fieldHandlers.put(Field.ADDRESS_COUNTRY_ISO3, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3) " + TextFormatter.italic("(actuel : %s)".formatted(currentCountryIso3)), clonedAddress::setCountryFromPk));
        fieldHandlers.put(Field.ADDRESS_ZIP_CODE, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getZipCode())), input -> clonedAddress.setZipCode(Address.verifyZipCode(input))));
        fieldHandlers.put(Field.ADDRESS_CITY, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getCity())), clonedAddress::setCity));
        fieldHandlers.put(Field.ADDRESS_STREET, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getStreet())), clonedAddress::setStreet));
        fieldHandlers.put(Field.ADDRESS_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro " + TextFormatter.italic("(actuel : %s)".formatted(clonedAddress.getNumber())), clonedAddress::setNumber));
        fieldHandlers.put(Field.ADDRESS_BOX_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de boîte " + TextFormatter.italic("(actuel : %s) (optionnel)".formatted(clonedAddress.getBoxNumber() != null ? clonedAddress.getBoxNumber() : "-")), input -> clonedAddress.setBoxNumber(Address.verifyBoxNumber(input))));
        fieldHandlers.put(Field.GOOGLE_MAPS_LINK, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(actuel : %s) (optionnel)".formatted(clonedClub.getGoogleMapsLink() != null ? clonedClub.getGoogleMapsLink() : "-")), clonedClub::setGoogleMapsLink));

        try {
            // Show selected club + address details
            System.out.println();
            this.displayClubAndAddressTables(clonedClub, clonedAddress);

            String input;

            SimpleBox clubSelectedConfirmationSimpleBox = new SimpleBox();
            clubSelectedConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous apporter des modifications à ce club ?")));
            clubSelectedConfirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (continuer), " + TextFormatter.bold("N") + " = Non (annuler)");
            clubSelectedConfirmationSimpleBox.display();

            List<String> clubSelectionConfirmationValidOptions = new ArrayList<>(List.of("O", "N"));
            AtomicReference<String> formattedInput = new AtomicReference<>();
            KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                formattedInput.set(rawInput.strip().toUpperCase());

                if (!clubSelectionConfirmationValidOptions.contains(formattedInput.get())) {
                    throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(rawInput));
                }
            });
            input = formattedInput.get();

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification du club annulée."));
                return new CallUrlEvent("/clubs/dashboard");
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
                editFieldMenu.addOption("Lien Google Maps", Field.GOOGLE_MAPS_LINK);
                editFieldMenu.addSectionSeparationIndex();
                editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");

                Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                if (editFieldMenuResponse instanceof Field field) {
                    promptField(scanner, fieldHandlers, field);
                }

                // Show updated club + address details
                System.out.println();
                this.displayClubAndAddressTables(clonedClub, clonedAddress);
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
                System.out.println(Functions.styleAsErrorMessage("Modification du club annulée."));
                return new CallUrlEvent("/clubs/dashboard");
            }

            // Only O left
            return new FormResultEvent<>(new ModifyClubFormData(clonedClub, clonedAddress));
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

    private void displayClubAndAddressTables(Club club, Address address) {
        Table clubTable = getModelTable(club);
        if (clubTable != null) {
            clubTable.display();
        }

        Table addressTable = getModelTable(address);
        if (addressTable != null) {
            addressTable.display();
        }
    }

    private enum Field implements FormViewField {
        NAME, ADDRESS_COUNTRY_ISO3, ADDRESS_ZIP_CODE, ADDRESS_CITY, ADDRESS_STREET, ADDRESS_NUMBER, ADDRESS_BOX_NUMBER, GOOGLE_MAPS_LINK
    }

}
