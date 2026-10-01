import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TempRecordDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public void saveRecord(double temperature, int unitId,
                           double convertedTemperature, int convertedUnitId) throws SQLException {

        String sql = """
                INSERT INTO temperature_records
                (temperature, unit_id, converted_temperature, converted_unit_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, temperature);
            statement.setInt(2, unitId);
            statement.setDouble(3, convertedTemperature);
            statement.setInt(4, convertedUnitId);

            statement.executeUpdate();
        }
    }
}