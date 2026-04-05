package app.views.affiliations;

import app.events.*;
import app.models.Affiliation;
import app.models.ModelException;
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
import java.util.concurrent.atomic.AtomicReference;

public class ModifyAffiliationView extends FormView {

    // ─── Properties ─── //

    private final Affiliation affiliation;

    // ─── Constructors ─── //

    public ModifyAffiliationView(Affiliation affiliation) {
        this.affiliation = affiliation;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        Affiliation clonedAffiliation = this.affiliation.clone();
        int originalPersonId;
        try {
            originalPersonId = this.affiliation.getPersonId();
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new GoBackEvent();
        }

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Modification d'une affiliation")));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Affiliation sélectionnée : " + TextFormatter.bold(clonedAffiliation.toString())));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        String currentPersonIdLabel;
        String currentClubIdLabel;
        try {
            currentPersonIdLabel = String.valueOf(clonedAffiliation.getPersonId());
        } catch (ModelException e) {
            currentPersonIdLabel = "?";
        }
        try {
            currentClubIdLabel = String.valueOf(clonedAffiliation.getClubId());
        } catch (ModelException e) {
            currentClubIdLabel = "?";
        }

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.PERSON_ID, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Identifiant de la personne (#) " + TextFormatter.italic("(actuel : %s)".formatted(currentPersonIdLabel)), input -> clonedAffiliation.setPersonFromPk(Integer.parseInt(input))));
        fieldHandlers.put(Field.CLUB_ID, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Identifiant du club (#) " + TextFormatter.italic("(actuel : %s)".formatted(currentClubIdLabel)), input -> clonedAffiliation.setClubFromPk(Integer.parseInt(input))));
        fieldHandlers.put(Field.AFFILIATION_NUMBER, new FieldHandler(TextFormatter.bold(TextFormatter.green("3.")) + " Numéro d'affiliation " + TextFormatter.italic("(actuel : %s)".formatted(clonedAffiliation.getAffiliationNumber())), clonedAffiliation::setAffiliationNumber));
        fieldHandlers.put(Field.VALIDITY_PERIOD_START, new FieldHandler(TextFormatter.bold(TextFormatter.green("4.")) + " Début de validité " + TextFormatter.italic("(actuel : %s)".formatted(clonedAffiliation.getValidityPeriod().getFormattedStart())), input -> {
            clonedAffiliation.setValidityPeriod(new app.utils.elements.time.TimeSlot(
                    Affiliation.verifyValidityPeriodStart(input),
                    clonedAffiliation.getValidityPeriod().getEnd()
            ));
        }));
        fieldHandlers.put(Field.VALIDITY_PERIOD_END, new FieldHandler(TextFormatter.bold(TextFormatter.green("5.")) + " Fin de validité " + TextFormatter.italic("(actuel : %s)".formatted(clonedAffiliation.getValidityPeriod().getFormattedEnd())), input -> {
            clonedAffiliation.setValidityPeriod(new app.utils.elements.time.TimeSlot(
                    clonedAffiliation.getValidityPeriod().getStart(),
                    Affiliation.verifyValidityPeriodEnd(input)
            ));
        }));

        try {
            Table affiliationModelTable = getModelTable(clonedAffiliation);

            if (affiliationModelTable == null) {
                return new CallUrlEvent("/");
            }

            // Show selected affiliation details
            System.out.println();
            affiliationModelTable.display();

            String input;

            SimpleBox affiliationSelectedConfirmationSimpleBox = new SimpleBox();
            affiliationSelectedConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous apporter des modifications à cette affiliation ?")));
            affiliationSelectedConfirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (continuer), " + TextFormatter.bold("N") + " = Non (annuler)");
            affiliationSelectedConfirmationSimpleBox.display();

            List<String> affiliationSelectionConfirmationValidOptions = new ArrayList<>(List.of("O", "N"));
            AtomicReference<String> formattedInput = new AtomicReference<>();
            KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                formattedInput.set(rawInput.strip().toUpperCase());

                if (!affiliationSelectionConfirmationValidOptions.contains(formattedInput.get())) {
                    throw new Exception("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(rawInput));
                }
            });
            input = formattedInput.get();

            // Treat N
            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Modification de l'affiliation annulée."));
                return new CallUrlEvent("/affiliations/dashboard");
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
                editFieldMenu.addOption("Identifiant de la personne (#)", Field.PERSON_ID);
                editFieldMenu.addOption("Identifiant du club (#)", Field.CLUB_ID);
                editFieldMenu.addOption("Numéro d'affiliation", Field.AFFILIATION_NUMBER);
                editFieldMenu.addOption("Début de validité", Field.VALIDITY_PERIOD_START);
                editFieldMenu.addOption("Fin de validité", Field.VALIDITY_PERIOD_END);
                editFieldMenu.addSectionSeparationIndex();
                editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");

                Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                if (editFieldMenuResponse instanceof Field field) {
                    if (field == Field.PERSON_ID) {
                        promptField(scanner, fieldHandlers, field);

                        // If person changed, ask for confirmation
                        try {
                            int newPersonId = clonedAffiliation.getPersonId();
                            if (newPersonId != originalPersonId) {
                                SimpleBox personChangeConfirmationSimpleBox = new SimpleBox();
                                personChangeConfirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Confirmation de changement de personne")));
                                personChangeConfirmationSimpleBox.addLine("La personne référencée va changer de " + TextFormatter.bold("#" + originalPersonId) + " à " + TextFormatter.bold("#" + newPersonId) + ".");
                                personChangeConfirmationSimpleBox.addLine(TextFormatter.italic("(" + TextFormatter.bold("O") + " = Oui, " + TextFormatter.bold("N") + " = Non)"));
                                personChangeConfirmationSimpleBox.display();

                                KinomichiFunctions.promptInputWithDefaultCommandHandling(scanner, rawInput -> {
                                    String normalizedInput = rawInput.strip().toUpperCase();

                                    if (!normalizedInput.equals("O") && !normalizedInput.equals("N")) {
                                        throw new Exception("Veuillez répondre par 'O' (oui) ou 'N' (non).");
                                    }

                                    if (normalizedInput.equals("N")) {
                                        clonedAffiliation.setPersonFromPk(originalPersonId);
                                        System.out.println(TextFormatter.italic("Changement de personne annulé."));
                                    }
                                });
                            }
                        } catch (ModelException e) {
                            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
                        }
                    } else {
                        promptField(scanner, fieldHandlers, field);
                    }
                }

                // Show updated affiliation details
                System.out.println();
                affiliationModelTable = getModelTable(clonedAffiliation);
                if (affiliationModelTable != null) {
                    affiliationModelTable.display();
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
                System.out.println(Functions.styleAsErrorMessage("Modification de l'affiliation annulée."));
                return new CallUrlEvent("/affiliations/dashboard");
            }

            // Only O left
            return new FormResultEvent<>(clonedAffiliation);
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
