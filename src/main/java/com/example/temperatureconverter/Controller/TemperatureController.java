
package com.example.temperatureconverter.Controller;

import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import java.io.IOException;
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

    @FXML private ComboBox<String> languageChoice;
    @FXML private Label languagePrompt;
    @FXML private Button startButton;
    @FXML private Label title;
    @FXML private Label temperatureLabel;
    @FXML private TextField temperatureInput;
    @FXML private ComboBox<Unit> fromUnit;
    @FXML private ComboBox<Unit> toUnit;
    @FXML private Button convertButton;
    @FXML private Label resultLabel;
    @FXML private Label historyLabel;
    @FXML private ListView<String> historyList;
    @FXML private Button speedDistanceTimeButton;

    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/language-view.fxml")
            );
            loader.setController(this);
            Parent languageRoot = loader.load();
            stage.setScene(new Scene(languageRoot, 300, 150));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load language view", exception
            );
        }

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


        stage.setTitle("Select Language");
        stage.show();

        startButton.setOnAction(event -> {
            String selectedLanguage = languageChoice.getValue();

            locale = getSelectedLocale(languageChoice.getValue());

            messages = ResourceBundle.getBundle("MessagesBundle", locale);
            showConverter(stage);
        });
    }

    @FXML
    private void initialize() {
        if (languageChoice != null && title == null) {
            languageChoice.getItems().addAll(
                    "English", "Suomi", "Svenska", "日本語", "العربية"
            );
            languageChoice.setValue("English");
        }
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
        
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main-view.fxml")
            );
            loader.setController(this);
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load main view", exception
            );
        }

        title.setText(messages.getString("app.title"));
        temperatureLabel.setText(messages.getString("temperature.label"));
        fromUnit.getItems().addAll(unitDAO.getUnits(locale.getLanguage()));
        fromUnit.setValue(fromUnit.getItems().get(0));
        toUnit.getItems().addAll(unitDAO.getUnits(locale.getLanguage()));
        toUnit.setValue(toUnit.getItems().get(1));
        convertButton.setText(messages.getString("button.convert"));

        speedDistanceTimeButton.setOnAction(event -> {
            showSpeedDistanceTime(stage);
        });
        resultLabel.setText(messages.getString("result.label"));
        historyLabel.setText(messages.getString("history.title"));

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

        root.setNodeOrientation(getNodeOrientation(locale));

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
