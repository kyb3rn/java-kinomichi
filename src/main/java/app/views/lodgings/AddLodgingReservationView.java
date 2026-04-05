package app.views.lodgings;

import app.events.*;
import app.models.*;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.LodgingDataManager;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class AddLodgingReservationView extends FormView {

    // ─── Properties ─── //

    private final int campId;
    private final int lodgingId;

    // ─── Constructors ─── //

    public AddLodgingReservationView(int campId, int lodgingId) {
        this.campId = campId;
        this.lodgingId = lodgingId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        LodgingReservation lodgingReservation = new LodgingReservation();

        List<Person> allPersons;
        try {
            DataManagers.get(CampDataManager.class).getCampWithExceptions(this.campId);
            lodgingReservation.setLodgingFromPk(this.lodgingId);
            allPersons = new ArrayList<>(DataManagers.get(PersonDataManager.class).getModels());
        } catch (Exception e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible de charger les données nécessaires : %s".formatted(e.getMessage())));
            return new GoBackEvent();
        }

        SimpleBox sectionHeaderSimpleBox = new SimpleBox();
        sectionHeaderSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Ajout d'une réservation à l'hébergement " + TextFormatter.bold("#%d".formatted(this.lodgingId)) + " du stage " + TextFormatter.bold("#%d".formatted(this.campId)))));
        sectionHeaderSimpleBox.addLine(TextFormatter.italic("Pour annuler à tout moment, entrez la commande " + TextFormatter.bold("!b")));

        System.out.println();
        sectionHeaderSimpleBox.display();

        HashMap<FormViewField, FieldHandler> fieldHandlers = new HashMap<>();
        fieldHandlers.put(Field.PERSON_ID, new FieldHandler(TextFormatter.bold(TextFormatter.green("1.")) + " Identifiant de la personne (#)", lodgingReservation::setPersonFromPk));
        fieldHandlers.put(Field.SINGLE_ROOM_OPTION, new FieldHandler(TextFormatter.bold(TextFormatter.green("2.")) + " Chambre individuelle ? " + TextFormatter.italic("(O = Oui, N = Non)"), input -> {
            String normalizedInput = input.strip().toUpperCase();
            if (!normalizedInput.equals("O") && !normalizedInput.equals("N")) {
                throw new ModelException("L'entrée '%s' est invalide. Veuillez entrer 'O' ou 'N'".formatted(input));
            }
            lodgingReservation.setSingleRoomOption(normalizedInput.equals("O"));
        }));

        try {
            this.displayModelTable(allPersons);
            promptField(scanner, fieldHandlers, Field.PERSON_ID);
            promptField(scanner, fieldHandlers, Field.SINGLE_ROOM_OPTION);

            SimpleBox confirmationSimpleBox = new SimpleBox();
            confirmationSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Voulez-vous ajouter cette réservation ?")));
            confirmationSimpleBox.addLine(TextFormatter.bold("O") + " = Oui (enregistrer), " + TextFormatter.bold("N") + " = Non (annuler), " + TextFormatter.bold("M") + " = Modifier");

            List<String> validOptions = new ArrayList<>(List.of("O", "N", "M"));
            String input;

            do {
                System.out.println();
                Table lodgingReservationTable = getModelTable(lodgingReservation);
                if (lodgingReservationTable != null) {
                    lodgingReservationTable.display();
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
                    editFieldMenu.addOption("Chambre individuelle", Field.SINGLE_ROOM_OPTION);
                    editFieldMenu.addSectionSeparationIndex();
                    editFieldMenu.addOption("Annuler la modification", "CANCEL_UPDATE");
                    editFieldMenu.addOption("Annuler l'ajout", "CANCEL_ADD");

                    Object editFieldMenuResponse = editFieldMenu.use().getResponse();
                    if (editFieldMenuResponse instanceof Field field) {
                        if (field == Field.PERSON_ID) {
                            this.displayModelTable(allPersons);
                        }
                        promptField(scanner, fieldHandlers, field);
                    } else if (editFieldMenuResponse instanceof String cancelOption) {
                        if (cancelOption.equals("CANCEL_ADD")) {
                            input = "N";
                        }
                    }
                }
            } while (input.equals("M"));

            if (input.equals("N")) {
                System.out.println(Functions.styleAsErrorMessage("Ajout d'une réservation annulé."));
                return new GoBackEvent();
            }

            return new FormResultEvent<>(lodgingReservation);
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
            return new GoBackEvent();
        } catch (UnimplementedFieldException e) {
            System.out.println(Functions.styleAsErrorMessage("Le champ '%s' n'a pas été implémenté.".formatted(e.getField())));
            return new GoBackEvent();
        }
    }

    // ─── Utility methods ─── //

    private <M extends Model> void displayModelTable(List<M> models) {
        try {
            Table table = ModelTableFormatter.forList(models);
            System.out.println();
            table.display();
        } catch (EmptyContentModelTableFormatterException e) {
            System.out.println(Functions.styleAsErrorMessage("Aucune donnée à afficher."));
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table : aucun ModelTable n'est défini pour ce modèle"));
        } catch (ModelTableInstanciationException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table : l'instanciation du ModelTable a échoué"));
        }
    }

    // ─── Sub classes ─── //

    private enum Field implements FormViewField {
        PERSON_ID, SINGLE_ROOM_OPTION
    }

}
