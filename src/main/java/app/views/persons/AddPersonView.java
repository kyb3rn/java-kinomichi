package app.views.persons;

import app.events.*;
import app.models.*;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.utils.ThrowingConsumer;
import app.utils.ThrowingConsumerException;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.KinomichiStandardMenu;
import app.views.FormView;
import app.views.View;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.tables.Table;
import utils.io.text_formatting.TextFormatter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class AddPersonView extends FormView {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Person person = new Person();
        Affiliation affiliation = new Affiliation();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'une personne")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.FIRSTNAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Prénom", person::setFirstName));
        fieldHandlers.put(Field.LASTNAME, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Nom", person::setLastName));
        fieldHandlers.put(Field.PHONE, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Téléphone", person::setPhone));
        fieldHandlers.put(Field.EMAIL, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Email", person::setEmail));

        AtomicBoolean isAffiliated = new AtomicBoolean(false);
        fieldHandlers.put(Field.AFFILIATED_QUESTION, new FieldHandler(TextFormatter.bold(TextFormatter.green("5.")) + " Cette personne est-elle affiliée ? " + TextFormatter.italic("(" + TextFormatter.bold("O") + " = Oui, " + TextFormatter.bold("N") + " = Non)"), input -> {
            String normalizedInput = input.strip().toUpperCase();
            if (normalizedInput.equals("O")) {
                isAffiliated.set(true);
            } else if (normalizedInput.equals("N")) {
                isAffiliated.set(false);
            } else {
                throw new ThrowingConsumerException("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'");
            }
        }));

        Affiliation newAffiliation = new Affiliation();
        fieldHandlers.put(Field.CLUB_ID, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("5.1.")) + " Identifiant du club", input -> {
            Club.Data tempClubData = new Club.Data();
            tempClubData.setId(input);

            Club temp = new Club();
            temp.setId(tempClubData.getId());

            newAffiliation.setClubFromPk(temp.getId());
        }));
        fieldHandlers.put(Field.AFFILIATION_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("5.2.")) + " Numéro d'affiliation", newAffiliation::setAffiliationNumber));

        try {
            promptField(scanner, fieldHandlers, Field.FIRSTNAME);
            promptField(scanner, fieldHandlers, Field.LASTNAME);
            promptField(scanner, fieldHandlers, Field.PHONE);
            promptField(scanner, fieldHandlers, Field.EMAIL);
            promptField(scanner, fieldHandlers, Field.AFFILIATED_QUESTION);

            if (isAffiliated.get()) {
                promptField(scanner, fieldHandlers, Field.CLUB_ID);
                promptField(scanner, fieldHandlers, Field.AFFILIATION_NUMBER);

                affiliation = newAffiliation;
            }

            Table personModelTable = getModelTable(person);

            if (personModelTable == null) {
                return new CallUrlEvent("/");
            }

            Table affiliationModelTable = getModelTable(affiliation);

            if (affiliationModelTable == null) {
                return new CallUrlEvent("/");
            }

            SimpleBox simpleBox = new SimpleBox();
            simpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter cette personne ?")));
            simpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            // Treat M
            do {
                System.out.println();
                personModelTable.display();

                if (isAffiliated.get()) {
                    affiliationModelTable.display();
                }

                simpleBox.display();

                AtomicReference<String> formattedInput = new AtomicReference<>();
                KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                    formattedInput.set(rawInput.strip().toUpperCase());

                    if (!validOptions.contains(formattedInput.get())) {
                        throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O', 'N' ou 'M'".formatted(rawInput));
                    }
                });
                input = formattedInput.get();

                if (input.equals("M")) {
                    KinomichiStandardMenu editFieldMenu = new KinomichiStandardMenu("Modifier un champs", null);
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
                    editFieldMenu.addOption("Annuler l'ajout", "CANCEL_ADD");

                    Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                    if (editFieldMenuResponse instanceof Field field) {
                        boolean wasAffiliated = isAffiliated.get();

                        promptField(scanner, fieldHandlers, field);

                        if (field == Field.AFFILIATED_QUESTION && !wasAffiliated && isAffiliated.get()) {
                            promptField(scanner, fieldHandlers, Field.CLUB_ID);
                            promptField(scanner, fieldHandlers, Field.AFFILIATION_NUMBER);

                            affiliation = newAffiliation;
                        } else {
                            affiliation = null;
                        }
                    } else if (editFieldMenuResponse instanceof String cancelOption) {
                        if (cancelOption.equals("CANCEL_ADD")) {
                            input = "N";
                        }
                    }
                }
            } while (input.equals("M"));

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Ajout d'une personne annulé."));
                return new CallUrlEvent("/");
            }

            // Only O left
            return new FormResultEvent<>(new AddPersonFormData(person, affiliation));
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
