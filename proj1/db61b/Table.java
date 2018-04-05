
package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author Jiarong Li
 */
class Table {
    /** A new Table whose columns are given by COLUMNTITLES, which may
     *  not contain duplicate names. */
    Table(String[] columnTitles) {
        if (columnTitles.length == 0) {
            throw error("table must have at least one column");
        }
        _size = 0;
        _rowSize = columnTitles.length;
        for (int i = columnTitles.length - 1; i >= 1; i -= 1) {
            for (int j = i - 1; j >= 0; j -= 1) {
                if (columnTitles[i].equals(columnTitles[j])) {
                    throw error("duplicate column name: %s",
                                columnTitles[i]);
                }
            }
        }
        _titles = columnTitles;
        _columns = new ValueList[_rowSize];
        for (int i = 0; i < _rowSize; i++) {
            _columns[i] = new ValueList();
        }
    }

    /** A new Table whose columns are give by COLUMNTITLES. */
    Table(List<String> columnTitles) {
        this(columnTitles.toArray(new String[columnTitles.size()]));
    }

    /** Return the number of columns in this table. */
    public int columns() {
        return _columns.length;
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    public String getTitle(int k) {
        try {
            return _titles[k];
        } catch (IndexOutOfBoundsException excp) {
            throw error("invalid column");
        }
    }

    /** Get columnTitles.
     * @return */
    public String[] getTitles() {
        return _titles;
    }

    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    public int findColumn(String title) {
        for (int i = 0; i < _titles.length; i += 1) {
            if (_titles[i].equals(title)) {
                return i;
            }
        }
        return -1;
    }

    /** Return the number of rows in this table. */
    public int size() {
        return _columns[0].size();
    }

    /** Return the value of column number COL (0 <= COL < columns())
     *  of record number ROW (0 <= ROW < size()). */
    public String get(int row, int col) {
        try {
            return _columns[col].get(row);
        } catch (IndexOutOfBoundsException excp) {
            throw error("invalid row or column");
        }
    }

    /** Get Row.
     * @return the Row.
     * @param rowIndex the index of a chosen row. */
    public String[] getRow(int rowIndex) {
        String[] row = new String[_rowSize];
        for (int col = 0; col < _rowSize; col++) {
            row[col] = _columns[col].get(rowIndex);
        }
        return row;
    }

    /** Get Column.
     * @return the Column.
     * @param colIndex the index of a chosen column. */
    public ValueList getColumn(int colIndex) {
        return _columns[colIndex];
    }

    /** Get Index of lexicographic order of a row k.
     * @return Sorted index of a row.
     * @param k is index of a row. */
    public int getIndex(int k) {
        return _index.get(k);
    }

    /** Find the place for new string values. Add into index in
     * lexicographic order.
     * @param values a row String. */
    public void addIndex(String[] values) {
        if (_index.isEmpty()) {
            _index.add(0, 0);
        } else {
            for (int indexRow = 0; indexRow < _index.size(); indexRow++) {
                int row = _index.indexOf(indexRow);
                if (this.compareRows(_index.size(), row) < 0) {
                    for (int k = 0; k < _index.size(); k++) {
                        if (_index.get(k) >= indexRow) {
                            _index.set(k, _index.get(k) + 1);
                        }
                    }
                    _index.add(indexRow);
                    return;
                }
            }
            _index.add(_index.size());
        }
    }

    /** Add a new row whose column values are VALUES to me if no equal
     *  row already exists.  Return true if anything was added,
     *  false otherwise. */
    public boolean add(String[] values) {
        if (values.length != _rowSize) {
            throw error("Unequal length. "
                            + "Input's row length is (%s) "
                            + "but number of columns is (%s)",
                            values.length, _rowSize);
        }
        if (!_columns[0].isEmpty()) {
            for (int row = 0; row < _size; row += 1) {
                for (int col = 0; col < _rowSize; col += 1) {
                    if (!_columns[col].get(row).equals(values[col])) {
                        break;
                    } else {
                        if (col == _rowSize - 1) {
                            return false;
                        }
                        continue;
                    }
                }
            }
        }
        for (int col = 0; col < _rowSize; col += 1) {
            _columns[col].add(values[col]);
        }
        _size = _columns[0].size();
        addIndex(values);
        return true;
    }

    /** Add a new row whose column values are extracted by COLUMNS from
     *  the rows indexed by ROWS, if no equal row already exists.
     *  Return true if anything was added, false otherwise.
     *  See Column.getFrom(Integer...) for a description of how Columns
     *  extract values. */
    public boolean add(List<Column> columns, Integer... rows) {
        if (columns.size() != _rowSize) {
            throw error("Unequal length. "
                            + "Input's row length is (%s) "
                            + "but number of columns is (%s)",
                            columns.size(), _rowSize);
        }
        if (!_columns[0].isEmpty()) {
            for (int row = 0; row < _size; row += 1) {
                for (int col = 0; col < _rowSize; col += 1) {
                    String value = columns.get(col).getFrom(rows);
                    if (!_columns[col].get(row).equals(value)) {
                        break;
                    } else {
                        if (col == _rowSize - 1) {
                            return false;
                        }
                        continue;
                    }
                }
            }
        }
        for (int col = 0; col < _rowSize; col += 1) {
            String value = columns.get(col).getFrom(rows);
            _columns[col].add(value);
        }
        _size = _columns[0].size();
        return true;
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        table = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            table = new Table(columnNames);
            String curRowData;
            while ((curRowData = input.readLine()) != null) {
                String[] curRow = curRowData.split(",");
                if (curRow.length != columnNames.length) {
                    throw error("Unequal length. "
                                    + "Input's row length is (%s) "
                                    + "but number of columns is (%s)",
                            curRow.length, columnNames.length);
                }
                table.add(curRow);
            }
        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* Ignore IOException */
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");
            String[] titles = _titles;
            output.println(Arrays.toString(titles).replace(" ", "")
                    .replace("[", "").replace("]", ""));
            for (int row = 0; row < _size; row++) {
                String[] rowValue = getRow(row);
                String rowWrite = "";
                for (int col = 0; col < _rowSize; col++) {
                    rowWrite += rowValue[col];
                    if (col != _rowSize - 1) {
                        rowWrite += ",";
                    }
                }
                output.println(rowWrite);
            }
        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output, separated by spaces
     *  and indented by two spaces. */
    void print() {
        for (int rowSorted = 0; rowSorted < _size; rowSorted++) {
            int row = _index.indexOf(rowSorted);
            String[] rowValue = getRow(row);
            String rowPrint = "";
            for (int col = 0; col < _rowSize; col++) {
                rowPrint += rowValue[col];
                if (col != _rowSize - 1) {
                    rowPrint += " ";
                }
            }
            System.out.println("  " + rowPrint);
        }
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result = new Table(columnNames);
        int colSize = columnNames.size();
        String[] columnTitles = columnNames.toArray(new String[colSize]);
        for (int row = 0; row < _size; row++) {
            if (Condition.test(conditions, row)) {
                String[] values = this.getRow(row);
                ArrayList<String> filterValues = new ArrayList<>();
                for (int k = 0; k < colSize; k++) {
                    String add = this.getRow(row)[findColumn(columnTitles[k])];
                    filterValues.add(add);
                }
                result.add(filterValues.toArray(new String[colSize]));
            }

        }
        return result;
    }

    /** Select Equal Column.
     * @return List of Equal Column pairs.
     * @param table2 the table2. */
    public IntergerList[] selectEqual(Table table2) {
        ArrayList<String> titles1 =
                new ArrayList<String>(Arrays.asList(_titles));
        ArrayList<String> titles2 =
                new ArrayList<String>(Arrays.asList(table2.getTitles()));
        IntergerList eqlCol1 = new IntergerList();
        IntergerList eqlCol2 = new IntergerList();
        for (int col1 = 0; col1 < titles1.size(); col1++) {
            for (int col2 = 0; col2 < titles2.size(); col2++) {
                if (titles1.get(col1).equals(titles2.get(col2))) {
                    eqlCol1.add(col1);
                    eqlCol2.add(col2);
                }
            }
        }
        return new IntergerList[]{eqlCol1, eqlCol2};
    }

    /** Return an unsorted jointable.
     * @return joinTable
     * @param eqlCols the List of equal colunms of two table
     * @param table2 the table2*/
    public Table cotable2(Table table2, IntergerList[] eqlCols) {
        Table joinTable;
        int rowsize1 = this.getTitles().length;
        int rowsize2 = table2.getTitles().length;
        String[] sTitles1 = _titles;
        String[] sTitles2 = table2.getTitles();
        int col2 = eqlCols[1].get(0);
        int col22 = eqlCols[1].get(1);
        if (col2 > col22) {
            int temp = col2;
            col2 = col22;
            col22 = temp;
        }
        int joinLength = rowsize1 + rowsize2 - 2;
        String[] joinTitles = new String[joinLength];
        System.arraycopy(sTitles1, 0, joinTitles, 0, rowsize1);
        System.arraycopy(sTitles2, 0, joinTitles, rowsize1, col2);
        System.arraycopy(sTitles2, col2 + 1, joinTitles,
                rowsize1 + col2, col22 - col2 - 1);
        System.arraycopy(sTitles2, col22 + 1, joinTitles,
                rowsize1 + col22 - col2 - 1, rowsize2 - col22 - 1);
        joinTable = new Table(joinTitles);
        int eqlCol1Index = eqlCols[0].get(0);
        int eqlCol2Index = eqlCols[1].get(0);
        ArrayList<Integer> eqlRow1 = new ArrayList<>();
        ArrayList<Integer> eqlRow2 = new ArrayList<>();
        for (int row1 = 0; row1 < this.size(); row1++) {
            for (int row2 = 0; row2 < table2.size(); row2++) {
                Boolean equal1 = this.getColumn(eqlCol1Index).get(row1)
                        .equals(table2.getColumn(eqlCol2Index).get(row2));
                Boolean equal2 = this.getColumn(eqlCols[0].get(1)).get(row1)
                        .equals(table2.getColumn(eqlCols[1].get(1)).get(row2));
                if (equal1 && equal2) {
                    eqlRow1.add(row1);
                    eqlRow2.add(row2);
                    String[] joinRow = new String[joinLength];
                    String[] valuesRow1 = this.getRow(row1);
                    String[] valuesRow2 = table2.getRow(row2);
                    System.arraycopy(valuesRow1, 0, joinRow, 0, rowsize1);
                    System.arraycopy(valuesRow2, 0, joinRow, rowsize1, col2);
                    System.arraycopy(valuesRow2, col2 + 1, joinRow,
                            rowsize1 + col2, col22 - col2 - 1);
                    System.arraycopy(valuesRow2, col22 + 1, joinRow,
                            rowsize1 + col22 - col2 - 1, rowsize2 - col22 - 1);
                    joinTable.add(joinRow);
                }
            }
        }
        return joinTable;
    }

    /** Return an unsorted jointable.
     * @return joinTable
     * @param eqlCols the List of equal colunms of two table
     * @param table2 the table2*/
    public Table cotable(Table table2, IntergerList[] eqlCols) {
        Table joinTable;
        int rowsize1 = this.getTitles().length;
        int rowsize2 = table2.getTitles().length;
        String[] sTitles1 = _titles;
        String[] sTitles2 = table2.getTitles();
        if (eqlCols[0].isEmpty()) {
            int joinLength = rowsize1 + rowsize2;
            String[] joinTitles = new String[joinLength];
            System.arraycopy(sTitles1, 0, joinTitles, 0, rowsize1);
            System.arraycopy(sTitles2, 0, joinTitles,
                    rowsize1, rowsize2);
            joinTable = new Table(joinTitles);
            for (int row1 = 0; row1 < this.size(); row1++) {
                for (int row2 = 0; row2 < table2.size(); row2++) {
                    String[] joinRow = new String[joinLength];
                    String[] valuesRow1 = this.getRow(row1);
                    String[] valuesRow2 = table2.getRow(row2);
                    System.arraycopy(valuesRow1, 0, joinRow, 0, rowsize1);
                    System.arraycopy(valuesRow2, 0, joinRow,
                            rowsize1, rowsize2);
                    joinTable.add(joinRow);
                }
            }
        } else if (eqlCols[0].size() == 1) {
            int col2 = eqlCols[1].get(0);
            int joinLength = rowsize1 + rowsize2 - 1;
            String[] joinTitles = new String[joinLength];
            System.arraycopy(sTitles1, 0, joinTitles, 0, rowsize1);
            System.arraycopy(sTitles2, 0, joinTitles, rowsize1, col2);
            System.arraycopy(sTitles2, col2 + 1, joinTitles,
                    rowsize1 + col2, rowsize2 - col2 - 1);
            joinTable = new Table(joinTitles);
            int eqlCol1Index = eqlCols[0].get(0);
            int eqlCol2Index = eqlCols[1].get(0);
            for (int row1 = 0; row1 < this.size(); row1++) {
                for (int row2 = 0; row2 < table2.size(); row2++) {
                    if (this.getColumn(eqlCol1Index).get(row1)
                            .equals(table2.getColumn(eqlCol2Index).get(row2))) {
                        String[] joinRow = new String[joinLength];
                        String[] valuesRow1 = this.getRow(row1);
                        String[] valuesRow2 = table2.getRow(row2);
                        System.arraycopy(valuesRow1, 0,
                                joinRow, 0, rowsize1);
                        System.arraycopy(valuesRow2, 0,
                                joinRow, rowsize1, col2);
                        System.arraycopy(valuesRow2, col2 + 1,
                                joinRow, rowsize1 + col2, rowsize2 - col2 - 1);
                        joinTable.add(joinRow);
                    }
                }
            }
        } else if (eqlCols[0].size() == table2.columns()) {
            joinTable = this;
        } else {
            joinTable = cotable2(table2, eqlCols);
        }
        return joinTable;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {
        Table result;
        IntergerList[] eqlCols = this.selectEqual(table2);
        Table joinTable = cotable(table2, eqlCols);

        Table sortedTable = new Table(joinTable.getTitles());
        for (int sortedRow = 0; sortedRow < joinTable.size(); sortedRow++) {
            int rawRow = joinTable._index.indexOf(sortedRow);
            sortedTable.add(joinTable.getRow(rawRow));
        }
        List<Condition> joinConditions = new ArrayList<>();
        for (Condition con : conditions) {
            Condition co; Column c1, c2;
            c1 = new Column(con.getCol1().getName(), sortedTable);
            if (con.getCol2() instanceof ColumnLiteral) {
                String value = ((ColumnLiteral) con.getCol2()).getValue();
                c2 = new ColumnLiteral(value, sortedTable);
            } else {
                c2 = new Column(con.getCol2().getName(), sortedTable);
            }
            joinConditions.add(new Condition(c1, con.getRelation(), c2));
        }
        result = sortedTable.select(columnNames, joinConditions);
        Table sortedResult = new Table(result.getTitles());
        for (int sortedRow = 0; sortedRow < result.size(); sortedRow++) {
            int rawRow = result._index.indexOf(sortedRow);
            sortedResult.add(result.getRow(rawRow));
        }
        return sortedResult;
    }

    /** Return <0, 0, or >0 depending on whether the row formed from
     *  the elements _columns[0].get(K0), _columns[1].get(K0), ...
     *  is less than, equal to, or greater than that formed from elememts
     *  _columns[0].get(K1), _columns[1].get(K1), ....  This method ignores
     *  the _index. */
    private int compareRows(int k0, int k1) {
        for (int i = 0; i < _columns.length; i += 1) {
            int c = _columns[i].get(k0).compareTo(_columns[i].get(k1));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /** Return true if the columns COMMON1 from ROW1 and COMMON2 from
     *  ROW2 all have identical values.  Assumes that COMMON1 and
     *  COMMON2 have the same number of elements and the same names,
     *  that the columns in COMMON1 apply to this table, those in
     *  COMMON2 to another, and that ROW1 and ROW2 are indices, respectively,
     *  into those tables. */
    private static boolean equijoin(List<Column> common1, List<Column> common2,
                                    int row1, int row2) {
        for (int col = 0; col < common1.size(); col++) {
            String s1 = common1.get(col).getFrom(row1);
            String s2 = common2.get(col).getFrom(row2);
            if (!s1.equals(s2)) {
                return false;
            }
        }
        return true;
    }

    /** A class that is essentially ArrayList<String>.  For technical reasons,
     *  we need to encapsulate ArrayList<String> like this because the
     *  underlying design of Java does not properly distinguish between
     *  different kinds of ArrayList at runtime (e.g., if you have a
     *  variable of type Object that was created from an ArrayList, there is
     *  no way to determine in general whether it is an ArrayList<String>,
     *  ArrayList<Integer>, or ArrayList<Object>).  This leads to annoying
     *  compiler warnings.  The trick of defining a new type avoids this
     *  issue. */
    public static class ValueList extends ArrayList<String> {
    }

    /** IntergerList. */
    public static class IntergerList extends ArrayList<Integer> {
    }

    /** My column titles. */
    private final String[] _titles;
    /** My columns. Row i consists of _columns[k].get(i) for all k. */
    private final ValueList[] _columns;

    /** Rows in the database are supposed to be sorted. To do so, we
     *  have a list whose kth element is the index in each column
     *  of the value of that column for the kth row in lexicographic order.
     *  That is, the first row (smallest in lexicographic order)
     *  is at position _index.get(0) in _columns[0], _columns[1], ...
     *  and the kth row in lexicographic order in at position _index.get(k).
     *  When a new row is inserted, insert its index at the appropriate
     *  place in this list.
     *  (Alternatively, we could simply keep each column in the proper order
     *  so that we would not need _index.  But that would mean that inserting
     *  a new row would require rearranging _rowSize lists (each list in
     *  _columns) rather than just one. */
    private final ArrayList<Integer> _index = new ArrayList<>();



    /** My number of rows (redundant, but convenient). */
    private int _size;
    /** My number of columns (redundant, but convenient). */
    private final int _rowSize;
}
