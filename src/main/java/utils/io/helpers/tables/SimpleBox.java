package utils.io.helpers.tables;

import java.util.EnumSet;

public class SimpleBox {

    private final Table table;

    public SimpleBox() {
        EnumSet<TableOptions> tableOptions = EnumSet.of(
            TableOptions.BOX_AROUND,
            TableOptions.SEPARATE_HEADER
        );
        this.table = new Table(tableOptions);
        this.table.addColumn();
    }

    public void addLine(String line) {
        this.table.addColumnValue(0, line);
    }

    public void display() {
        this.table.display();
    }

}
