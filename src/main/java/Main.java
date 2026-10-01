import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        TemperatureConverter converter = new TemperatureConverter();
        TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
        TempRecordDAO recordDAO = new TempRecordDAO();

        Label title = new Label("Temperature Converter");

        Label temperatureLabel = new Label("Temperature:");
        TextField temperatureInput = new TextField();

        ComboBox<String> fromUnit = new ComboBox<>();
        fromUnit.getItems().addAll("Celsius", "Fahrenheit", "Kelvin");
        fromUnit.setValue("Celsius");

        ComboBox<String> toUnit = new ComboBox<>();
        toUnit.getItems().addAll("Celsius", "Fahrenheit", "Kelvin");
        toUnit.setValue("Fahrenheit");

        Button convertButton = new Button("Convert");

        Label resultLabel = new Label("Result:");

        convertButton.setOnAction(event -> {

            double temperature = Double.parseDouble(temperatureInput.getText());

            String from = fromUnit.getValue();
            String to = toUnit.getValue();

            double result = temperature;

            if (from.equals("Celsius") && to.equals("Fahrenheit")) {
                result = converter.celsiusToFahrenheit(temperature);
            } else if (from.equals("Fahrenheit") && to.equals("Celsius")) {
                result = converter.fahrenheitToCelsius(temperature);
            } else if (from.equals("Kelvin") && to.equals("Celsius")) {
                result = converter.kelvinToCelsius(temperature);
            } else if (from.equals("Celsius") && to.equals("Kelvin")) {
                result = temperature + 273.15;
            } else if (from.equals("Fahrenheit") && to.equals("Kelvin")) {
                double celsius = converter.fahrenheitToCelsius(temperature);
                result = celsius + 273.15;
            } else if (from.equals("Kelvin") && to.equals("Fahrenheit")) {
                double celsius = converter.kelvinToCelsius(temperature);
                result = converter.celsiusToFahrenheit(celsius);
            }

            try {
                int fromUnitId = unitDAO.getUnitId(from);
                int toUnitId = unitDAO.getUnitId(to);
                recordDAO.saveRecord(temperature, fromUnitId, result, toUnitId);
                resultLabel.setText("Result: " + result + " " + getSymbol(to));
            } catch (SQLException exception) {
                resultLabel.setText("Database error: " + exception.getMessage());
            }
        });

        VBox root = new VBox(10);

        root.setPadding(new Insets(20));

        root.getChildren().addAll(
                title,
                temperatureLabel,
                temperatureInput,
                fromUnit,
                toUnit,
                convertButton,
                resultLabel
        );

        Scene scene = new Scene(root, 400, 350);

        stage.setTitle("Temperature Converter");
        stage.setScene(scene);
        stage.show();
    }

    private String getSymbol(String unit) {
        if (unit.equals("Celsius")) {
            return "°C";
        } else if (unit.equals("Fahrenheit")) {
            return "°F";
        } else {
            return "K";
        }
    }

    public static void main(String[] args) {
        launch();
    }
}