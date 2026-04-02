package app.views.persons;

import app.events.*;
import app.models.Affiliated;
import app.models.ModelException;
import app.models.Person;
import app.models.formatting.ModelTableFormatter;
import app.models.managers.ClubDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.ThrowingConsumer;
import app.utils.ThrowingConsumerException;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.KinomichiStandardMenu;
import app.views.View;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.tables.Table;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuResponse;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


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

        HashMap<Field, FieldHandler> fieldHandlers = new HashMap<>();
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

        Affiliated.Data newAffiliatedData = new Affiliated.Data();
        fieldHandlers.put(Field.CLUB_ID, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("5.1.")) + " Identifiant du club", input -> {
            newAffiliatedData.setClubId(input);
            int clubId = newAffiliatedData.getClubId();

            ClubDataManager clubDataManager;
            try {
                clubDataManager = DataManagers.get(ClubDataManager.class);
            } catch (DataManagerException | ModelException e) {
                throw new DataManagerException("Impossible de vérifier l'identifiant de club '%s'".formatted(input), e);
            }

            clubDataManager.getClubWithExceptions(clubId);
        }));
        fieldHandlers.put(Field.AFFILIATION_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.yellow("5.2.")) + " Numéro d'affiliation", newAffiliatedData::setAffiliationNumber));

        try {
            this.promptField(scanner, fieldHandlers, Field.FIRSTNAME);
            this.promptField(scanner, fieldHandlers, Field.LASTNAME);
            this.promptField(scanner, fieldHandlers, Field.PHONE);
            this.promptField(scanner, fieldHandlers, Field.EMAIL);
            this.promptField(scanner, fieldHandlers, Field.AFFILIATED_QUESTION);

            if (isAffiliated.get()) {
                this.promptField(scanner, fieldHandlers, Field.CLUB_ID);
                this.promptField(scanner, fieldHandlers, Field.AFFILIATION_NUMBER);

                affiliatedData = newAffiliatedData;
            }

            Table modelTable = ModelTableFormatter.forDetail(person);

            SimpleBox simpleBox = new SimpleBox();
            simpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter cette personne ?")));
            simpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            // Treat M
            do {
                modelTable.display();
                simpleBox.display();

                while (true) {
                    input = scanner.nextLine();

                    if (validOptions.contains(input.toUpperCase())) {
                        input = input.toUpperCase();
                        break;
                    } else {
                        System.out.println(Functions.styleAsErrorMessage("L'entrée '%s' est invalide. Veuillez entrer 'O', 'N' ou 'M'."));
                    }
                }

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

                        this.promptField(scanner, fieldHandlers, field);

                        if (field == Field.AFFILIATED_QUESTION && !wasAffiliated && isAffiliated.get()) {
                            this.promptField(scanner, fieldHandlers, Field.CLUB_ID);
                            this.promptField(scanner, fieldHandlers, Field.AFFILIATION_NUMBER);

                            affiliatedData = newAffiliatedData;
                        } else {
                            affiliatedData = null;
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
            return new FormResultEvent<>(new AddPersonFormData(person, affiliatedData));
        } catch (CommandResponseException commandResponseException) {
            Object response = commandResponseException.getResponse();

            if (response instanceof ExitCommand) {
                return new ExitProgramEvent();
            } else if (response instanceof BackCommand) {
                return new GoBackEvent();
            }

            System.out.println(Functions.styleAsErrorMessage("Il y a eu un problème durant le processus de gestion des commandes."));
            return new CallUrlEvent("/");
        } catch (UnimplementedFieldException e) {
            System.out.println(Functions.styleAsErrorMessage("Le champ '%s' n'a pas été implémenté.".formatted(e.getField())));
            return new CallUrlEvent("/");
        }
    }

    private void promptField(Scanner scanner, HashMap<Field, FieldHandler> fieldHandlers, Field field) throws CommandResponseException, UnimplementedFieldException {
        FieldHandler fieldHandler = fieldHandlers.get(field);
        if (fieldHandler != null) {
            System.out.println();
            System.out.println(fieldHandler.label);
            KinomichiFunctions.promptFieldWithExitAndBackCommands(scanner, fieldHandler.inputConsumer);
        } else {
            throw new UnimplementedFieldException(field);
        }
    }

    private enum Field {
        FIRSTNAME, LASTNAME, PHONE, EMAIL, AFFILIATED_QUESTION, CLUB_ID, AFFILIATION_NUMBER
    }

    private record FieldHandler(String label, ThrowingConsumer<String> inputConsumer) {}

    private static class UnimplementedFieldException extends Exception {

        private final Field field;

        public UnimplementedFieldException(Field field) {
            this.field = field;
        }

        public Field getField() {
            return field;
        }

    }

}
