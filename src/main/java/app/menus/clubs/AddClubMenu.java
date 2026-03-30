package app.menus.clubs;

import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import app.models.managers.*;
import app.utils.ThrowingStringAcceptor;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuStage;

import java.util.Scanner;

public class AddClubMenu extends MenuStage {

    // ─── Utility methods ─── //

    private boolean promptField(Scanner scanner, ThrowingStringAcceptor throwingStringAcceptor) {
        while (true) {
            System.out.print("> ");
            try {
                String input = scanner.nextLine();
                if (input.equals("!q")) {
                    System.out.println();
                    return true;
                }
                throwingStringAcceptor.accept(input);
                System.out.println();
                return false;
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

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
        if (this.promptField(scanner, clubData::setName)) return "clubs.manage";

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
        })) return "clubs.manage";

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
        if (this.promptField(scanner, clubAddressData::setZipCode)) return "clubs.manage";

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
        if (this.promptField(scanner, clubAddressData::setCity)) return "clubs.manage";

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
        if (this.promptField(scanner, clubAddressData::setStreet)) return "clubs.manage";

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
        if (this.promptField(scanner, clubAddressData::setNumber)) return "clubs.manage";

        System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de bôite " + TextFormatter.italic("(optionnel)"));
        if (this.promptField(scanner, clubAddressData::setBoxNumber)) return "clubs.manage";

        System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(optionnel)"));
        if (this.promptField(scanner, clubData::setGoogleMapsLink)) return "clubs.manage";

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
