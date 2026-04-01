package app.views.menus.camps;

import app.AppState;
import app.models.Address;
import app.models.Camp;
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

public class AddCampMenu extends FormMenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        Scanner scanner = new Scanner(System.in);

        Camp.Data campData = new Camp.Data();
        Address.Data campAddressData = new Address.Data();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'un stage")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        try {
            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Nom");
            this.promptField(scanner, campData::setName);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)");
            this.promptField(scanner, input -> {
                campAddressData.setCountryIso3(input);
                String iso3 = campAddressData.getCountryIso3();
                try {
                    DataManagers.get(CountryDataManager.class).getCountryWithExceptions(iso3);
                } catch (DataManagerException | ModelException e) {
                    throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3), e);
                }
            });

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
            this.promptField(scanner, campAddressData::setZipCode);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
            this.promptField(scanner, campAddressData::setCity);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
            this.promptField(scanner, campAddressData::setStreet);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
            this.promptField(scanner, campAddressData::setNumber);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de boîte " + TextFormatter.italic("(optionnel)"));
            this.promptField(scanner, campAddressData::setBoxNumber);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Date de début " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"));
            this.promptField(scanner, campData::setTimeSlotStart);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("4.")) + " Date de fin " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"));
            this.promptField(scanner, campData::setTimeSlotEnd);
        } catch (GoBackException _) {
            return AppState.navigationHistory.goBack();
        } catch (ExitCommandHandlerException _) {
            System.out.println(Functions.styleAsErrorMessage("Il y a eu un problème durant l'exécution du gestionnaire des commandes."));
            return new MenuLeadTo("main");
        } catch (ExitProgramException _) {
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

        System.out.println();
        campAddedSimpleBox.display();

        return new MenuLeadTo("main");
    }

}
