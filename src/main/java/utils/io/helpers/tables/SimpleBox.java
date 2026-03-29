package utils.io.helpers.tables;

import java.util.EnumSet;

public class SimpleBox {

    private final TableFormatter tableFormatter;

    public SimpleBox() {
        EnumSet<TableFormattingOptions> tableFormattingOptions = EnumSet.of(
            TableFormattingOptions.BOX_AROUND,
            TableFormattingOptions.SEPARATE_HEADER
        );
        this.tableFormatter = new TableFormatter(tableFormattingOptions);
        this.tableFormatter.addColumn();
    }

    public void addLine(String line) {
        this.tableFormatter.addColumnValue(0, line);
    }

    public void display() {
        this.tableFormatter.display();
    }

}
