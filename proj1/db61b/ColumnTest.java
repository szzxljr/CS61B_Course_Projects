package db61b;

import static org.junit.Assert.assertTrue;
import db61b.Table.ValueList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Column.java .
 *  @author Jiarong Li */
public class ColumnTest {

    private Table _table;
    private static final String NAME = "TEST";
    private static final String[] TITLE =
            new String[] {"SID", "NAME", "MAJOR", "Year"};
    private static Column _col1;
    private static Column _col2;
    private static ValueList[] _columns = new ValueList[TITLE.length];
    private static String[][] values =
    {
        { "101", "Knowles", "EECS", "2003" },
        { "104", "Chan", "Math", "2005" },
        { "102", "Xavier", "LSUnd", "2004" },
        { "103", "Armstrong", "EECS", "2006" }
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

    }

    @Test
    public void testGetName() {
        assertTrue(TITLE[0].equals(_col1.getName()));
    }

    @Test
    public void testGetFrom() {
        assertTrue(_columns[0].get(0).equals(_col1.getFrom(0)));
    }


}
