package utils.io.menus;

import utils.io.helpers.tables.Table;
import utils.io.helpers.tables.TableOptions;
import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextColor;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextStyle;

public class StandardMenu extends OptionedMenuStage {

    // ─── Properties ─── //

    private String title = null;

    // ─── Constructors ─── //

    public StandardMenu(String title) {
        this.setTitle(title);
    }

    public StandardMenu() {}

    // ─── Setters ─── //

    public void setTitle(String title) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }

        if (title.contains("\n") || title.contains("\r")) {
            throw new IllegalArgumentException("Le titre ne peut pas contenir de retours à la ligne");
        }

        this.title = title;
    }

    // ─── Overrides & inheritance ─── //

    public void display() {
        Table menuTable = new Table();

        TextFormattingOptions singleHeaderTextFormattingOptions = new TextFormattingOptions();
        singleHeaderTextFormattingOptions.setColor(TextColor.MAGENTA);
        singleHeaderTextFormattingOptions.addStyle(TextStyle.BOLD);
        menuTable.setSingleHeader(TextFormatter.format(singleHeaderTextFormattingOptions, this.title).toString());

        menuTable.removeOption(TableOptions.SEPARATE_COLUMNS);
        Table.Column prefixNumbersColumn = new Table.Column(TextAlignement.RIGHT);
        Table.Column optionsColumn = new Table.Column();
        menuTable.addColumn(prefixNumbersColumn);
        menuTable.addColumn(optionsColumn);
        for (int i = 0; i < this.options.size(); i++) {
            prefixNumbersColumn.addValue(TextFormatter.bold(String.valueOf(i + 1)) + ".");
            optionsColumn.addValue(this.options.get(i).getText());
        }

        menuTable.display();
    }

}
