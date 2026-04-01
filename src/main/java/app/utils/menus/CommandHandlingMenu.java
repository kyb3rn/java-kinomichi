package app.utils.menus;

import app.events.ExitProgramEvent;
import app.events.GoBackEvent;
import app.utils.ExitProgramException;
import app.utils.helpers.KinomichiFunctions;
import utils.io.commands.*;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.menus.HookInterruptException;
import utils.io.menus.Menu;
import utils.io.menus.MenuResponse;
import utils.io.menus.OrderedMenuOption;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public abstract class CommandHandlingMenu extends Menu {

    // ─── Utility methods ─── //

    protected void handleCommand(Command command) throws ExitProgramException, ExitInputPromptException, UnhandledCommandException {
        switch (command) {
            case ExitCommand _ -> throw new ExitProgramException();
            case BackCommand _ -> throw new GoBackException();
            default -> throw new UnhandledCommandException(command);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    protected MenuResponse askingValidInputLoopHandle(AtomicReference<String> validInput, AtomicReference<OrderedMenuOption> selectedOption) {
        Scanner scanner = new Scanner(System.in);

        try {
            KinomichiFunctions.promptInput(scanner, (_, command) -> this.handleCommand(command), input -> {
                int parsedChoice;
                try {
                    parsedChoice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new InvalidInputFormMenuException("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(input), e);
                }

                int optionsSize = this.options.size();
                if (parsedChoice < 1 || parsedChoice > optionsSize) {
                    if (optionsSize == 1) {
                        throw new InvalidInputFormMenuException("Le choix '%s' est invalide. Seul le choix 1 est valide.".formatted(input));
                    } else if (optionsSize == 2) {
                        throw new InvalidInputFormMenuException("Le choix '%s' est invalide. Veuillez entrer soit 1, soit 2.".formatted(input));
                    } else {
                        throw new InvalidInputFormMenuException("Le choix '%s' est invalide. Veuillez choisir une option entre 1 et %s (inclus).".formatted(input, optionsSize));
                    }
                }

                validInput.set(input);
                selectedOption.set((OrderedMenuOption) this.options.get(parsedChoice - 1));
            }, this::beforeInput, this::afterEveryInput);
        } catch (GoBackException _) {
            return new MenuResponse(new GoBackEvent());
        } catch (ExitProgramException _) {
            return new MenuResponse(new ExitProgramEvent());
        } catch (HookInterruptException hookInterruptException) {
            return hookInterruptException.getMenuResponse();
        } catch (ExitInputPromptException _) {
        }

        return null;
    }

}
