package db61b;

/** A ColumnLiteral is a fake Column to make
 * select condition work.
 * @author Jiarongli */
public class ColumnLiteral extends Column {

    /** A ColumnLiteral with string value.
     * @param value value
     * @param tables tables */
    ColumnLiteral(String value, Table... tables) {
        super("ColumnLiteral", new Table(_titles));
        this._value = value;
    }

    @Override
    String getFrom(Integer... rows) {
        return _value;
    }

    /** Store value. */
    private final String _value;
    /** Get value.
     * @return _value */
    String getValue() {
        return _value;
    }
    /** Get new fake table titles. */
    private static String[] _titles = new String[] {"ColumnLiteral"};
}
