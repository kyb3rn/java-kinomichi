package utils.io.menus;

import app.events.ExitProgramEvent;
import utils.io.helpers.tables.Table;
import utils.io.helpers.tables.TableOptions;
import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextColor;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextStyle;

public class StandardMenu extends OrderedMenu {

    // ─── Properties ─── //

    private String title = null;
    private boolean showGoBackOption = true;
    private boolean showExitOption = true;

    // ─── Constructors ─── //

    public StandardMenu(String title) {
        this.setTitle(title);
    }

    public StandardMenu() {}

    // ─── Setters ─── //

    public void setShowGoBackOption(boolean showGoBackOption) {
        this.showGoBackOption = showGoBackOption;
    }

    public void setShowExitOption(boolean showExitOption) {
        this.showExitOption = showExitOption;
    }

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

    @Override
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
        for (int i = 0, j = 0; i + j < this.options.size() + this.unoptionedRows.size();) {
            if (this.unoptionedRows.containsKey(i)) {
                prefixNumbersColumn.addValue(TextFormatter.bold(">") + " ");
                optionsColumn.addValue(this.unoptionedRows.get(j));
                j++;
            }

            prefixNumbersColumn.addValue(TextFormatter.bold(String.valueOf(i + 1)) + ".");
            optionsColumn.addValue(this.options.get(i).getText());
            i++;
        }

        menuTable.setRowSeparationIndexes(this.sectionSeparationIndexes);

        menuTable.display();
    }

    @Override
    public MenuResponse beforeDisplay() {
        this.addSectionSeparationIndex();

        if (this.showGoBackOption) {
            this.addOption("Retour", "main");
        }

        if (this.showExitOption) {
            this.addOption("Quitter", new ExitProgramEvent());
        }

        return null;
    }

}
