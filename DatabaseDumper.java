import java.util.List;
import java.sql.Connection;

/**
 * Interface for 201 Coursework which defines the methods that need to be
 * implemented in order for the work to be marked and tested againast different
 * databases.
 */
public abstract class DatabaseDumper {

    /**
     * Connection to a database which is passed in the constructor. The connection
     * Dumper should NOT close the connection
     */
    private final Connection conn;
    /**
     * The type of the database e.g.sqlite or mysql
     */
    private final String type;

    /**
     * 
     * @param c            a connection to be used which talks to the database which
     *                     is being dumped
     * @param databaseType is the type of database e.g. sqlite, mysql, derby
     */
    DatabaseDumper(Connection c, String databaseType) {
        conn = c;
        type = databaseType;
    }

    /**
     * 
     * @param c a connection to be used which talks to the database which is being
     *          dumped
     */
    DatabaseDumper(Connection c) {
        conn = c;
        type = c.getClass().getCanonicalName();
    }

    /**
     * 
     * @return the database connection provided in the constructor
     */
    protected Connection getConnection() {
        return conn;
    }

    /**
     * 
     * @return the type of database e.g. sqlite, derby etc. If the user did not
     *         specify the type, the canonical name of the connection will be used.
     */
    protected String getDatabaseType() {
        return type;
    }

    /**
     * 
     * @return a list of table names
     */
    public abstract List<String> getTableNames();

    /**
     * 
     * @return a list of view names
     */
    public abstract List<String> getViewNames();

    /**
     * 
     * @param tableName to generate the create statement for
     * @return the create statement needed to create the table
     * Preferably, each line should have a different purpose e.g.
     * a seperate line for each attribute
     * <br>
     * c)	A single text file as in (b) above, but also contains at the start the CREATE TABLE statements that create the tables that your text file will document. (But without the primary and foreign keys being indicated).  
 <br>
CREATE TABLE give_course (  s_id VARCHAR(4),  c_id VARCHAR(3)  <br>
);  <br>
d)	A single text file as in (c) above, but the CREATE TABLE statements include indicators of primary keys. <br> 
 <br>
CREATE TABLE give_course (  s_id VARCHAR(4),  c_id VARCHAR(3),  PRIMARY KEY (s_id, c_id )  <br>
);  <br>
 <br>
e)	A single text file as in (d) above, but including foreign keys.  <br>
 <br>
CREATE TABLE give_course (  s_id VARCHAR(4),  c_id VARCHAR(3),  PRIMARY KEY(s_id, c_id ),  <br>
FOREIGN KEY (s_id) REFERENCES staff(s_id),  <br>
FOREIGN KEY (c_id) REFERENCES courses(c_id)  ); <br> 
<br> 
     */
    public abstract String getDDLForTable(String tableName);

    /**
     * A method to get the set of index creation instructions...<br>
     * e.g.<br>
     * CREATE INDEX planets_id ON planets (planet_id); 
     * ...
     * @return a String of CREATE INDEX statements.
     */
    public abstract String getDatabaseIndexes();

    /**
     * 
     * @param tableName to generate the inserts for
     * @return a String which will be one or more lines of INSERT INTO ..
     */
    public abstract String getInsertsForTable(String tableName);

    /**
     * 
     * @param viewName the name of the view to generate the DDL for
     * @return the DDL (CREATE VIEW ...) for a named view
     */
    public abstract String getDDLForView(String viewName);

    /**
     * 
     * @return A String which could either be displayed or saved to a file. It will
     *         contain important DROP statements and CREATE IF NOT EXISTS....
     * (g) As with (e) but with code to ensure the CREATE TABLE statements are in the ‘correct’ order. 
     * NB each CREATE, DROP INSERT etc MUST be seperated by a  -- -- -- line e.g.<br>
     * -- -- --<br>
     * CREATE TABLE A();<br>
     * -- -- --<br>
     * CREATE TABLE B();<br>
     */
    public abstract String getDumpString();

    /**
     * 
     * @param fileName dump the dumpString to the named file
     */
    public abstract void dumpToFileName(String fileName);

    /**
     * This method should print out the create table and insert statements in the
     * correct order. The method will be used to create any dump file from the
     * command prompt
     */
    public abstract void dumpToSystemOut();

}