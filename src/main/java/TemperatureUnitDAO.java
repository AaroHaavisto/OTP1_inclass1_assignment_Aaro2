import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TemperatureUnitDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public int getUnitId(String unitName) throws SQLException {
        String sql = "SELECT id FROM temperature_units WHERE name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, unitName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        throw new SQLException("Temperature unit was not found: " + unitName);
    }

    public List<String> getUnits() {

        List<String> units = new ArrayList<>();

        String sql = "SELECT name FROM temperature_units";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                units.add(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return units;
    }
}