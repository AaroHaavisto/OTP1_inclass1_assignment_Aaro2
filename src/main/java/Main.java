
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
        languageChoice.getItems().addAll("English", "Suomi", "Svenska", "日本語", "العربية");
        languageChoice.setValue("English");

        Label languagePrompt = new Label();
        Button startButton = new Button();

        languageChoice.setStyle("-fx-font-family: 'Noto Sans CJK JP';");
        languagePrompt.setStyle("-fx-font-family: 'Noto Sans CJK JP';");
        startButton.setStyle("-fx-font-family: 'Noto Sans CJK JP';");

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
        } else if (language.equals("العربية")) {
            return new Locale("ar", "SA");
        } else {
            return new Locale("en", "US");
        }
    }


    private void showConverter(Stage stage) {

        TemperatureConverter converter = new TemperatureConverter();
        TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
        TempRecordDAO recordDAO = new TempRecordDAO();

        SpeedDistanceTimeUnitDAO speedUnitDAO =
                new SpeedDistanceTimeUnitDAO();

        SpeedDistanceTimeDAO speedRecordDAO =
                new SpeedDistanceTimeDAO();

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

        Button speedDistanceTimeButton =
                new Button(messages.getString("sdt.title"));

        speedDistanceTimeButton.setOnAction(event -> {
            showSpeedDistanceTime(stage);
        });

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
        root.setStyle("-fx-font-family: 'Noto Sans CJK JP';");
        root.getChildren().addAll(
                title,
                temperatureLabel,
                temperatureInput,
                fromUnit,
                toUnit,
                convertButton,
                resultLabel,
                speedDistanceTimeButton
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

    private String translateUnit(String unitName) {
        switch (unitName) {
            case "Meter":
                return messages.getString("unit.meter");
            case "Kilometer":
                return messages.getString("unit.kilometer");
            case "Mile":
                return messages.getString("unit.mile");
            case "Second":
                return messages.getString("unit.second");
            case "Minute":
                return messages.getString("unit.minute");
            case "Hour":
                return messages.getString("unit.hour");
            case "Meters per second":
                return messages.getString("unit.mps");
            case "Kilometers per hour":
                return messages.getString("unit.kmh");
            case "Miles per hour":
                return messages.getString("unit.mph");
            default:
                return unitName;
        }
    }

    private void showSpeedDistanceTime(Stage stage) {

        SpeedDistanceTimeUnitDAO unitDAO =
                new SpeedDistanceTimeUnitDAO();

        SpeedDistanceTimeDAO recordDAO =
                new SpeedDistanceTimeDAO();

        Label distanceLabel = new Label(messages.getString("sdt.distance"));        TextField distanceInput = new TextField();

        ComboBox<String> distanceUnit = new ComboBox<>();
        for (String unit : unitDAO.getUnits("distance")) {
            distanceUnit.getItems().add(translateUnit(unit));
        }

        Label timeLabel = new Label(messages.getString("sdt.time"));        TextField timeInput = new TextField();

        ComboBox<String> timeUnit = new ComboBox<>();
        for (String unit : unitDAO.getUnits("time")) {
            timeUnit.getItems().add(translateUnit(unit));
        }

        Label speedLabel = new Label(messages.getString("sdt.speed"));        TextField speedInput = new TextField();

        ComboBox<String> speedUnit = new ComboBox<>();
        for (String unit : unitDAO.getUnits("speed")) {
            speedUnit.getItems().add(translateUnit(unit));
        }
        distanceUnit.getSelectionModel().selectFirst();
        timeUnit.getSelectionModel().selectFirst();
        speedUnit.getSelectionModel().selectFirst();

        Button saveButton = new Button(messages.getString("sdt.save"));        Label resultLabel = new Label();
        saveButton.setOnAction(event -> {
            try {
                double distance = Double.parseDouble(distanceInput.getText());
                double time = Double.parseDouble(timeInput.getText());
                double speed = Double.parseDouble(speedInput.getText());

                int distanceId = unitDAO.getUnitId(
                        distanceUnit.getValue(), "distance");

                int timeId = unitDAO.getUnitId(
                        timeUnit.getValue(), "time");

                int speedId = unitDAO.getUnitId(
                        speedUnit.getValue(), "speed");

                recordDAO.saveRecord(
                        distance, distanceId,
                        time, timeId,
                        speed, speedId
                );

                resultLabel.setText("Record saved successfully.");

            } catch (NumberFormatException exception) {
                resultLabel.setText("Enter valid numbers in all fields.");
            } catch (SQLException exception) {
                resultLabel.setText("Database error: " + exception.getMessage());
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        root.getChildren().addAll(
                distanceLabel, distanceInput, distanceUnit,
                timeLabel, timeInput, timeUnit,
                speedLabel, speedInput, speedUnit,
                saveButton, resultLabel
        );

        stage.setTitle(messages.getString("sdt.title"));
        stage.setScene(new Scene(root, 400, 500));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
