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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.temperatureconverter.Dao.TempRecordDAO;
import com.example.temperatureconverter.Dao.SpeedDistanceTimeDAO;
import com.example.temperatureconverter.Dao.SpeedDistanceTimeUnitDAO;
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
            statement.execute("DROP TABLE IF EXISTS speed_distance_time_records");
            statement.execute("DROP TABLE IF EXISTS speed_distance_time_unit_translation");
            statement.execute("DROP TABLE IF EXISTS speed_distance_time_units");

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
                    CREATE TABLE speed_distance_time_units (
                        id INT PRIMARY KEY,
                        name VARCHAR(50) NOT NULL,
                        unit_type VARCHAR(20) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE speed_distance_time_unit_translation (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        unit_id INT NOT NULL,
                        lang VARCHAR(5) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        description VARCHAR(255),
                        UNIQUE(unit_id, lang)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE speed_distance_time_records (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        distance_value DOUBLE NOT NULL,
                        distance_unit_id INT NOT NULL,
                        time_value DOUBLE NOT NULL,
                        time_unit_id INT NOT NULL,
                        speed_value DOUBLE NOT NULL,
                        speed_unit_id INT NOT NULL
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

            statement.execute("""
                    INSERT INTO speed_distance_time_units (id, name, unit_type)
                    VALUES
                    (1, 'Meter', 'distance'),
                    (2, 'Kilometer', 'distance'),
                    (3, 'Second', 'time'),
                    (4, 'Hour', 'time'),
                    (5, 'Meters per second', 'speed')
                    """);

            statement.execute("""
                    INSERT INTO speed_distance_time_unit_translation
                    (unit_id, lang, name, description)
                    VALUES
                    (1, 'en', 'Meter', 'Distance in meters'),
                    (2, 'en', 'Kilometer', 'Distance in kilometers'),
                    (3, 'en', 'Second', 'Time in seconds'),
                    (4, 'en', 'Hour', 'Time in hours'),
                    (5, 'en', 'Meters per second', 'Speed in meters per second'),
                    (1, 'fi', 'Metri', 'Etäisyys metreinä')
                    """);
        }
    }

    @Test
    void getUnitNameReturnsNameForUnitId() throws SQLException {
        TemperatureUnitDAO dao = testUnitDAO();

        assertEquals("Celsius", dao.getUnitName(1));
    }

    @Test
    void getUnitNameThrowsWhenUnitIdDoesNotExist() {
        TemperatureUnitDAO dao = testUnitDAO();

        assertThrows(SQLException.class, () -> dao.getUnitName(999));
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

    @Test
    void saveRecordStoresExplicitLanguage() throws SQLException {
        TempRecordDAO dao = testRecordDAO();

        dao.saveRecord(0, 1, 32, 2, "fi");

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT language FROM temperature_records")) {
            assertTrue(resultSet.next());
            assertEquals("fi", resultSet.getString("language"));
        }
    }

    @Test
    void getLastFiveRecordsReturnsNewestFiveRecords() throws SQLException {
        TempRecordDAO dao = testRecordDAO();

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            for (int i = 1; i <= 6; i++) {
                statement.executeUpdate("""
                        INSERT INTO temperature_records
                        (temperature, unit_id, converted_temperature,
                         converted_unit_id, language, created_at)
                        VALUES (%d, 1, %d, 2, 'en', TIMESTAMP '2026-01-01 00:00:%02d')
                        """.formatted(i, i + 30, i));
            }
        }

        List<com.example.temperatureconverter.Model.TempRecord> records =
                dao.getLastFiveRecords();

        assertEquals(5, records.size());
        assertTrue(records.get(0).toString().startsWith("6.0 Celsius"));
        assertTrue(records.get(4).toString().startsWith("2.0 Celsius"));
    }

    @Test
    void getUnitIdFindsTranslatedSpeedDistanceTimeUnit() throws SQLException {
        SpeedDistanceTimeUnitDAO dao = testSpeedDistanceTimeUnitDAO();

        assertEquals(1, dao.getUnitId("Metri", "distance", "fi"));
    }

    @Test
    void getUnitIdUsesEnglishFallbackForSpeedDistanceTimeUnit() throws SQLException {
        SpeedDistanceTimeUnitDAO dao = testSpeedDistanceTimeUnitDAO();

        assertEquals(2, dao.getUnitId("Kilometer", "distance", "fi"));
    }

    @Test
    void getUnitIdThrowsForWrongSpeedDistanceTimeUnitType() {
        SpeedDistanceTimeUnitDAO dao = testSpeedDistanceTimeUnitDAO();

        assertThrows(SQLException.class,
                () -> dao.getUnitId("Meter", "time", "en"));
    }

    @Test
    void getSpeedDistanceTimeUnitsFiltersByTypeAndUsesFallback() {
        SpeedDistanceTimeUnitDAO dao = testSpeedDistanceTimeUnitDAO();

        List<Unit> units = dao.getUnits("distance", "fi");

        assertEquals(2, units.size());
        assertEquals("Metri", units.get(0).getName());
        assertEquals("Kilometer", units.get(1).getName());
        assertEquals("Etäisyys metreinä", units.get(0).getDescription());
    }

    @Test
    void saveSpeedDistanceTimeRecordInsertsAllValues() throws SQLException {
        SpeedDistanceTimeDAO dao = testSpeedDistanceTimeDAO();

        dao.saveRecord(100.5, 1, 2.0, 4, 50.25, 5);

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT distance_value, distance_unit_id, time_value, " +
                             "time_unit_id, speed_value, speed_unit_id " +
                             "FROM speed_distance_time_records")) {
            assertTrue(resultSet.next());
            assertEquals(100.5, resultSet.getDouble("distance_value"));
            assertEquals(1, resultSet.getInt("distance_unit_id"));
            assertEquals(2.0, resultSet.getDouble("time_value"));
            assertEquals(4, resultSet.getInt("time_unit_id"));
            assertEquals(50.25, resultSet.getDouble("speed_value"));
            assertEquals(5, resultSet.getInt("speed_unit_id"));
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

    private SpeedDistanceTimeUnitDAO testSpeedDistanceTimeUnitDAO() {

        return new SpeedDistanceTimeUnitDAO() {
            @Override
            protected Connection getConnection() throws SQLException {
                return openConnection();
            }
        };
    }

    private SpeedDistanceTimeDAO testSpeedDistanceTimeDAO() {

        return new SpeedDistanceTimeDAO() {
            @Override
            protected Connection getConnection() throws SQLException {
                return openConnection();
            }
        };
    }
}