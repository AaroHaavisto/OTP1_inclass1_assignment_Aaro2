package com.example.temperatureconverter.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.temperatureconverter.Database.DBConnection;
import com.example.temperatureconverter.Model.Unit;

public class SpeedDistanceTimeUnitDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public int getUnitId(String unitName, String unitType, String language)
            throws SQLException {

        String sql = """
                SELECT u.id
                FROM speed_distance_time_units u
                LEFT JOIN speed_distance_time_unit_translation t
                    ON u.id = t.unit_id AND t.lang = ?
                LEFT JOIN speed_distance_time_unit_translation en
                    ON u.id = en.unit_id AND en.lang = 'en'
                WHERE COALESCE(t.name, en.name) = ?
                AND u.unit_type = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, language);
            statement.setString(2, unitName);
            statement.setString(3, unitType);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        throw new SQLException("Unit was not found: " + unitName);
    }

    public List<Unit> getUnits(String unitType, String language) {

        List<Unit> units = new ArrayList<>();

        String sql = """
                SELECT
                    u.id,
                    COALESCE(t.name, en.name) AS name,
                    COALESCE(t.description, en.description) AS description
                FROM speed_distance_time_units u
                LEFT JOIN speed_distance_time_unit_translation t
                    ON u.id = t.unit_id AND t.lang = ?
                LEFT JOIN speed_distance_time_unit_translation en
                    ON u.id = en.unit_id AND en.lang = 'en'
                WHERE u.unit_type = ?
                ORDER BY u.id
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, language);
            statement.setString(2, unitType);

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
