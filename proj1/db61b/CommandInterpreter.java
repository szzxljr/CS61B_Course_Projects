package db61b;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static db61b.Utils.*;
import static db61b.Tokenizer.*;

/** An object that reads and interprets a sequence of commands from an
 *  input source.
 *  @author Jiarong Li*/
class CommandInterpreter {
    /** A new CommandInterpreter executing commands read from INP, writing
     *  prompts on PROMPTER, if it is non-null. */
    CommandInterpreter(Scanner inp, PrintStream prompter) {
        _input = new Tokenizer(inp, prompter);
        _database = new Database();
    }

    /** Parse and execute one statement from the token stream.  Return true
     *  iff the command is something other than quit or exit. */
    boolean statement() {
        switch (_input.peek()) {
        case "create":
            createStatement();
            break;
        case "load":
            loadStatement();
            break;
        case "exit": case "quit":
            exitStatement();
            return false;
        case "*EOF*":
            return false;
        case "insert":
            insertStatement();
            break;
        case "print":
            printStatement();
            break;
        case "select":
            selectStatement();
            break;
        case "store":
            storeStatement();
            break;
        default:
            throw error("unrecognizable command");
        }
        return true;
    }

    /** Parse and execute a create statement from the token stream. */
    void createStatement() {
        _input.next("create");
        _input.next("table");
        String name = name();
        Table table;
        if (_input.nextIf("as")) {
            table = selectClause();
        } else {
            table = tableDefinition();
        }
        _input.next(";");
        _database.put(name, table);
    }

    /** Parse and execute an exit or quit statement. Actually does nothing
     *  except check syntax, since statement() handles the actual exiting. */
    void exitStatement() {
        if (!_input.nextIf("quit")) {
            _input.next("exit");
        }
        _input.next(";");
    }

    /** Parse and execute an insert statement from the token stream. */
    void insertStatement() {
        _input.next("insert");
        _input.next("into");
        Table table = tableName();
        _input.next("values");
        int cols = table.columns();
        while (true) {
            int k;
            _input.next("(");
            ArrayList<String> valuesList = new ArrayList<>();
            valuesList.add(this.literal());
            while (_input.nextIf(",")) {
                valuesList.add(this.literal());
            }
            String[] values =
                    valuesList.toArray(new String[valuesList.size()]);
            table.add(values);
            _input.next(")");
            if (!_input.nextIf(",")) {
                break;
            }
        }
        _input.next(";");

    }

    /** Parse and execute a load statement from the token stream. */
    void loadStatement() {
        _input.next("load");
        String name = this.name();
        Table table = Table.readTable(name);
        _database.put(name, table);
        System.out.printf("Loaded %s.db%n", name);
        _input.next(";");
    }

    /** Parse and execute a store statement from the token stream. */
    void storeStatement() {
        _input.next("store");
        String name = _input.peek();
        Table table = tableName();
        table.writeTable(name);
        System.out.printf("Stored %s.db%n", name);
        _input.next(";");
    }

    /** Parse and execute a print statement from the token stream. */
    void printStatement() {
        _input.next("print");
        String name = _input.peek();
        Table table = tableName();
        System.out.println("Contents of " + name + ":");
        table.print();
        _input.next(";");
    }

    /** Parse and execute a select statement from the token stream. */
    void selectStatement() {
        Table table = selectClause();
        System.out.println("Search results:");
        table.print();
        _input.next(";");
    }

    /** Parse and execute a table definition, returning the specified
     *  table. */
    Table tableDefinition() {
        Table table;
        if (_input.nextIf("(")) {
            ArrayList<String> columnTitles = new ArrayList<>();
            columnTitles.add(this.columnName());
            while (_input.nextIf(",")) {
                columnTitles.add(this.columnName());
            }
            _input.next(")");
            table = new Table(columnTitles);
        } else {
            _input.next("as");
            table = this.selectClause();
        }
        return table;
    }

    /** Parse and execute a select clause from the token stream, returning the
     *  resulting table. */
    Table selectClause() {
        _input.next("select");
        List<String> columnNames = new ArrayList<>();
        List<Condition> conditions;
        do {
            columnNames.add(_input.next());
        } while (_input.nextIf(","));
        String[] strNames =
                columnNames.toArray(new String[columnNames.size()]);
        _input.next("from");
        Table tableRaw1 = tableName();
        Table table;
        if (_input.nextIf(",")) {
            Table tableRaw2 = tableName();
            conditions = this.conditionClause(tableRaw1, tableRaw2);
            table = tableRaw1.select(tableRaw2, columnNames, conditions);
        } else {
            List<String> titles = Arrays.asList(tableRaw1.getTitles());
            for (int row = 0; row < strNames.length; row++) {
                if (!titles.contains(strNames[row])) {
                    throw error("unknown column: (%s)", strNames[row]);
                }
            }
            conditions = this.conditionClause(tableRaw1);
            table = tableRaw1.select(columnNames, conditions);
        }
        return table;
    }

    /** Parse and return a valid name (identifier) from the token stream. */
    String name() {
        return _input.next(Tokenizer.IDENTIFIER);
    }

    /** Parse and return a valid column name from the token stream. Column
     *  names are simply names; we use a different method name to clarify
     *  the intent of the code. */
    String columnName() {
        return name();
    }

    /** Parse a valid table name from the token stream, and return the Table
     *  that it designates, which must be loaded. */
    Table tableName() {
        String name = name();
        Table table = _database.get(name);
        if (table == null) {
            throw error("unknown table: %s", name);
        }
        return table;
    }

    /** Parse a literal and return the string it represents (i.e., without
     *  single quotes). */
    String literal() {
        String lit = _input.next(Tokenizer.LITERAL);
        return lit.substring(1, lit.length() - 1).trim();
    }

    /** Parse a relation and return the string it represents. */
    String relation() {
        return _input.next(Tokenizer.RELATION);
    }

    /** Parse and return a list of Conditions that apply to TABLES from the
     *  token stream.  This denotes the conjunction (`and') of zero
     *  or more Conditions. */
    ArrayList<Condition> conditionClause(Table... tables) {
        ArrayList<Condition> conditions = new ArrayList<>();
        if (_input.nextIf("where")) {
            do {
                conditions.add(this.condition(tables));
            } while (_input.nextIf("and"));
        }
        return conditions;
    }

    /** Parse and return a Condition that applies to TABLES from the
     *  token stream. */
    Condition condition(Table... tables) {
        Condition con;
        String columnTitle1 = this.columnName();
        Column column1 = new Column(columnTitle1, tables);
        String relation = this.relation();
        Column column2;
        if (_input.peek().startsWith("'")) {
            String value2 = this.literal();
            con = new Condition(column1, relation, value2);
        } else {
            String columnTitle2 = this.columnName();
            column2 = new Column(columnTitle2, tables);
            con = new Condition(column1, relation, column2);
        }
        return con;
    }

    /** Advance the input past the next semicolon. */
    void skipCommand() {
        while (true) {
            try {
                while (!_input.nextIf(";") && !_input.nextIf("*EOF*")) {
                    _input.next();
                }
                return;
            } catch (DBException excp) {
                /* No action */
            }
        }
    }

    /** The command input source. */
    private Tokenizer _input;
    /** Database containing all tables. */
    private Database _database;
}
