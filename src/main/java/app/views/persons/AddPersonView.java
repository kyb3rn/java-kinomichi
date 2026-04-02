package app.views.persons;

import app.events.Event;
import app.events.ExitProgramEvent;
import app.events.FormResultEvent;
import app.events.GoBackEvent;
import app.models.Affiliated;
import app.models.ModelException;
import app.models.Person;
import app.models.managers.ClubDataManager;
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
import java.util.concurrent.atomic.AtomicBoolean;


public class AddPersonView extends View {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Person person = new Person();
        Affiliated.Data affiliatedData = null;

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'une personne")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        try {
            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("1.")) + " Prénom");
            KinomichiFunctions.promptField(scanner, person::setFirstName);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("2.")) + " Nom");
            KinomichiFunctions.promptField(scanner, person::setLastName);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("3.")) + " Téléphone");
            KinomichiFunctions.promptField(scanner, person::setPhone);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("4.")) + " Email");
            KinomichiFunctions.promptField(scanner, person::setEmail);

            System.out.println();
            System.out.println(TextFormatter.bold(TextFormatter.green("5.")) + " Cette personne est-elle affiliée ? " + TextFormatter.italic("(oui/non)"));
            AtomicBoolean isAffiliated = new AtomicBoolean(false);
            KinomichiFunctions.promptField(scanner, input -> {
                String normalizedInput = input.strip().toLowerCase();
                if (normalizedInput.equals("oui") || normalizedInput.equals("o")) {
                    isAffiliated.set(true);
                } else if (normalizedInput.equals("non") || normalizedInput.equals("n")) {
                    isAffiliated.set(false);
                } else {
                    throw new ModelException("Veuillez répondre par 'oui' ou 'non'");
                }
            });

            if (isAffiliated.get()) {
                Affiliated.Data newAffiliatedData = new Affiliated.Data();
                affiliatedData = newAffiliatedData;

                System.out.println();
                System.out.println(TextFormatter.bold(TextFormatter.yellow("5.1.")) + " Identifiant du club");
                KinomichiFunctions.promptField(scanner, input -> {
                    newAffiliatedData.setClubId(input);
                    int clubId = newAffiliatedData.getClubId();

                    ClubDataManager clubDataManager;
                    try {
                        clubDataManager = DataManagers.get(ClubDataManager.class);
                    } catch (DataManagerException | ModelException e) {
                        throw new DataManagerException("Impossible de vérifier l'identifiant de club '%s'".formatted(input), e);
                    }

                    clubDataManager.getClubWithExceptions(clubId);
                });

                System.out.println();
                System.out.println(TextFormatter.bold(TextFormatter.yellow("5.2.")) + " Numéro d'affiliation");
                KinomichiFunctions.promptField(scanner, newAffiliatedData::setAffiliationNumber);
            }
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

        return new FormResultEvent<>(new AddPersonFormData(person, affiliatedData));
    }

}
