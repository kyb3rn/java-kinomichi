package utils.io.commands;

import utils.io.commands.exceptions.NotACommandException;
import utils.io.commands.exceptions.UnknownCommandException;

import java.util.List;

public enum ECommand {

    EXIT("exit", List.of("exit", "e")),
    BACK("back", List.of("back", "b")),
    BACK_BACK("backback", List.of("backback", "bb")),
    SORT_COLUMN("sort", List.of("sort", "s"));

    private final String name;
    private final List<String> shortcuts;

    ECommand(String name, List<String> shortcuts) {
        this.name = name;
        this.shortcuts = shortcuts;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getShortcuts() {
        return this.shortcuts;
    }

    public static ECommand convert(String command) throws UnknownCommandException, NotACommandException {
        if (command == null) {
            throw new NotACommandException();
        }

        for (ECommand c : ECommand.values()) {
            for (String s : c.getShortcuts()) {
                if (s.equalsIgnoreCase(command)) {
                    return c;
                }
            }
        }

        throw new UnknownCommandException();
    }

}
