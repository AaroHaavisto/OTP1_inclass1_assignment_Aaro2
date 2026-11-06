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

    public String getUnitName(int unitId) throws SQLException {

        String sql = "SELECT name FROM temperature_units WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, unitId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        }

        throw new SQLException("Temperature unit was not found: " + unitId);
    }

    public int getUnitId(String unitName, String language) throws SQLException {

        String sql = """
                SELECT u.id
                FROM temperature_units u
                LEFT JOIN temperature_unit_translation t
                    ON u.id = t.unit_id AND t.lang = ?
                LEFT JOIN temperature_unit_translation en
                    ON u.id = en.unit_id AND en.lang = 'en'
                WHERE COALESCE(t.name, en.name) = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, language);
            statement.setString(2, unitName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        throw new SQLException("Temperature unit was not found: " + unitName);
    }

    public List<Unit> getUnits(String language) {

        List<Unit> units = new ArrayList<>();

        String sql = """
                SELECT
                    u.id,
                    COALESCE(t.name, en.name) AS name,
                    COALESCE(t.description, en.description) AS description
                FROM temperature_units u
                LEFT JOIN temperature_unit_translation t
                    ON u.id = t.unit_id AND t.lang = ?
                LEFT JOIN temperature_unit_translation en
                    ON u.id = en.unit_id AND en.lang = 'en'
                ORDER BY u.id
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, language);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    units.add(new Unit(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("description")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return units;
    }
}