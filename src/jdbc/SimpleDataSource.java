package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDataSource {

    private final String URL_DATABASE = "jdbc:postgresql://localhost:5432/Homework";
    private final String USER = "postgres";
    private final String PASSWORD = "1029384756Qq";


    public Connection openConnection() {
        try {
            return DriverManager.getConnection(URL_DATABASE, USER, PASSWORD);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
