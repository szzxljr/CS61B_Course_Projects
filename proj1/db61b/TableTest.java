package db61b;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import db61b.Table.ValueList;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Table.
 *  @author Jiarong Li */
public class TableTest {

    private Table _table, _tabletest;
    private static final String NAME = "TEST";
    private static final String[] TITLE =
            new String[] {"SID", "NAME", "MAJOR", "Year"};
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
    }

    @Test
    public void testColumns() {
        assertEquals(TITLE.length, _table.columns());
    }

    @Test
    public void testGetTITLE() {
        for (int k = 0; k < TITLE.length; k++) {
            assertEquals(TITLE[k], _table.getTitle(k));
        }
    }

    @Test
    public void testFindColumn() {
        assertEquals(1, _table.findColumn("NAME"));
        assertEquals(-1, _table.findColumn("NA"));
    }



    @Test
    public void testAdd() {
        String[] addValues1 = new String[] {"100", "Brown", "EECS", "2009"};
        String[] addValues3 = new String[] {"100", "Brown", "EECS", "2009"};
        _table.add(addValues1);
        _table.add(addValues3);
        assertEquals(addValues1[3], _table.get(4, 3));
        assertEquals(_table.size(), 5);
        Table table = new Table(TITLE);
        table.add(addValues1);
    }

    @Test
    public void testSize() {
        assertEquals(values.length, _table.size());
    }

    @Test
    public void testGet() {
        assertEquals(values[1][2], _table.get(1, 2));
        assertEquals(values[0][0], _table.get(0, 0));
        assertEquals(values[1][3], _table.get(1, 3));
    }

    @Test
    public void testGetRow() {
        String[] expRow = new String[]{ "101", "Knowles", "EECS", "2003" };
        String[] actRow = _table.getRow(0);
        for (int i = 0; i < TITLE.length; i++) {
            assertEquals(expRow[i], actRow[i]);
        }
    }

    @Test
    public void testAddIndex() {
        _tabletest = new Table(TITLE);
        int[] actIndex = new int[values.length];
        int[] expIndex = new int[] {0, 3, 1, 2};
        _tabletest.add(values[0]);
        _tabletest.add(values[1]);
        _tabletest.add(values[2]);
        _tabletest.add(values[3]);
        for (int i = 0; i < values.length; i++) {
            actIndex[i] = _tabletest.getIndex(i);
        }
        assertTrue(Arrays.toString(expIndex).
                equals(Arrays.toString(actIndex)));
    }
}
