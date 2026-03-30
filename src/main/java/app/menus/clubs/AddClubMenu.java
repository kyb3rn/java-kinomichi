package app.menus.clubs;

import app.data_management.managers.*;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuStage;

import java.util.Scanner;

public class AddClubMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public String use() {
        Scanner scanner = new Scanner(System.in);

        Club.Data clubData = new Club.Data();
        Address.Data clubAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un club")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!q")));
        sectionHeaderSimpleBox.display();

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

                clubData.setGoogleMapsLink(input);
                break;
            } catch (ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }
        System.out.println();

        Club club;
        try {
            Address address = DataManagers.initAndGet(AddressDataManager.class).addAddress(clubAddressData);
            clubData.setAddressId(address.getId());
            club = DataManagers.initAndGet(ClubDataManager.class).addClub(clubData);
        } catch (LoadDataManagerDataException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return "clubs.manage";
        }

        SimpleBox clubAddedSimpleBox = new SimpleBox();
        clubAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Club ajouté")));
        clubAddedSimpleBox.addLine(TextFormatter.italic("Le club a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + club.getId())));
        clubAddedSimpleBox.display();

        return "clubs.manage";
    }

}
