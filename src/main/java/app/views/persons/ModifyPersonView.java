package app.views.persons;

import app.events.*;
import app.models.Affiliation;
import app.models.Club;
import app.models.ModelException;
import app.models.Person;
import app.models.managers.AffiliationDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.ThrowingConsumerException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class ModifyPersonView extends FormView {

    // ─── Properties ─── //

    private final Person person;

    // ─── Constructors ─── //

    public ModifyPersonView(Person person) {
        this.person = person;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Person clonedPerson = this.person.clone();
        final AtomicReference<Affiliation> resultAffiliation = new AtomicReference<>();
        boolean wasAffiliated;

        try {
            AffiliationDataManager affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
            resultAffiliation.set(affiliationDataManager.getAffiliationWithExceptions(clonedPerson.getId()).clone());
            wasAffiliated = true;
        } catch (DataManagerException | ModelException _) {
            resultAffiliation.set(new Affiliation());
            wasAffiliated = false;
        }

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Modification d'une personne")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Personne sélectionnée : " + TextFormatter.bold(clonedPerson.toString())));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.FIRSTNAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Prénom " + TextFormatter.italic("(actuel : %s)".formatted(clonedPerson.getFirstName())), clonedPerson::setFirstName));
        fieldHandlers.put(Field.LASTNAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Nom " + TextFormatter.italic("(actuel : %s)".formatted(clonedPerson.getLastName())), clonedPerson::setLastName));
        fieldHandlers.put(Field.PHONE, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Téléphone " + TextFormatter.italic("(actuel : %s)".formatted(clonedPerson.getPhone())), clonedPerson::setPhone));
        fieldHandlers.put(Field.EMAIL, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Email " + TextFormatter.italic("(actuel : %s)".formatted(clonedPerson.getEmail())), clonedPerson::setEmail));

        AtomicBoolean isAffiliated = new AtomicBoolean(wasAffiliated);
        fieldHandlers.put(Field.AFFILIATED_QUESTION, new FieldHandler(TextFormatter.bold(TextFormatter.green("5.")) + " Cette personne est-elle affiliée ? " + TextFormatter.italic("(" + TextFormatter.bold("O") + " = Oui, " + TextFormatter.bold("N") + " = Non)") + TextFormatter.italic(" (actuel : %s)".formatted(wasAffiliated ? "Oui" : "Non")), input -> {
            String normalizedInput = input.strip().toUpperCase();
            if (normalizedInput.equals("O")) {
                isAffiliated.set(true);
            } else if (normalizedInput.equals("N")) {
                isAffiliated.set(false);
                resultAffiliation.set(new Affiliation());
            } else {
                throw new ThrowingConsumerException("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'");
            }
        }));

        fieldHandlers.put(Field.CLUB_ID, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("5.1.")) + " Identifiant du club", input -> {
            Club.Data tempClubData = new Club.Data();
            tempClubData.setId(input);

            Club temp = new Club();
            temp.setId(tempClubData.getId());

            resultAffiliation.get().setClubFromPk(temp.getId());
        }));
        fieldHandlers.put(Field.AFFILIATION_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("5.2.")) + " Numéro d'affiliation", resultAffiliation.get()::setAffiliationNumber));

        try {
            Table personModelTable = getModelTable(clonedPerson);

            if (personModelTable == null) {
                return new CallUrlEvent("/");
            }

            Table affiliationModelTable = getModelTable(resultAffiliation.get());

            if (affiliationModelTable == null) {
                return new CallUrlEvent("/");
            }

            // Show selected person details
            System.out.println();
            personModelTable.display();

            if (isAffiliated.get()) {
                affiliationModelTable = getModelTable(resultAffiliation.get());
                if (affiliationModelTable != null) {
                    affiliationModelTable.display();
                }
            }

            String input;

            SimpleBox personSelectedConfirmationSimpleBox = new SimpleBox();
            personSelectedConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous apporter des modifications à cette personne ?")));
            personSelectedConfirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (continuer), " + TextFormatter.bold("N") + " = Non (annuler)");
            personSelectedConfirmationSimpleBox.display();

            List<String> personSelectionConfirmationValidOptions = new ArrayList<>(List.of("O", "N"));
            AtomicReference<String> formattedInput = new AtomicReference<>();
            KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                formattedInput.set(rawInput.strip().toUpperCase());

                if (!personSelectionConfirmationValidOptions.contains(formattedInput.get())) {
                    throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(rawInput));
                }
            });
            input = formattedInput.get();

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification de la personne annulée."));
                return new CallUrlEvent("/persons/dashboard");
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
                editFieldMenu.addOption("Prénom", Field.FIRSTNAME);
                editFieldMenu.addOption("Nom", Field.LASTNAME);
                editFieldMenu.addOption("Téléphone", Field.PHONE);
                editFieldMenu.addOption("Email", Field.EMAIL);
                editFieldMenu.addOption("Affiliée ou non", Field.AFFILIATED_QUESTION);
                if (isAffiliated.get()) {
                    editFieldMenu.addOption("Identifiant du club", Field.CLUB_ID);
                    editFieldMenu.addOption("Numéro d'affiliation", Field.AFFILIATION_NUMBER);
                }
                editFieldMenu.addSectionSeparationIndex();
                editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");

                Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                if (editFieldMenuResponse instanceof Field field) {
                    boolean wasAffiliatedBeforeEdit = isAffiliated.get();

                    promptField(scanner, fieldHandlers, field);

                    if (field == Field.AFFILIATED_QUESTION && !wasAffiliatedBeforeEdit && isAffiliated.get()) {
                        promptField(scanner, fieldHandlers, Field.CLUB_ID);
                        promptField(scanner, fieldHandlers, Field.AFFILIATION_NUMBER);
                    }
                }

                // Show selected person details
                System.out.println();
                personModelTable.display();

                if (isAffiliated.get()) {
                    affiliationModelTable = getModelTable(resultAffiliation.get());
                    if (affiliationModelTable != null) {
                        affiliationModelTable.display();
                    }
                }

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
                System.out.println(Functions.styleAsErrorMessage("Modification de la personne annulée."));
                return new CallUrlEvent("/persons/dashboard");
            }

            // Only O left
            return new FormResultEvent<>(new ModifyPersonFormData(clonedPerson, resultAffiliation.get(), wasAffiliated, isAffiliated.get()));
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

    private enum Field implements FormViewField {
        FIRSTNAME, LASTNAME, PHONE, EMAIL, AFFILIATED_QUESTION, CLUB_ID, AFFILIATION_NUMBER
    }

}
