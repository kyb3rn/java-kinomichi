package app.menus.camps;

import app.models.Address;
import app.models.Camp;
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

public class AddCampMenu extends MenuStage {

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

        Camp.Data campData = new Camp.Data();
        Address.Data campAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un stage")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));
        sectionHeaderSimpleBox.display();

        try {
            System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Nom");
            if (this.promptField(scanner, campData::setName)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)");
            if (this.promptField(scanner, input -> {
                campAddressData.setCountryIso3(input);
                String iso3 = campAddressData.getCountryIso3();
                try {
                    DataManagers.get(CountryDataManager.class).getCountryWithExceptions(iso3);
                } catch (DataManagerException | ModelException e) {
                    throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3));
                }
            })) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
            if (this.promptField(scanner, campAddressData::setZipCode)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
            if (this.promptField(scanner, campAddressData::setCity)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
            if (this.promptField(scanner, campAddressData::setStreet)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
            if (this.promptField(scanner, campAddressData::setNumber)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de boîte " + TextFormatter.italic("(optionnel)"));
            if (this.promptField(scanner, campAddressData::setBoxNumber)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Date de début " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"));
            if (this.promptField(scanner, campData::setTimeSlotStart)) return new MenuLeadTo("main");

            System.out.println(TextFormatter.bold(TextFormatter.green("4.")) + " Date de fin " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"));
            if (this.promptField(scanner, campData::setTimeSlotEnd)) return new MenuLeadTo("main");
        } catch (ExitProgramException e) {
            return null;
        }

        Camp camp;
        try {
            Address address = DataManagers.get(AddressDataManager.class).addAddress(campAddressData);
            campData.setAddressId(address.getId());
            camp = DataManagers.get(CampDataManager.class).addCamp(campData);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new MenuLeadTo("main");
        }

        SimpleBox campAddedSimpleBox = new SimpleBox();
        campAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Stage ajouté")));
        campAddedSimpleBox.addLine(TextFormatter.italic("Le stage a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + camp.getId())));
        campAddedSimpleBox.display();

        return new MenuLeadTo("main");
    }

}
