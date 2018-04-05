package db61b;

import static org.junit.Assert.assertEquals;
import db61b.Table.ValueList;

import org.junit.Before;

import org.junit.Test;
import org.junit.BeforeClass;

/** Test database.
 *  @author Jiarong Li */
public class DatabaseTest {

    private static Database _database = new Database();
    private Table _table;
    private static Table _table1, _table2;
    private static String nametable1, nametable2;
    private static final String NAME = "TEST";
    private static final String[] TITLE =
            new String[] {"SID", "NAME", "MAJOR", "Year"};
    private static Column _col1;
    private static Column _col2;
    private static ValueList[] _columns = new ValueList[TITLE.length];
    private static String[][] values =
    {
        { "101", "103", "EECS", "2003" },
        { "104", "102", "Math", "2005" },
        { "102", "102", "LSUnd", "2004" },
        { "103", "105", "EECS", "2006" }
    };

    @BeforeClass
    public static void setupOnce() {
        for (int i = 0; i < TITLE.length; i++) {
            _columns[i] = new ValueList();
        }
        for (int row = 0; row < values.length; row += 1) {
            for (int col = 0; col < TITLE.length; col += 1) {
                _columns[col].add(values[row][col]);
            }
        }
    }

    @Before
    public void setup() throws Exception {
        _table = new Table(TITLE);
        for (int row = 0; row < values.length; row += 1) {
            _table.add(values[row]);
        }
        _col1 = new Column(TITLE[0], _table);
        _table1 = _table;
        nametable1 = NAME;
        _table2 = _table;
        nametable2 = NAME;
        _database.put(nametable1, _table1);

    }

    @Test
    public void testGet() {
        Table actTable1 = _database.get(nametable1);
        Table expTable1 = _table1;
        assertEquals(actTable1.get(0, 0), expTable1.get(0, 0));
    }

    @Test
    public void testPut() {
        _database.put(nametable2, _table2);
        Table actTable2 = _database.get(nametable2);
        Table expTable2 = _table2;
        assertEquals(actTable2.get(0, 0), expTable2.get(0, 0));
    }
}
