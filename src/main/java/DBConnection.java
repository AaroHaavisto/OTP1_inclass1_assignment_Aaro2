import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input != null) {
                properties.load(input);
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not load database properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {

        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String database = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        String url;

        if (host != null) {
            url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
        } else {
            url = properties.getProperty("db.url");
            user = properties.getProperty("db.user");
            password = properties.getProperty("db.password");
        }

        return DriverManager.getConnection(url, user, password);
    }
}