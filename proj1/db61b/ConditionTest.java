package db61b;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import db61b.Table.ValueList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Condition.java .
 *  @author Jiarong Li */

public class ConditionTest {

    private Table _table;
    private static final String NAME = "TEST";
    private static final String[] TITLE =
            new String[] {"SID", "SID2", "MAJOR", "Year"};
    private static Column _col1, _col2;
    private static Condition _cond1, _cond2;
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
        _col2 = new Column(TITLE[1], _table);
        _cond1 = new Condition(_col1, ">=", _col2);
    }

    @Test
    public void testTest() {
        Integer greater = 1;
        assertFalse(_cond1.test(0));
        assertTrue(_cond1.test(1));
        assertTrue(_cond1.test(2));
        assertFalse(_cond1.test(3));
    }

    @Test
    public void testGetRelation() {
        assertTrue(_cond1.getRelation().contains(">="));
    }

    @Test
    public void testGetCol1() {
        assertTrue(values[0][0].equals(_col1.getFrom(0)));
    }

    @Test
    public void testGetCol2() {
        assertTrue(values[0][1].equals(_col2.getFrom(0)));
    }
}
