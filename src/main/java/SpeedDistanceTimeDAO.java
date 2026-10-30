import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SpeedDistanceTimeDAO {

    public void saveRecord(
            double distanceValue, int distanceUnitId,
            double timeValue, int timeUnitId,
            double speedValue, int speedUnitId) throws SQLException {

        String sql = "INSERT INTO speed_distance_time_records "
                + "(distance_value, distance_unit_id, "
                + "time_value, time_unit_id, "
                + "speed_value, speed_unit_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, distanceValue);
            statement.setInt(2, distanceUnitId);
            statement.setDouble(3, timeValue);
            statement.setInt(4, timeUnitId);
            statement.setDouble(5, speedValue);
            statement.setInt(6, speedUnitId);

            statement.executeUpdate();
        }
    }
}
