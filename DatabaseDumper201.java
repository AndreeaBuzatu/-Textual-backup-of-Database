import java.sql.*;
import java.util.*;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Name: Andreea Buzatu
 * Student ID: 34683089
 */

public class DatabaseDumper201 extends DatabaseDumper {

    /**
     * @param c connection which the dumper should use
     * @param type a string naming the type of database being connected to e.g. sqlite
     */
    public DatabaseDumper201(Connection c,String type) {
        super(c,type);
    }
    /**
     * @param c connection to a database which will have a sql dump create for
     */

    public DatabaseDumper201(Connection c) {
        super(c,c.getClass().getCanonicalName());
    }

    public List<String> getTableNames() {
        List<String> result = new ArrayList<>();
        Connection c = super.getConnection(); //get connection

        try{
            String[] types = {"TABLE"}; //Array of strings that contain the types of tables to include
            DatabaseMetaData dbmd =c.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, "%", types); //Retrieve a description of the tables that are available

            while (rs.next()){
                result.add(rs.getString("TABLE_NAME"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<String> getViewNames() {

        List<String> views = new ArrayList<>();
        Connection c = super.getConnection(); //Get connection

        try{
            DatabaseMetaData dbmd =c.getMetaData();
            String[] types = {"VIEW"};  //Array of strings that contain the types of tables to include
            ResultSet rs = dbmd.getTables(null, null, "%", types); //Retrieve a description of the tables that are available

            if(rs != null) {
                while (rs.next()) {
                    views.add(rs.getString("TABLE_NAME"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return views;

    }

    @Override
    public String getDDLForTable(String tableName) {

        Connection c = super.getConnection();
        String create_table = new String();

        try{
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " "; //Create SQL statement to be executed

            ResultSet rs = stmt.executeQuery( sql ); //Get a table as a resultSet
            ResultSetMetaData rsmd = rs.getMetaData(); //Get metadata of table
            DatabaseMetaData dbmd = c.getMetaData();

            ResultSet primary_keys = dbmd.getPrimaryKeys(null, null, tableName);
            ResultSet foreign_keys = dbmd.getImportedKeys(null, null, tableName);

            List<String> pk = new ArrayList<>();
            List<String> fk = new ArrayList<>();
            List<String> fk_references = new ArrayList<>();
            List<String> imported_pk_colName = new ArrayList<>();

            int colCount = rsmd.getColumnCount(); //Get number of attributes in the table

            StringBuilder sb = new StringBuilder(1024); //Builds a string of the CREATE TABLE statement
            if ( colCount > 0 ) {
                sb.append( "CREATE TABLE \"" ).append( tableName ).append( "\" (\n" );
            }

            for ( int i = 1; i <= colCount; i ++ ) {
                if ( i > 1 ) {
                    sb.append( ",\n" );   //If there is more than 1 column, a new line is created
                }

                String colName = rsmd.getColumnName( i );
                String colType = rsmd.getColumnTypeName( i );

                sb.append( "   \"").append( colName ).append( "\" " ).append( colType );

                int precision = rsmd.getPrecision( i ); //Get the specified size of the column
                if ( precision != 0 ) {
                    sb.append( "( " ).append( precision ).append( " )" );
                }

                if(i == colCount){
                    sb.append( ",\n" );
                }
            }

            //Get primary keys table
            while ( primary_keys.next() ){
                pk.add( primary_keys.getString("COLUMN_NAME") );
            }

            //Get foreign keys of table
            while ( foreign_keys.next() ){
                fk.add( foreign_keys.getString("FKCOLUMN_NAME") );
                fk_references.add( foreign_keys.getString("PKTABLE_NAME") );
                imported_pk_colName.add(foreign_keys.getString("PKCOLUMN_NAME") );
            }

            if (pk.size() != 0) {
                sb.append("   PRIMARY KEY (");
                for (int j = 0; j < pk.size(); j++) {
                    if (j > 0) {
                        sb.append( ", " );
                    }
                    sb.append("\"").append(pk.get(j)).append("\"");
                }
                sb.append(")");
            }
            if (fk.size() != 0) {
                for (int j = 0; j < fk.size(); j++) {
                    if(j >= 0) {
                        sb.append( "," );
                    }
                    sb.append( "\n   FOREIGN KEY (\"" ).append( fk.get(j) ).append( "\") REFERENCES \"" ).append(fk_references.get(j) ).append( "\" (\" " ).append( imported_pk_colName.get(j) ).append( "\")" );
                }
            }
            sb.append( "\n);" );
            create_table = sb.toString();

        }catch (Exception e){
            e.printStackTrace();
        }

        return create_table;
    }

    @Override
    public String getInsertsForTable(String tableName) {

        Connection c = super.getConnection();
        String inserts = new String();

        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " "; //Create SQL statement to be executed

            ResultSet rs = stmt.executeQuery(sql); //Get a table as a resultSet
            ResultSetMetaData rsmd = rs.getMetaData(); //Get metadata of table

            StringBuilder sb = new StringBuilder(1024);
            int colCount = rsmd.getColumnCount();
            String columnName = "";
            for(int i = 1; i <= colCount; i++) {
                columnName += rsmd.getColumnName(i);
                if (i < colCount)
                    columnName += ",";
            }

            CharSequence test = "\'";
            String doubleQuote ="\"";
            while(rs.next()){
                String columnValues = "";
                for(int i = 1; i <= colCount; i++){
                    columnValues += "'";
                    String rowValue = rs.getString(i);
                    if(rowValue.contains(test)){
                        rowValue = rowValue.replace(test.toString(),doubleQuote); //In case the string contains ' we replace it with "
                    }
                    columnValues += rowValue;
                    columnValues += "'";
                    if(i < colCount)
                        columnValues += ", ";
                }
                sb.append( "INSERT INTO " ).append( tableName ).append("(").append(columnName).append(") ").append( "VALUES(" ).append(columnValues).append(");\n");
            }

            inserts = sb.toString();

        }catch (Exception e){
            e.printStackTrace();
        }

        return inserts;
    }

    @Override
    public String getDDLForView(String viewName) {

        Connection c = super.getConnection();
        String create_view = new String();

        try{
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + viewName + " ";

            ResultSet rs = stmt.executeQuery( sql ); //Get a table as a resultSet
            ResultSetMetaData rsmd = rs.getMetaData(); //Get metadata of table
            DatabaseMetaData dbmd = c.getMetaData();

            int colCount = rsmd.getColumnCount(); //Number of attributes of the table

            StringBuilder sb = new StringBuilder(1024);
            if ( colCount > 0 ) {
                sb.append( "CREATE TABLE " ).append("\"view_").append( viewName ).append( "\" (\n" );
            }

            for ( int i = 1; i <= colCount; i ++ ) {
                if ( i > 1 ) {
                    sb.append( ",\n" );
                }

                String colName = rsmd.getColumnName( i ); //Get column name
                String colType = rsmd.getColumnTypeName( i ); //Get column type

                sb.append( "   \"").append( colName ).append( "\" " ).append( colType );

                int precision = rsmd.getPrecision( i );
                if ( precision != 0 ) {
                    sb.append( "(" ).append( precision ).append( ")" );
                }
            }
            sb.append( "\n);" );
            create_view = sb.toString();

        }catch (Exception e){
            e.printStackTrace();
        }

        return create_view;
    }

    @Override
    public String getDumpString() {
        Connection c = super.getConnection();
        String dump_string = new String();

        try{
            DatabaseDumper201 dumper = new DatabaseDumper201(c); ////Make object of the class
            DatabaseMetaData dbmd = c.getMetaData();
            List<String> tableNames = dumper.getTableNames();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < tableNames.size(); i ++){

                String create_table = dumper.getDDLForTable((tableNames.get(i))); //Call the method getDDLForTable()
                String to_insert = "IF NOT EXISTS ";
                String create_table_dump = new String();
                int index = 12;

                for (int j = 0; j < create_table.length(); j++){
                    create_table_dump += create_table.charAt(j);
                    if (j == index){
                        create_table_dump += to_insert;
                    }
                }

                sb.append( "-- -- --\n");
                sb.append( create_table_dump );
                sb.append( "\n-- -- --\n");
                sb.append( dumper.getInsertsForTable(tableNames.get(i)) ); //Call method getInsertsForTable()
            }

            sb.append( "-- -- --\n" );
            sb.append( dumper.getDatabaseIndexes()); //Call method getDatabaseIndexes()

            dump_string = sb.toString();

        }catch (Exception e){
            e.printStackTrace();
        }
        return dump_string;
    }

    @Override
    public void dumpToFileName(String fileName) {

        Connection c = super.getConnection();
        DatabaseDumper201 dumper = new DatabaseDumper201(c);

        String data = dumper.getDumpString();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(data);   //Using write() method of BufferedWriter to write the text into the given file

            writer.close();
        }
        catch(Exception e) {
            e.getStackTrace();
        }

    }

    @Override
    public void dumpToSystemOut() {

        Connection c = super.getConnection();

        try{
            DatabaseDumper201 dumper = new DatabaseDumper201(c);
            System.out.println(dumper.getDumpString());  //print out the result of getDumpString() method
        }catch(Exception e) {
            e.getStackTrace();
        }

    }

    @Override
    public String getDatabaseIndexes() {

        Connection c = super.getConnection();
        String index = new String();


        try {
            DatabaseDumper201 dumper = new DatabaseDumper201(c);
            DatabaseMetaData dbmd = c.getMetaData();
            List<String> tableNames = dumper.getTableNames();
            StringBuilder sb = new StringBuilder();
            List<String> tblName = new ArrayList<>();
            List<String> colName = new ArrayList<>();

            for (int i = 0; i < tableNames.size(); i ++){

                ResultSet rs = dbmd.getIndexInfo(null,null,tableNames.get(i),false,false); //Retrieve a description of the given table's indices

                while(rs.next()){
                    tblName.add(rs.getString("INDEX_NAME")); //Extract index name
                    colName.add(rs.getString("COLUMN_NAME")); //Extract column name
                }

                if(tblName.get(i).contains("autoindex") == false) {
                    sb.append("CREATE INDEX \"").append(tblName.get(i)).append("\" ON ").append(tableNames.get(i)).append(" (\"").append(colName.get(i)).append("\");\n");
                }
            }
           index =  sb.toString();

        }catch(Exception e) {
            e.getStackTrace();
        }

        return index;
    }

}
