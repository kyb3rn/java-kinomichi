package app.views.clubs;

import app.events.Event;
import app.events.ExitProgramEvent;
import app.events.FormResultEvent;
import app.events.GoBackEvent;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import app.models.NotResultForPrimaryKeyException;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.helpers.KinomichiFunctions;
import app.views.View;
import utils.io.commands.CommandResponseException;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.util.Scanner;

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

        try {
            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Nom");
            KinomichiFunctions.promptField(scanner, clubData::setName);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Adresse");
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.1.")) + " Adresse - Pays (ISO 3)");
            KinomichiFunctions.promptField(scanner, input -> {
                clubAddressData.setCountryIso3(input);
                String iso3 = clubAddressData.getCountryIso3();

                CountryDataManager countryDataManager;
                try {
                    countryDataManager = DataManagers.get(CountryDataManager.class);
                } catch (DataManagerException | ModelException e) {
                    throw new DataManagerException("Impossible de vérifier l'ISO3 '%s'".formatted(iso3), e);
                }

                countryDataManager.getCountryWithExceptions(iso3);
            });

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.2.")) + " Adresse - Code postal");
            KinomichiFunctions.promptField(scanner, clubAddressData::setZipCode);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.3.")) + " Adresse - Ville");
            KinomichiFunctions.promptField(scanner, clubAddressData::setCity);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.4.")) + " Adresse - Rue");
            KinomichiFunctions.promptField(scanner, clubAddressData::setStreet);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.5.")) + " Adresse - Numéro");
            KinomichiFunctions.promptField(scanner, clubAddressData::setNumber);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.yellow("2.6.")) + " Adresse - Numéro de boîte " + TextFormatter.italic("(optionnel)"));
            KinomichiFunctions.promptField(scanner, clubAddressData::setBoxNumber);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Lien Google Maps " + TextFormatter.italic("(optionnel)"));
            KinomichiFunctions.promptField(scanner, clubData::setGoogleMapsLink);
        } catch (CommandResponseException commandResponseException) {
            Object response = commandResponseException.getResponse();

            if (response instanceof ExitCommand) {
                return new ExitProgramEvent();
            } else if (response instanceof BackCommand) {
                return new GoBackEvent();
            }

            System.out.println(Functions.styleAsErrorMessage("Il y a eu un problème durant l'exécution du gestionnaire des commandes."));
            return new GoBackEvent();
        }

        return new FormResultEvent<>(new AddClubFormData(clubData, clubAddressData));
    }

}
