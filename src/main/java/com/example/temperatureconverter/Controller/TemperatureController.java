
package com.example.temperatureconverter.Controller;

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
import javafx.scene.control.ListView;
import javafx.geometry.NodeOrientation;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.example.temperatureconverter.Dao.SpeedDistanceTimeDAO;
import com.example.temperatureconverter.Dao.SpeedDistanceTimeUnitDAO;
import com.example.temperatureconverter.Dao.TempRecordDAO;
import com.example.temperatureconverter.Dao.TemperatureUnitDAO;
import com.example.temperatureconverter.Model.TempRecord;
import com.example.temperatureconverter.Model.TemperatureConverter;
import com.example.temperatureconverter.Model.Unit;

public class TemperatureController {

    private ResourceBundle messages;
    private Locale locale;

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

        ComboBox<Unit> fromUnit = new ComboBox<>();
        fromUnit.getItems().addAll(unitDAO.getUnits(locale.getLanguage()));
        fromUnit.setValue(fromUnit.getItems().get(0));

        ComboBox<Unit> toUnit = new ComboBox<>();
        toUnit.getItems().addAll(unitDAO.getUnits(locale.getLanguage()));
        toUnit.setValue(toUnit.getItems().get(1));

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

        Label historyLabel = new Label(
                messages.getString("history.title")
        );

        ListView<String> historyList = new ListView<>();

        convertButton.setOnAction(event -> {
            try {
                double temperature = Double.parseDouble(
                        temperatureInput.getText()
                );

                String from = unitDAO.getUnitName(fromUnit.getValue().getId());
                String to = unitDAO.getUnitName(toUnit.getValue().getId());

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

                int fromUnitId = fromUnit.getValue().getId();
                int toUnitId = toUnit.getValue().getId();

                recordDAO.saveRecord(
                        temperature,
                        fromUnitId,
                        result,
                        toUnitId,
                        locale.getLanguage()
                );

                updateHistory(historyList, recordDAO);

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

        try {
            updateHistory(historyList, recordDAO);
        } catch (SQLException exception) {
            resultLabel.setText(
                    messages.getString("error.database") + " "
                            + exception.getMessage()
            );
        }

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-font-family: 'Noto Sans CJK JP';");

        root.setNodeOrientation(getNodeOrientation(locale));
        root.getChildren().addAll(
                title,
                temperatureLabel,
                temperatureInput,
                fromUnit,
                toUnit,
                convertButton,
                resultLabel,
                historyLabel,
                historyList,
                speedDistanceTimeButton
        );

        stage.setTitle(messages.getString("app.title"));
        stage.setScene(new Scene(root, 500, 500));
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

    public static NodeOrientation getNodeOrientation(Locale locale) {
        if ("ar".equals(locale.getLanguage())) {
            return NodeOrientation.RIGHT_TO_LEFT;
        }
        return NodeOrientation.LEFT_TO_RIGHT;
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

    private void updateHistory(ListView<String> historyList,
                               TempRecordDAO recordDAO) throws SQLException {

        historyList.getItems().clear();

        for (TempRecord record : recordDAO.getLastFiveRecords()) {
            historyList.getItems().add(record.toString());
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

        Label distanceLabel = new Label(messages.getString("sdt.distance"));
        TextField distanceInput = new TextField();

        ComboBox<Unit> distanceUnit = new ComboBox<>();
        distanceUnit.getItems().addAll(
                unitDAO.getUnits("distance", locale.getLanguage())
        );

        Label timeLabel = new Label(messages.getString("sdt.time"));
        TextField timeInput = new TextField();

        ComboBox<Unit> timeUnit = new ComboBox<>();
        timeUnit.getItems().addAll(
                unitDAO.getUnits("time", locale.getLanguage())
        );

        Label speedLabel = new Label(messages.getString("sdt.speed"));
        TextField speedInput = new TextField();

        ComboBox<Unit> speedUnit = new ComboBox<>();
        speedUnit.getItems().addAll(
                unitDAO.getUnits("speed", locale.getLanguage())
        );

        distanceUnit.getSelectionModel().selectFirst();
        timeUnit.getSelectionModel().selectFirst();
        speedUnit.getSelectionModel().selectFirst();

        Button saveButton = new Button(messages.getString("sdt.save"));
        Label resultLabel = new Label();

        saveButton.setOnAction(event -> {
            try {
                double distance = Double.parseDouble(distanceInput.getText());
                double time = Double.parseDouble(timeInput.getText());
                double speed = Double.parseDouble(speedInput.getText());

                int distanceId = distanceUnit.getValue().getId();
                int timeId = timeUnit.getValue().getId();
                int speedId = speedUnit.getValue().getId();

                recordDAO.saveRecord(
                        distance, distanceId,
                        time, timeId,
                        speed, speedId
                );

                resultLabel.setText(messages.getString("sdt.success"));

            } catch (NumberFormatException exception) {
                resultLabel.setText(messages.getString("sdt.error"));

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

        root.setNodeOrientation(getNodeOrientation(locale));

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


}
