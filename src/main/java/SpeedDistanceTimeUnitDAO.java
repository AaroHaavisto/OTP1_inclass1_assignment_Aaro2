
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SpeedDistanceTimeUnitDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public int getUnitId(String unitName, String unitType) throws SQLException {
        String sql = """
                SELECT id FROM speed_distance_time_units
                WHERE unit_name = ? AND unit_type = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, unitName);
            statement.setString(2, unitType);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        throw new SQLException("Unit was not found: " + unitName);
    }

    public List<String> getUnits(String unitType) {
        List<String> units = new ArrayList<>();

        String sql = """
                SELECT unit_name FROM speed_distance_time_units
                WHERE unit_type = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, unitType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    units.add(resultSet.getString("unit_name"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return units;
    }
}
