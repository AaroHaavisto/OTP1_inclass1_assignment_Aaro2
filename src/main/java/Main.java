
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    private ResourceBundle messages;
    private Locale locale;

    @Override
    public void start(Stage stage) {


        ComboBox<String> languageChoice = new ComboBox<>();
        languageChoice.getItems().addAll(
                "English", "Suomi", "Svenska", "日本語"
        );
        languageChoice.setValue("English");

        Label languagePrompt = new Label();
        Button startButton = new Button();

        languageChoice.setOnAction(event -> {
            Locale selectedLocale = getSelectedLocale(
                    languageChoice.getValue()
            );

            ResourceBundle selectedMessages =
                    ResourceBundle.getBundle("MessagesBundle", selectedLocale);

            languagePrompt.setText(
                    selectedMessages.getString("language.prompt")
            );

            startButton.setText(
                    selectedMessages.getString("button.continue")
            );

            stage.setTitle(
                    selectedMessages.getString("language.title")
            );
        });

        languageChoice.getSelectionModel().select("English");


        VBox languageRoot = new VBox(10);
        languageRoot.setPadding(new Insets(20));
        languageRoot.getChildren().addAll(
                languagePrompt, languageChoice, startButton
        );

        stage.setTitle("Select Language");
        stage.setScene(new Scene(languageRoot, 300, 150));
        stage.show();

        startButton.setOnAction(event -> {
            String selectedLanguage = languageChoice.getValue();

            locale = getSelectedLocale(languageChoice.getValue());

            messages = ResourceBundle.getBundle("MessagesBundle", locale);
            showConverter(stage);
        });
    }


    private Locale getSelectedLocale(String language) {
        if (language.equals("Suomi")) {
            return new Locale("fi", "FI");
        } else if (language.equals("Svenska")) {
            return new Locale("sv", "SE");
        } else if (language.equals("日本語")) {
            return new Locale("ja", "JP");
        } else {
            return new Locale("en", "US");
        }
    }


    private void showConverter(Stage stage) {

        TemperatureConverter converter = new TemperatureConverter();
        TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
        TempRecordDAO recordDAO = new TempRecordDAO();

        Label title = new Label(messages.getString("app.title"));
        Label temperatureLabel = new Label(
                messages.getString("temperature.label")
        );

        TextField temperatureInput = new TextField();

        ComboBox<String> fromUnit = new ComboBox<>();
        fromUnit.getItems().addAll(
                messages.getString("unit.celsius"),
                messages.getString("unit.fahrenheit"),
                messages.getString("unit.kelvin")
        );
        fromUnit.setValue(messages.getString("unit.celsius"));

        ComboBox<String> toUnit = new ComboBox<>();
        toUnit.getItems().addAll(
                messages.getString("unit.celsius"),
                messages.getString("unit.fahrenheit"),
                messages.getString("unit.kelvin")
        );
        toUnit.setValue(messages.getString("unit.fahrenheit"));

        Button convertButton = new Button(
                messages.getString("button.convert")
        );

        Label resultLabel = new Label(
                messages.getString("result.label")
        );

        convertButton.setOnAction(event -> {
            try {
                double temperature = Double.parseDouble(
                        temperatureInput.getText()
                );

                String from = getEnglishUnit(fromUnit.getValue());
                String to = getEnglishUnit(toUnit.getValue());

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

                int fromUnitId = unitDAO.getUnitId(from);
                int toUnitId = unitDAO.getUnitId(to);

                recordDAO.saveRecord(
                        temperature, fromUnitId, result, toUnitId
                );

                resultLabel.setText(
                        messages.getString("result.label") + " "
                                + result + " " + getSymbol(to)
                );

            } catch (NumberFormatException exception) {
                resultLabel.setText(messages.getString("error.invalid"));
            } catch (SQLException exception) {
                resultLabel.setText(
                        messages.getString("error.database") + " "
                                + exception.getMessage()
                );
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

        stage.setTitle(messages.getString("app.title"));
        stage.setScene(new Scene(root, 400, 350));
        stage.show();
    }

    private String getEnglishUnit(String unit) {
        if (unit.equals(messages.getString("unit.celsius"))) {
            return "Celsius";
        } else if (unit.equals(messages.getString("unit.fahrenheit"))) {
            return "Fahrenheit";
        } else {
            return "Kelvin";
        }
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
        launch(args);
    }
}
