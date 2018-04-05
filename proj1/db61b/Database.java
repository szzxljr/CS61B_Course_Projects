package db61b;

import java.util.HashMap;

/** A collection of Tables, indexed by name.
 *  @author Jiarong Li*/
class Database {
    /** An empty database. */
    public Database() {
        _database = new HashMap<>();
    }

    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        return _database.getOrDefault(name, null);
    }

    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        _database.put(name, table);
    }

    /** HashMasp to store data. */
    private static HashMap<String, Table> _database;
}
