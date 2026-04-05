package app.views.clubs;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.ExitProgramEvent;
import app.events.FormResultEvent;
import app.events.GoBackBackEvent;
import app.events.GoBackEvent;
import app.models.Club;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.ClubDataManager;
import app.utils.helpers.KinomichiFunctions;
import app.utils.menus.InvalidMenuInputException;
import app.views.View;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.commands.exceptions.UnhandledCommandException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.Table;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SelectClubView extends View {

    // ─── Properties ─── //

    private final List<Club> sortedClubs;
    private final ClubDataManager clubDataManager;

    // ─── Constructors ─── //

    public SelectClubView(List<Club> sortedClubs, ClubDataManager clubDataManager) {
        this.sortedClubs = sortedClubs;
        this.clubDataManager = clubDataManager;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        try {
            Table table = ModelTableFormatter.forList(this.sortedClubs);
            System.out.println();
            table.display();
        } catch (EmptyContentModelTableFormatterException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car la liste est vide ou nul"));
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car aucun ModelTable n'est défini pour ce modèle"));
        } catch (ModelTableInstanciationException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car l'instanciation du ModelTable a échoué"));
        }

        System.out.println(TextFormatter.italic("Entrez l'identifiant (#) d'un club pour le sélectionner."));

        try {
            final int[] selectedClubId = new int[1];

            KinomichiFunctions.promptInputWithCommandHandling(scanner, (_, command) -> {
                switch (command) {
                    case ExitCommand exitCommand -> {
                        return exitCommand;
                    }
                    case BackCommand backCommand -> {
                        return backCommand;
                    }
                    case BackBackCommand backBackCommand -> {
                        return backBackCommand;
                    }
                    case SortColumnCommand sortColumnCommand -> {
                        int columnCount;
                        try {
                            columnCount = ModelTableFormatter.getColumnCount(Club.class);
                        } catch (UnimplementedModelTableException e) {
                            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
                            return null;
                        }

                        for (int columnIndex : sortColumnCommand.getSortOrders().keySet()) {
                            if (columnIndex < 1 || columnIndex > columnCount) {
                                System.out.println("L'index de colonne '%d' est invalide. Veuillez entrer un nombre entre 1 et %d.".formatted(columnIndex, columnCount));
                                return null;
                            }
                        }
                        return sortColumnCommand;
                    }
                    default -> throw new UnhandledCommandException(command);
                }
            }, input -> {
                int clubId;
                try {
                    clubId = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new InvalidMenuInputException("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(input), e);
                }

                Club selectedClub = this.clubDataManager.getClub(clubId);

                if (selectedClub == null) {
                    throw new InvalidMenuInputException("Aucun club ne porte l'identifiant '%d'.".formatted(clubId));
                }

                selectedClubId[0] = clubId;
                System.out.printf("%s%n", TextFormatter.green(TextFormatter.italic("Club sélectionné : ", TextFormatter.bold(selectedClub.toString()))));
            });

            return new FormResultEvent<>(selectedClubId[0]);
        } catch (CommandResponseException commandResponseException) {
            Object response = commandResponseException.getResponse();

            if (response instanceof SortColumnCommand sortColumnCommand) {
                return new CallUrlEvent("/clubs/modify/select/sort/" + this.buildSortPathSegment(sortColumnCommand));
            } else if (response instanceof ExitCommand) {
                return new ExitProgramEvent();
            } else if (response instanceof BackBackCommand) {
                return new GoBackBackEvent();
            } else if (response instanceof BackCommand) {
                return new GoBackEvent();
            }

            return new CallUrlEvent("/");
        }
    }

    // ─── Utility methods ─── //

    private String buildSortPathSegment(SortColumnCommand sortColumnCommand) {
        StringBuilder sortPathSegmentBuilder = new StringBuilder();

        for (Map.Entry<Integer, SortColumnCommand.SortOrder> sortOrderEntry : sortColumnCommand.getSortOrders().entrySet()) {
            if (!sortPathSegmentBuilder.isEmpty()) {
                sortPathSegmentBuilder.append(",");
            }

            sortPathSegmentBuilder.append(sortOrderEntry.getKey());

            if (sortOrderEntry.getValue() == SortColumnCommand.SortOrder.DESCENDING) {
                sortPathSegmentBuilder.append(":DESC");
            }
        }

        return sortPathSegmentBuilder.toString();
    }

}
