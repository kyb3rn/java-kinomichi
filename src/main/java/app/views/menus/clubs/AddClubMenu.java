package app.views.menus.clubs;

import app.AppState;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import app.models.managers.*;
import app.utils.ExitProgramException;
import app.views.utils.ExitCommandHandlerException;
import app.views.utils.FormMenuStage;
import app.views.utils.GoBackException;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;

import java.util.Scanner;

public class AddClubMenu extends FormMenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        Scanner scanner = new Scanner(System.in);

        Club.Data clubData = new Club.Data();
        Address.Data clubAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un club")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        try {
            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Nom");
            this.promptField(scanner, clubData::setName);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)");
            this.promptField(scanner, input -> {
                clubAddressData.setCountryIso3(input);
                String iso3 = clubAddressData.getCountryIso3();
                try {
                    DataManagers.get(CountryDataManager.class).getCountryWithExceptions(iso3);
                } catch (DataManagerException | ModelException e) {
                    throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3), e);
                }
            });

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
            this.promptField(scanner, clubAddressData::setZipCode);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
            this.promptField(scanner, clubAddressData::setCity);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
            this.promptField(scanner, clubAddressData::setStreet);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
            this.promptField(scanner, clubAddressData::setNumber);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de bôite " + TextFormatter.italic("(optionnel)"));
            this.promptField(scanner, clubAddressData::setBoxNumber);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(optionnel)"));
            this.promptField(scanner, clubData::setGoogleMapsLink);
        } catch (GoBackException _) {
            return AppState.navigationHistory.goBack();
        } catch (ExitCommandHandlerException _) {
            System.out.println(Functions.styleAsErrorMessage("Il y a eu un problème durant l'exécution du gestionnaire des commandes."));
            return new MenuLeadTo("main");
        } catch (ExitProgramException _) {
            return null;
        }

        Club club;
        try {
            Address address = DataManagers.get(AddressDataManager.class).addAddress(clubAddressData);
            clubData.setAddressId(address.getId());
            club = DataManagers.get(ClubDataManager.class).addClub(clubData);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new MenuLeadTo("clubs.manage");
        }

        SimpleBox clubAddedSimpleBox = new SimpleBox();
        clubAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Club ajouté")));
        clubAddedSimpleBox.addLine(TextFormatter.italic("Le club a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + club.getId())));

        System.out.println();
        clubAddedSimpleBox.display();

        return new MenuLeadTo("clubs.manage");
    }

}
