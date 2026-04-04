package utils.io.menus;

import utils.io.tables.Table;
import utils.io.tables.TableOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextColor;
import utils.io.text_formatting.TextFormatter;
import utils.io.text_formatting.TextFormattingOptions;
import utils.io.text_formatting.TextStyle;

public abstract class StandardMenu extends OrderedMenu {

    // ─── Properties ─── //

    private String title = null;
    private boolean showGoBackOption = true;
    private boolean showExitOption = true;
    private Object backResponseObject = null;
    private Object exitResponseObject = null;
    private boolean navigationOptionsAdded = false;

    // ─── Constructors ─── //

    public StandardMenu() {}

    public StandardMenu(String title, Object backResponseObject) {
        this.setTitle(title);
        this.backResponseObject = backResponseObject;
    }

    public StandardMenu(String title, Object backResponseObject, Object exitResponseObject) {
        this.setTitle(title);
        this.backResponseObject = backResponseObject;
        this.exitResponseObject = exitResponseObject;
    }

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
        Table.Column prefixNumbersColumn = new Table.Column(TextAlignment.RIGHT);
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
        if (!this.navigationOptionsAdded) {
            if (this.showGoBackOption || this.showExitOption) {
                this.addSectionSeparationIndex();
            }

            if (this.showGoBackOption) {
                this.addOption("Retour", this.backResponseObject);
            }

            if (this.showExitOption) {
                this.addOption("Quitter", this.exitResponseObject);
            }

            this.navigationOptionsAdded = true;
        }

        return null;
    }

}
