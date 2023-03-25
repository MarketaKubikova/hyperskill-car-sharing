package carsharing;

import org.h2.jdbc.JdbcSQLSyntaxErrorException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL_PREFIX = "jdbc:h2:file:";
    public static void main(String[] args) throws SQLException {
        String dbName = "carsharing";

        for (int i = 0; i < args.length; i++) {
            if ("-databaseFileName".equals(args[i])) {
                dbName = args[i + 1];
                break;
            }
        }

        try {
            Class.forName(JDBC_DRIVER);
            String filePath = "./src/carsharing/db/" + dbName;
            Connection connection = DriverManager.getConnection(DB_URL_PREFIX + filePath);
            connection.setAutoCommit(true);

            Application.printMainMenu(connection);

            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Wrong class");
            e.printStackTrace();
        } catch (JdbcSQLSyntaxErrorException e) {
            System.out.println("Table exists");
            e.printStackTrace();
        }
    }
}
