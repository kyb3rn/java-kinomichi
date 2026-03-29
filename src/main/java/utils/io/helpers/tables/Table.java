package utils.io.helpers.tables;

import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.helpers.texts.formatting.TextFormattingOptions;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Table {

    /**
     * Properties
     **/

    private final ArrayList<Column> columns = new ArrayList<>();
    private String singleHeader = null;
    private int horizontalPadding = 1;
    private EnumSet<TableOptions> options = EnumSet.of(
        TableOptions.SEPARATE_COLUMNS,
        TableOptions.SEPARATE_HEADER,
        TableOptions.DISPLAY_HEADER,
        TableOptions.BOX_AROUND
    );

    /**
     * Constructors
     **/

    public Table() {
    }

    public Table(EnumSet<TableOptions> options) {
        this.options = options;
    }

    public Table(ArrayList<Column> columns) {
        this.columns.addAll(columns);
    }

    /**
     * Setters
     **/

    public void setSingleHeader(String singleHeader) {
        this.singleHeader = singleHeader;
    }

    public void setHorizontalPadding(int horizontalPadding) {
        if (horizontalPadding < 1 || horizontalPadding > 5) {
            throw new IllegalArgumentException("Le padding horizontal doit être un nombre entier compris dans l'intervalle [1;5]");
        }

        this.horizontalPadding = horizontalPadding;
    }

    /**
     * Special methods
     **/

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public void addColumn(String name, TextAlignement textAlignement) {
        this.columns.add(new Column(name, textAlignement));
    }

    public void addColumn(String name) {
        this.columns.add(new Column(name));
    }

    public void addColumn(TextAlignement textAlignement) {
        this.columns.add(new Column(textAlignement));
    }

    public void addColumn() {
        this.columns.add(new Column());
    }

    public void addColumns(int count) {
        for (int i = 0; i < count; i++) {
            this.columns.add(new Column());
        }
    }

    public void addColumnValue(int columnIndex, String value) {
        Column column = columns.get(columnIndex);

        if (column == null) {
            throw new IndexOutOfBoundsException("Cet index de colonne n'existe pas");
        }

        column.addValue(value);
    }

    public void addColumnValues(int columnIndex, List<String> values) {
        Column column = columns.get(columnIndex);

        if (column == null) {
            throw new IndexOutOfBoundsException("Cet index de colonne n'existe pas");
        }

        column.addValues(values);
    }

    public void addOption(TableOptions option) {
        this.options.add(option);
    }

    public void removeOption(TableOptions option) {
        this.options.remove(option);
    }

    public void display() {
        StringBuilder stringBuilder = new StringBuilder();

        if (!this.columns.isEmpty()) {
            // Calculate and save columns width
            ArrayList<Integer> columnsInnerWidth = new ArrayList<>();
            for (Column column : columns) {
                int columnLongestValueLength = column.getValues().stream().mapToInt(Functions::visibleLength).max().orElse(0);
                int columnHeaderLength = this.options.contains(TableOptions.DISPLAY_HEADER) ? (column.getName() != null ? Functions.visibleLength(column.getName()) : 0) : 0;
                columnsInnerWidth.add(Math.max(columnLongestValueLength, columnHeaderLength));
            }

            // Column separation
            StringBuilder columnSeparationStringBuilder = new StringBuilder();
            columnSeparationStringBuilder.append(" ".repeat(this.horizontalPadding));
            if (this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                columnSeparationStringBuilder.append('│');
                columnSeparationStringBuilder.append(" ".repeat(this.horizontalPadding));
            }

            final int separationWidth = columnSeparationStringBuilder.length();
            int totalInnerWidth = columnsInnerWidth.stream().mapToInt(Integer::intValue).reduce(-separationWidth, (acc, val) -> acc + val + separationWidth);
            int missingWidthComparedToSingleHeader = (this.singleHeader != null) ? Functions.visibleLength(this.singleHeader) - totalInnerWidth : 0;

            if (missingWidthComparedToSingleHeader > 0) {
                totalInnerWidth += missingWidthComparedToSingleHeader;
                int lastColumnWidth = columnsInnerWidth.getLast();
                columnsInnerWidth.set(columnsInnerWidth.size() - 1, lastColumnWidth + missingWidthComparedToSingleHeader);
            }

            // Format header
            StringBuilder headerStringBuilder = new StringBuilder();
            if (this.options.contains(TableOptions.DISPLAY_HEADER)) {
                if (this.singleHeader != null) {
                    headerStringBuilder.append(TextFormatter.center(totalInnerWidth, this.singleHeader));
                } else {
                    headerStringBuilder.append(
                        this.columns.stream()
                            .map(column -> TextFormatter.align(columnsInnerWidth.get(this.columns.indexOf(column)), column.getName(), column.getAlignement()))
                            .collect(Collectors.joining(columnSeparationStringBuilder))
                    );
                }

                // Add surrounding vertical lines to header if box around option
                if (this.options.contains(TableOptions.BOX_AROUND)) {
                    headerStringBuilder.insert(0, " ".repeat(this.horizontalPadding));
                    headerStringBuilder.insert(0, '│');
                    headerStringBuilder.append(" ".repeat(this.horizontalPadding));
                    headerStringBuilder.append('│');
                }
            }

            // Format each rows
            int maxAmountOfRowsToDisplay = this.columns.stream().mapToInt(c -> c.getValues().size()).max().orElse(0);
            List<StringBuilder> rowsStringBuilder = new ArrayList<>(
                IntStream
                    .range(0, maxAmountOfRowsToDisplay)
                    .mapToObj(i -> new StringBuilder().append(
                            this.columns.stream()
                                .map(column -> {
                                    TextFormattingOptions opts = column.getFormattingOptions();
                                    int savedMinWidth = opts.getMinWidth();
                                    opts.setMinWidth(columnsInnerWidth.get(this.columns.indexOf(column)));
                                    String formatted = TextFormatter.format(column.getValues().get(i), opts);
                                    opts.setMinWidth(savedMinWidth);
                                    return formatted;
                                })
                                .collect(Collectors.joining(columnSeparationStringBuilder))
                        )
                    ).toList()
            );

            // Add surrounding vertical lines to rows if box around option
            if (this.options.contains(TableOptions.BOX_AROUND)) {
                rowsStringBuilder.forEach(row -> {
                    row.insert(0, " ".repeat(this.horizontalPadding));
                    row.insert(0, '│');
                    row.append(" ".repeat(this.horizontalPadding));
                    row.append('│');
                });
            }

            // Intersections - X
            StringBuilder xIntersectionStringBuilder = new StringBuilder();
            xIntersectionStringBuilder.append("─".repeat(this.horizontalPadding));
            if (this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                xIntersectionStringBuilder.append('┼');
                xIntersectionStringBuilder.append("─".repeat(this.horizontalPadding));
            }

            // Intersections - T downwards
            StringBuilder tDownwardsIntersectionStringBuilder = new StringBuilder();
            tDownwardsIntersectionStringBuilder.append("─".repeat(this.horizontalPadding));
            if (this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                tDownwardsIntersectionStringBuilder.append('┬');
                tDownwardsIntersectionStringBuilder.append("─".repeat(this.horizontalPadding));
            }

            // Intersections - T upwards
            StringBuilder tUpwardsIntersectionStringBuilder = new StringBuilder();
            tUpwardsIntersectionStringBuilder.append("─".repeat(this.horizontalPadding));
            if (this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                tUpwardsIntersectionStringBuilder.append('┴');
                tUpwardsIntersectionStringBuilder.append("─".repeat(this.horizontalPadding));
            }

            // Format row separation line
            StringBuilder rowSeparatorStringBuilder = new StringBuilder();

            if (this.options.contains(TableOptions.BOX_AROUND)) {
                rowSeparatorStringBuilder.append('├').append("─".repeat(this.horizontalPadding));
            }
            rowSeparatorStringBuilder.append(
                this.columns.stream()
                    .map(column -> "─".repeat(columnsInnerWidth.get(this.columns.indexOf(column))))
                    .collect(Collectors.joining(xIntersectionStringBuilder))
            );
            if (this.options.contains(TableOptions.BOX_AROUND)) {
                rowSeparatorStringBuilder.append("─".repeat(this.horizontalPadding)).append('┤');
            }

            // Insert separating rows between real rows
            if (this.options.contains(TableOptions.SEPARATE_ROWS)) {
                for (int i = rowsStringBuilder.size() - 1; i > 0; i--) {
                    rowsStringBuilder.add(i, rowSeparatorStringBuilder);
                }
            }

            // Build the final stringBuilder
            if (this.options.contains(TableOptions.BOX_AROUND)) {
                // Top line
                StringBuilder topLineStringBuilder = new StringBuilder();

                StringBuilder topLineColumnIntersection = new StringBuilder();
                if (this.singleHeader != null) {
                    topLineColumnIntersection.append("─".repeat(this.horizontalPadding));

                    if (this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                        topLineColumnIntersection.append("─".repeat(1 + this.horizontalPadding));
                    }
                } else if (!this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                    topLineColumnIntersection.append("─".repeat(this.horizontalPadding));
                } else {
                    topLineColumnIntersection = tDownwardsIntersectionStringBuilder;
                }

                topLineStringBuilder.append("╭");
                topLineStringBuilder.append("─".repeat(this.horizontalPadding));
                topLineStringBuilder.append(
                    this.columns.stream()
                        .map(column -> "─".repeat(columnsInnerWidth.get(this.columns.indexOf(column))))
                        .collect(Collectors.joining(topLineColumnIntersection))
                );
                topLineStringBuilder.append("─".repeat(this.horizontalPadding));
                topLineStringBuilder.append("╮");

                stringBuilder.append(topLineStringBuilder);
                stringBuilder.append(System.lineSeparator());
            }

            // Header
            if (!headerStringBuilder.isEmpty()) {
                stringBuilder.append(headerStringBuilder);
                stringBuilder.append(System.lineSeparator());

                if (this.options.contains(TableOptions.SEPARATE_HEADER)) {
                    StringBuilder headerSeparatingLineStringBuilder = new StringBuilder();

                    if (this.singleHeader != null) {
                        headerSeparatingLineStringBuilder.append('├');
                        headerSeparatingLineStringBuilder.append("─".repeat(this.horizontalPadding));

                        if (!this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                            headerSeparatingLineStringBuilder.append("─".repeat(totalInnerWidth));
                        } else {
                            headerSeparatingLineStringBuilder.append(
                                this.columns.stream()
                                    .map(column -> "─".repeat(columnsInnerWidth.get(this.columns.indexOf(column))))
                                    .collect(Collectors.joining(tDownwardsIntersectionStringBuilder))
                            );
                        }

                        headerSeparatingLineStringBuilder.append("─".repeat(this.horizontalPadding));
                        headerSeparatingLineStringBuilder.append('┤');
                    } else {
                        headerSeparatingLineStringBuilder = rowSeparatorStringBuilder;
                    }

                    stringBuilder.append(headerSeparatingLineStringBuilder);
                    stringBuilder.append(System.lineSeparator());
                }
            }

            // Main content
            rowsStringBuilder.forEach(row -> {
                stringBuilder.append(row);
                stringBuilder.append(System.lineSeparator());
            });

            if (this.options.contains(TableOptions.BOX_AROUND)) {
                // Bottom line
                StringBuilder bottomLineStringBuilder = new StringBuilder();

                StringBuilder bottomLineColumnIntersection = new StringBuilder();
                if (!this.options.contains(TableOptions.SEPARATE_COLUMNS)) {
                    bottomLineColumnIntersection.append("─".repeat(this.horizontalPadding));
                } else {
                    bottomLineColumnIntersection = tUpwardsIntersectionStringBuilder;
                }

                bottomLineStringBuilder.append("╰");
                bottomLineStringBuilder.append("─".repeat(this.horizontalPadding));
                bottomLineStringBuilder.append(
                    this.columns.stream()
                        .map(column -> "─".repeat(columnsInnerWidth.get(this.columns.indexOf(column))))
                        .collect(Collectors.joining(bottomLineColumnIntersection))
                );
                bottomLineStringBuilder.append("─".repeat(this.horizontalPadding));
                bottomLineStringBuilder.append("╯");

                stringBuilder.append(bottomLineStringBuilder);
                stringBuilder.append(System.lineSeparator());
            }
        }

        System.out.println(stringBuilder);
    }

    public static class Column {

        /**
         * Properties
         **/

        private final List<String> values = new ArrayList<>();
        private String name = null;
        private TextFormattingOptions formattingOptions = new TextFormattingOptions();

        /**
         * Constructors
         **/

        public Column(String name, TextFormattingOptions formattingOptions) {
            this.setName(name);
            this.formattingOptions = formattingOptions;
        }

        public Column(String name, TextAlignement alignement) {
            this.setName(name);
            this.formattingOptions.setAlignment(alignement);
        }

        public Column(TextFormattingOptions formattingOptions) {
            this.formattingOptions = formattingOptions;
        }

        public Column(TextAlignement alignement) {
            this.formattingOptions.setAlignment(alignement);
        }

        public Column(String name) {
            this.setName(name);
        }

        public Column() {
        }

        /**
         * Getters
         **/

        public List<String> getValues() {
            return this.values;
        }

        public String getName() {
            return this.name;
        }

        public TextFormattingOptions getFormattingOptions() {
            return this.formattingOptions;
        }

        public TextAlignement getAlignement() {
            return this.formattingOptions.getAlignment();
        }

        /**
         * Setters
         **/

        public void setName(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Le nom d'une colonne ne peut pas être vide");
            }

            this.name = name.strip();
        }

        /**
         * Special methods
         **/

        public void addValue(String value) {
            if (value == null) {
                this.values.add("");
            } else {
                if (value.contains("\n") || value.contains("\r")) {
                    throw new IllegalArgumentException("La valeur ne peut pas contenir de retours à la ligne");
                }

                this.values.add(value);
            }
        }

        public void addValues(List<String> values) {
            for (String value : values) {
                this.addValue(value);
            }
        }

    }

}
