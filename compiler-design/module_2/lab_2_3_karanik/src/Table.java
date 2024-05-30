import java.util.ArrayList;
import java.util.List;

public class Table<R, C, V> {
    private List<C> columnHeaders = new ArrayList<>();
    private List<R> rowHeaders = new ArrayList<>();
    private List<List<V>> data = new ArrayList<>();

    public void addColumn(C header) {
        columnHeaders.add(header);
        for (List<V> row : data) {
            row.add(null);
        }
    }

    public void addRow(R header) {
        rowHeaders.add(header);
        List<V> newRow = new ArrayList<>();
        for (int i = 0; i < columnHeaders.size(); i++) {
            newRow.add(null);
        }
        data.add(newRow);
    }

    public V get(R rowHeader, C colHeader) {
        int rowIndex = rowHeaders.indexOf(rowHeader);
        int colIndex = columnHeaders.indexOf(colHeader);

        if (rowIndex == -1 || colIndex == -1) {
            throw new IllegalArgumentException("Invalid row or column header.");
        }

        return data.get(rowIndex).get(colIndex);
    }

    public void setValue(R rowHeader, C colHeader, V value) {
        int rowIndex = rowHeaders.indexOf(rowHeader);
        int colIndex = columnHeaders.indexOf(colHeader);

        if (rowIndex == -1 || colIndex == -1) {
            throw new IllegalArgumentException("Invalid row or column header.");
        }

        data.get(rowIndex).set(colIndex, value);
    }

    public void print() {
        int[] colWidths = new int[columnHeaders.size()];

        for (int i = 0; i < columnHeaders.size(); i++) {
            colWidths[i] = columnHeaders.get(i).toString().length();
        }
        for (List<V> row : data) {
            for (int i = 0; i < row.size(); i++) {
                V value = row.get(i);
                if (value != null) {
                    colWidths[i] = Math.max(colWidths[i], value.toString().length());
                }
            }
        }

        int rowHeaderWidth = rowHeaders.stream().mapToInt(header -> header.toString().length()).max().orElse(0);

        System.out.print(" ".repeat(rowHeaderWidth + 2));
        for (int i = 0; i < columnHeaders.size(); i++) {
            System.out.print(String.format("%-" + (colWidths[i] + 2) + "s", columnHeaders.get(i)));
        }
        System.out.println();

        for (int row = 0; row < data.size(); row++) {
            System.out.print(String.format("%-" + (rowHeaderWidth + 2) + "s", rowHeaders.get(row)));
            for (int col = 0; col < columnHeaders.size(); col++) {
                V value = data.get(row).get(col);
                System.out.print(String.format("%-" + (colWidths[col] + 2) + "s", value != null ? value.toString() : "null"));
            }
            System.out.println();
        }
    }
}