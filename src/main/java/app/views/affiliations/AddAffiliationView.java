package app.views.affiliations;

import app.events.*;
import app.models.Affiliation;
import app.models.ModelException;
import app.utils.elements.time.TimeSlot;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.KinomichiStandardMenu;
import app.views.FormView;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.tables.Table;
import utils.io.text_formatting.TextFormatter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class AddAffiliationView extends FormView {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Affiliation affiliation = new Affiliation();
        AtomicReference<Instant> validityPeriodStart = new AtomicReference<>();
        AtomicReference<Instant> validityPeriodEnd = new AtomicReference<>();

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'une affiliation")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.PERSON_ID, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Identifiant de la personne (#)", input -> affiliation.setPersonFromPk(input)));
        fieldHandlers.put(Field.CLUB_ID, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Identifiant du club (#)", input -> affiliation.setClubFromPk(input)));
        fieldHandlers.put(Field.AFFILIATION_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Numéro d'affiliation " + TextFormatter.italic("(format: 0000-ABCDE)"), affiliation::setAffiliationNumber));
        fieldHandlers.put(Field.VALIDITY_PERIOD_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Début de validité " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), input -> {
            Instant start = Affiliation.verifyValidityPeriodStart(input);
            if (validityPeriodEnd.get() != null && !start.isBefore(validityPeriodEnd.get())) {
                throw new ModelException("La date de début de validité doit être strictement antérieure à la date de fin");
            }
            validityPeriodStart.set(start);
            if (validityPeriodEnd.get() != null) {
                affiliation.setValidityPeriod(new TimeSlot(start, validityPeriodEnd.get()));
            }
        }));
        fieldHandlers.put(Field.VALIDITY_PERIOD_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("5.")) + " Fin de validité " + TextFormatter.italic("(format: yyyy-MM-ddTHH:mm:ssZ)"), input -> {
            Instant end = Affiliation.verifyValidityPeriodEnd(input);
            if (validityPeriodStart.get() != null && !end.isAfter(validityPeriodStart.get())) {
                throw new ModelException("La date de fin de validité doit être strictement postérieure à la date de début");
            }
            validityPeriodEnd.set(end);
            if (validityPeriodStart.get() != null) {
                affiliation.setValidityPeriod(new TimeSlot(validityPeriodStart.get(), end));
            }
        }));

        try {
            promptField(scanner, fieldHandlers, Field.PERSON_ID);
            promptField(scanner, fieldHandlers, Field.CLUB_ID);
            promptField(scanner, fieldHandlers, Field.AFFILIATION_NUMBER);
            promptField(scanner, fieldHandlers, Field.VALIDITY_PERIOD_START);
            promptField(scanner, fieldHandlers, Field.VALIDITY_PERIOD_END);

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter cette affiliation ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            // Treat M
            do {
                System.out.println();
                Table affiliationTable = getModelTable(affiliation);
                if (affiliationTable != null) {
                    affiliationTable.display();
                }
                confirmationSimpleBox.display();

                AtomicReference<String> formattedInput = new AtomicReference<>();
                KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                    formattedInput.set(rawInput.strip().toUpperCase());

                    if (!validOptions.contains(formattedInput.get())) {
                        throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O', 'N' ou 'M'".formatted(rawInput));
                    }
                });
                input = formattedInput.get();

                if (input.equals("M")) {
                    KinomichiStandardMenu editFieldMenu = new KinomichiStandardMenu("Modifier un champ", null);
                    editFieldMenu.setShowGoBackOption(false);
                    editFieldMenu.setShowExitOption(false);
                    editFieldMenu.addOption("Identifiant de la personne (#)", Field.PERSON_ID);
                    editFieldMenu.addOption("Identifiant du club (#)", Field.CLUB_ID);
                    editFieldMenu.addOption("Numéro d'affiliation", Field.AFFILIATION_NUMBER);
                    editFieldMenu.addOption("Début de validité", Field.VALIDITY_PERIOD_START);
                    editFieldMenu.addOption("Fin de validité", Field.VALIDITY_PERIOD_END);
                    editFieldMenu.addSectionSeparationIndex();
                    editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");
                    editFieldMenu.addOption("Annuler l'ajout", "CANCEL_ADD");

                    Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                    if (editFieldMenuResponse instanceof Field field) {
                        promptField(scanner, fieldHandlers, field);
                    } else if (editFieldMenuResponse instanceof String cancelOption) {
                        if (cancelOption.equals("CANCEL_ADD")) {
                            input = "N";
                        }
                    }
                }
            } while (input.equals("M"));

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Ajout d'une affiliation annulé."));
                return new CallUrlEvent("/");
            }

            // Only O left
            return new FormResultEvent<>(affiliation);
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
        PERSON_ID, CLUB_ID, AFFILIATION_NUMBER, VALIDITY_PERIOD_START, VALIDITY_PERIOD_END
    }

}
