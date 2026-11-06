import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.temperatureconverter.Dao.TempRecordDAO;
import com.example.temperatureconverter.Dao.TemperatureUnitDAO;
import com.example.temperatureconverter.Model.Unit;

class DatabaseDaoTest {

    private static final String DATABASE_URL =
            "jdbc:h2:mem:temperature_test;DB_CLOSE_DELAY=-1";

    private static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    @BeforeEach
    void createTables() throws SQLException {

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("DROP TABLE IF EXISTS temperature_records");
            statement.execute("DROP TABLE IF EXISTS temperature_unit_translation");
            statement.execute("DROP TABLE IF EXISTS temperature_units");

            statement.execute("""
                    CREATE TABLE temperature_units (
                        id INT PRIMARY KEY,
                        name VARCHAR(50) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE temperature_unit_translation (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        unit_id INT NOT NULL,
                        lang VARCHAR(5) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        description VARCHAR(255),
                        UNIQUE(unit_id, lang)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE temperature_records (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      temperature DOUBLE NOT NULL,
                      unit_id INT NOT NULL,
                      converted_temperature DOUBLE NOT NULL,
                      converted_unit_id INT NOT NULL,
                      language VARCHAR(5) NOT NULL DEFAULT 'en',
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                  )
                    """);

            statement.execute("""
                    INSERT INTO temperature_units (id, name)
                    VALUES
                    (1, 'Celsius'),
                    (2, 'Fahrenheit'),
                    (3, 'Kelvin')
                    """);

            statement.execute("""
                    INSERT INTO temperature_unit_translation
                    (unit_id, lang, name, description)
                    VALUES
                    (1, 'en', 'Celsius', 'Temperature in Celsius'),
                    (2, 'en', 'Fahrenheit', 'Temperature in Fahrenheit'),
                    (3, 'en', 'Kelvin', 'Temperature in Kelvin'),
                    (1, 'fi', 'Celsius FI', 'Lämpötila celsiusasteina')
                    """);
        }
    }

    @Test
    void getUnitIdReturnsIdForUnit() throws SQLException {

        TemperatureUnitDAO dao = testUnitDAO();

        assertEquals(2, dao.getUnitId("Fahrenheit", "en"));
    }

    @Test
    void getUnitIdThrowsWhenUnitDoesNotExist() {

        TemperatureUnitDAO dao = testUnitDAO();

        assertThrows(SQLException.class,
                () -> dao.getUnitId("Unknown", "en"));
    }

    @Test
    void getUnitsReturnsAllUnitNames() {

        TemperatureUnitDAO dao = testUnitDAO();

        List<Unit> units = dao.getUnits("en");
        List<String> names = new ArrayList<>();

        for (Unit unit : units) {
            names.add(unit.getName());
        }

        assertEquals(
                List.of("Celsius", "Fahrenheit", "Kelvin"),
                names
        );
    }

    @Test
    void getUnitsUsesEnglishFallbackWhenTranslationIsMissing() {

        TemperatureUnitDAO dao = testUnitDAO();

        List<Unit> units = dao.getUnits("fi");
        List<String> names = new ArrayList<>();

        for (Unit unit : units) {
            names.add(unit.getName());
        }

        assertEquals(
                List.of("Celsius FI", "Fahrenheit", "Kelvin"),
                names
        );
    }

    @Test
    void saveRecordInsertsConvertedTemperature() throws SQLException {

        TempRecordDAO dao = testRecordDAO();

        dao.saveRecord(100.0, 1, 212.0, 2);

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT temperature, unit_id, converted_temperature, converted_unit_id " +
                             "FROM temperature_records")) {

            resultSet.next();

            assertEquals(100.0, resultSet.getDouble("temperature"));
            assertEquals(1, resultSet.getInt("unit_id"));
            assertEquals(212.0, resultSet.getDouble("converted_temperature"));
            assertEquals(2, resultSet.getInt("converted_unit_id"));
        }
    }

    private TemperatureUnitDAO testUnitDAO() {

        return new TemperatureUnitDAO() {
            @Override
            protected Connection getConnection() throws SQLException {
                return openConnection();
            }
        };
    }

    private TempRecordDAO testRecordDAO() {

        return new TempRecordDAO() {
            @Override
            protected Connection getConnection() throws SQLException {
                return openConnection();
            }
        };
    }
}