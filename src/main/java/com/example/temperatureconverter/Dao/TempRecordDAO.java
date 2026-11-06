package com.example.temperatureconverter.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.example.temperatureconverter.Database.DBConnection;
import com.example.temperatureconverter.Model.TempRecord;

public class TempRecordDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public void saveRecord(double temperature, int unitId,
                           double convertedTemperature, int convertedUnitId,
                           String language) throws SQLException {

        String sql = """
                INSERT INTO temperature_records
                (temperature, unit_id, converted_temperature,
                 converted_unit_id, language)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, temperature);
            statement.setInt(2, unitId);
            statement.setDouble(3, convertedTemperature);
            statement.setInt(4, convertedUnitId);
            statement.setString(5, language);

            statement.executeUpdate();
        }
    }


    public void saveRecord(double temperature, int unitId,
                           double convertedTemperature, int convertedUnitId)
            throws SQLException {

        saveRecord(temperature, unitId, convertedTemperature,
                convertedUnitId, "en");
    }

    public List<TempRecord> getLastFiveRecords() throws SQLException {

        List<TempRecord> records = new ArrayList<>();

        String sql = """
            SELECT r.temperature,
                   COALESCE(ft.name, fen.name, fu.name) AS from_unit,
                   r.converted_temperature,
                   COALESCE(tt.name, ten.name, tu.name) AS to_unit,
                   r.language,
                   r.created_at
            FROM temperature_records r
            JOIN temperature_units fu ON r.unit_id = fu.id
            JOIN temperature_units tu ON r.converted_unit_id = tu.id
            LEFT JOIN temperature_unit_translation ft
                ON fu.id = ft.unit_id AND ft.lang = r.language
            LEFT JOIN temperature_unit_translation fen
                ON fu.id = fen.unit_id AND fen.lang = 'en'
            LEFT JOIN temperature_unit_translation tt
                ON tu.id = tt.unit_id AND tt.lang = r.language
            LEFT JOIN temperature_unit_translation ten
                ON tu.id = ten.unit_id AND ten.lang = 'en'
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT 5
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                TempRecord record = new TempRecord(
                        resultSet.getDouble("temperature"),
                        resultSet.getString("from_unit"),
                        resultSet.getDouble("converted_temperature"),
                        resultSet.getString("to_unit"),
                        resultSet.getString("language"),
                        resultSet.getTimestamp("created_at").toString()
                );

                records.add(record);
            }
        }

        return records;
    }
}
