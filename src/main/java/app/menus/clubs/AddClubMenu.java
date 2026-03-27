package app.menus.clubs;

import app.data_management.managers.*;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.TableFormatter;
import utils.io.helpers.tables.TableFormattingOptions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuStage;

import java.util.Scanner;

public class AddClubMenu extends MenuStage {

    /** Overrides & inheritance **/

    @Override
    public String use() {
        Scanner scanner = new Scanner(System.in);

        Club.Data clubData = new Club.Data();
        Address.Data clubAddressData = new Address.Data();

        TableFormatter sectionHeaderTableFormatter = new TableFormatter();
        sectionHeaderTableFormatter.removeOption(TableFormattingOptions.DISPLAY_HEADER);
        TableFormatter.Column sectionHeaderSingleColumn = new TableFormatter.Column();
        sectionHeaderTableFormatter.addColumn(sectionHeaderSingleColumn);

        sectionHeaderSingleColumn.addValue(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un club")));
        sectionHeaderSingleColumn.addValue(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!q")));

        sectionHeaderTableFormatter.display();

        System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Nom");
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubData.setName(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");
        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)");
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubAddressData.setCountryIso3(input);
                Address.getCountryFromIso3(clubAddressData.getCountryIso3());
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubAddressData.setZipCode(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubAddressData.setCity(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubAddressData.setStreet(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubAddressData.setNumber(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de bôite " + TextFormatter.italic("(optionnel)"));
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubAddressData.setBoxNumber(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(optionnel)"));
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (input.equals("!q")) {
                    System.out.println();
                    return "clubs.manage";
                }

                clubData.setGoogleMapsPositionLink(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        Club club;
        try {
            Address address = DataManagers.get(AddressDataManager.class).addAddress(clubAddressData);
            clubData.setAddressId(address.getId());
            club = DataManagers.get(ClubDataManager.class).addClub(clubData);
        } catch (LoadDataManagerDataException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return "clubs.manage";
        }

        TableFormatter clubAddedTableFormatter = new TableFormatter();
        clubAddedTableFormatter.removeOption(TableFormattingOptions.DISPLAY_HEADER);
        TableFormatter.Column clubAddedSingleColumn = new TableFormatter.Column();
        clubAddedTableFormatter.addColumn(clubAddedSingleColumn);

        clubAddedSingleColumn.addValue(TextFormatter.bold(TextFormatter.magenta("# Club ajouté")));
        clubAddedSingleColumn.addValue(TextFormatter.italic("Le club a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + club.getId())));

        clubAddedTableFormatter.display();

        return "clubs.manage";
    }

}
