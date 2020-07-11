import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws SQLException {
        SimpleDataSource dataSource = new SimpleDataSource();
        Connection connection = dataSource.openConnection();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from branch");
        while (resultSet.next()) {
            System.out.println("Id" + resultSet.getString("id"));
            System.out.println("Name " + resultSet.getString("name"));
            System.out.println("Address " + resultSet.getString("address"));
            System.out.println("town id " + resultSet.getString("town_id"));
        }
        resultSet.close();
        statement.close();
        connection.close();
    }
}
