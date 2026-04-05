package app.views.sessions;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.ExitProgramEvent;
import app.events.FormResultEvent;
import app.events.GoBackBackEvent;
import app.events.GoBackEvent;
import app.models.Session;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.SessionDataManager;
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
import java.util.concurrent.atomic.AtomicInteger;

public class SelectSessionView extends View {

    // ─── Properties ─── //

    private final int campId;
    private final List<Session> sortedSessions;
    private final SessionDataManager sessionDataManager;

    // ─── Constructors ─── //

    public SelectSessionView(int campId, List<Session> sortedSessions, SessionDataManager sessionDataManager) {
        this.campId = campId;
        this.sortedSessions = sortedSessions;
        this.sessionDataManager = sessionDataManager;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        Scanner scanner = new Scanner(System.in);

        try {
            Table table = ModelTableFormatter.forList(this.sortedSessions);
            System.out.println();
            table.display();
        } catch (EmptyContentModelTableFormatterException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car la liste est vide ou nulle"));
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car aucun ModelTable n'est défini pour ce modèle"));
        } catch (ModelTableInstanciationException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car l'instanciation du ModelTable a échoué"));
        }

        System.out.println(TextFormatter.italic("Entrez l'identifiant (#) d'une session pour la sélectionner."));

        try {
            AtomicInteger selectedSessionId = new AtomicInteger();

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
                            columnCount = ModelTableFormatter.getColumnCount(Session.class);
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
                int sessionId;
                try {
                    sessionId = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new InvalidMenuInputException("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(input), e);
                }

                Session selectedSession = this.sessionDataManager.getSessionWithExceptions(sessionId);

                if (selectedSession == null) {
                    throw new InvalidMenuInputException("Aucune session ne porte l'identifiant '%d'.".formatted(sessionId));
                }

                selectedSessionId.set(sessionId);
                System.out.printf("%s%n", TextFormatter.green(TextFormatter.italic("Session sélectionnée : ", TextFormatter.bold(selectedSession.toString()))));
            });

            return new FormResultEvent<>(selectedSessionId.get());
        } catch (CommandResponseException commandResponseException) {
            Object response = commandResponseException.getResponse();

            if (response instanceof SortColumnCommand sortColumnCommand) {
                return new CallUrlEvent("/camps/manage/%d/sessions/trainers/select/sort/%s".formatted(this.campId, this.buildSortPathSegment(sortColumnCommand)));
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
