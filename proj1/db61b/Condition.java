package db61b;

import java.util.ArrayList;
import java.util.List;

/** Represents a single 'where' condition in a 'select' command.
 *  @author Jiarong Li */
class Condition {

    /** A Condition representing COL1 RELATION COL2, where COL1 and COL2
     *  are column designators. and RELATION is one of the
     *  strings "<", ">", "<=", ">=", "=", or "!=". */
    Condition(Column col1, String relation, Column col2) {
        _col1 = col1;
        _col2 = col2;
        _relation = relation;
        this._relationList = new ArrayList<>();
        switch (relation) {
        case "<":
            this._relationList.add(LOWER);
            break;
        case "<=":
            this._relationList.add(LOWER);
            this._relationList.add(EQUAL);
            break;
        case "=":
            this._relationList.add(EQUAL);
            break;
        case "!=":
            this._relationList.add(GREATER);
            this._relationList.add(LOWER);
            break;
        case ">=":
            this._relationList.add(GREATER);
            this._relationList.add(EQUAL);
            break;
        case ">":
            this._relationList.add(GREATER);
            break;
        default:
            throw new DBException("Error: Relation Invalid.");
        }

    }

    /** A Condition representing COL1 RELATION 'VAL2', where COL1 is
     *  a column designator, VAL2 is a literal value (without the
     *  quotes), and RELATION is one of the strings "<", ">", "<=",
     *  ">=", "=", or "!=".
     */
    Condition(Column col1, String relation, String val2) {
        this(col1, relation, new ColumnLiteral(val2));
        _val2 = val2;
    }

    /** Assuming that ROWS are row indices in the respective tables
     *  from which my columns are selected, returns the result of
     *  performing the test I denote. */
    boolean test(Integer... rows) {
        String s1 = _col1.getFrom(rows);
        String s2 = _col2.getFrom(rows);
        int compare = s1.compareTo(s2);
        if (compare < 0 && this._relationList.contains(LOWER)
                || compare == 0 && this._relationList.contains(EQUAL)
                || compare > 0 && this._relationList.contains(GREATER)) {
            return true;
        }
        return false;
    }

    /** Return true iff ROWS satisfies all CONDITIONS. */
    static boolean test(List<Condition> conditions, Integer... rows) {
        for (Condition cond : conditions) {
            if (!cond.test(rows)) {
                return false;
            }
        }
        return true;
    }
    /** Get column1.
     * @return col1*/
    public Column getCol1() {
        return _col1;
    }

    /** Get column2.
     * @return col2*/
    public Column getCol2() {
        return _col2;
    }

    /** Get Relation.
     * @return relation*/
    public String getRelation() {
        return _relation;
    }
    /** The operands of this condition.  _col2 is null if the second operand
     *  is a literal. */
    private Column _col1, _col2;
    /** relation string. */
    private String _relation;
    /** Second operand, if literal (otherwise null). */
    private String _val2;
    /** relationList. */
    private ArrayList<Integer> _relationList;
    /** Eql GREATER LOWER. */
    private static final int EQUAL = 0, GREATER = 1, LOWER = -1;
}
