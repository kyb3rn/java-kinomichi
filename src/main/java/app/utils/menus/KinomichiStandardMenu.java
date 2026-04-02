package app.utils.menus;

import app.events.ExitProgramEvent;
import app.events.GoBackBackEvent;
import app.events.GoBackEvent;
import utils.io.commands.exceptions.UnhandledCommandException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.menus.StandardMenu;

public class KinomichiStandardMenu extends StandardMenu {

    // ─── Constructors ─── //

    public KinomichiStandardMenu(String title, Object backResponseObject) {
        super(title, backResponseObject, new ExitProgramEvent());
        this.setCommandHandler((input, command) -> switch (command) {
            case BackCommand _ -> new GoBackEvent();
            case BackBackCommand _ -> new GoBackBackEvent();
            case ExitCommand _ -> new ExitProgramEvent();
            default -> throw new UnhandledCommandException(command);
        });
    }

}
