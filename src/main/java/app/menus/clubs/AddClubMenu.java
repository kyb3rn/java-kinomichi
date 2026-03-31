package app.menus.clubs;

import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import app.models.managers.*;
import app.utils.ExitProgramException;
import app.utils.ThrowingStringAcceptor;
import utils.io.commands.*;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

import java.util.Scanner;

public class AddClubMenu extends MenuStage {

    // ─── Utility methods ─── //

    private boolean promptField(Scanner scanner, ThrowingStringAcceptor throwingStringAcceptor) throws ExitProgramException {
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                try {
                    Command command = CommandManager.convertInput(input);
                    switch (command.getCommand()) {
                        case QUIT -> throw new ExitProgramException();
                        case BACK -> {
                            System.out.println();
                            return true;
                        }
                        default -> throw new UnhandledCommandException(command);
                    }
                } catch (NotACommandException _) {
                    throwingStringAcceptor.accept(input);
                    System.out.println();
                    return false;
                } catch (UnknownCommandException e) {
                    System.out.printf(Functions.styleAsErrorMessage("Cette commande n'existe pas.%n%n"));
                } catch (CommandArgumentException e) {
                    System.out.printf(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides.%n%n"));
                } catch (UnhandledCommandException e) {
                    System.out.printf(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici.%n%n"));
                }
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            } catch (ExitProgramException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        Scanner scanner = new Scanner(System.in);

        Club.Data clubData = new Club.Data();
        Address.Data clubAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un club")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));
        sectionHeaderSimpleBox.display();

        try {
            System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Nom");
            if (this.promptField(scanner, clubData::setName)) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)");
            if (this.promptField(scanner, input -> {
                clubAddressData.setCountryIso3(input);
                String iso3 = clubAddressData.getCountryIso3();
                try {
                    DataManagers.initAndGet(CountryDataManager.class).getCountryWithExceptions(iso3);
                } catch (LoadDataManagerDataException e) {
                    throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3));
                }
            })) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
            if (this.promptField(scanner, clubAddressData::setZipCode)) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
            if (this.promptField(scanner, clubAddressData::setCity)) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
            if (this.promptField(scanner, clubAddressData::setStreet)) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
            if (this.promptField(scanner, clubAddressData::setNumber)) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de bôite " + TextFormatter.italic("(optionnel)"));
            if (this.promptField(scanner, clubAddressData::setBoxNumber)) return new MenuLeadTo("clubs.manage");

            System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(optionnel)"));
            if (this.promptField(scanner, clubData::setGoogleMapsLink)) return new MenuLeadTo("clubs.manage");
        } catch (ExitProgramException e) {
            return null;
        }

        Club club;
        try {
            Address address = DataManagers.initAndGet(AddressDataManager.class).addAddress(clubAddressData);
            clubData.setAddressId(address.getId());
            club = DataManagers.initAndGet(ClubDataManager.class).addClub(clubData);
        } catch (ModelException | DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new MenuLeadTo("clubs.manage");
        }

        SimpleBox clubAddedSimpleBox = new SimpleBox();
        clubAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Club ajouté")));
        clubAddedSimpleBox.addLine(TextFormatter.italic("Le club a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + club.getId())));
        clubAddedSimpleBox.display();

        return new MenuLeadTo("clubs.manage");
    }

}
