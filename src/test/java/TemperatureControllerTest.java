import com.example.temperatureconverter.Controller.TemperatureController;
import com.example.temperatureconverter.Dao.SpeedDistanceTimeDAO;
import com.example.temperatureconverter.Dao.SpeedDistanceTimeUnitDAO;
import com.example.temperatureconverter.Dao.TempRecordDAO;
import com.example.temperatureconverter.Dao.TemperatureUnitDAO;
import com.example.temperatureconverter.Model.TempRecord;
import com.example.temperatureconverter.Model.Unit;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemperatureControllerTest {

    private TemperatureController controller;
    private static boolean toolkitStarted;

    @BeforeAll
    static void startJavaFxToolkit() {
        if (!Boolean.getBoolean("runJavaFxTests")) {
            return;
        }
        try {
            Platform.startup(() -> {
            });
            toolkitStarted = true;
        } catch (IllegalStateException alreadyStarted) {
            // The toolkit may already be running when the suite is executed in an IDE.
            toolkitStarted = true;
        }
    }

    @AfterAll
    static void stopJavaFxToolkit() {
        if (toolkitStarted) {
            Platform.exit();
        }
    }

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        controller = new TemperatureController();

        Field messages = TemperatureController.class
                .getDeclaredField("messages");
        messages.setAccessible(true);
        messages.set(controller,
                ResourceBundle.getBundle(
                        "MessagesBundle", Locale.US));
    }

    @Test
    void selectedLocaleMapsEverySupportedLanguage() throws Exception {
        assertEquals(new Locale("en", "US"),
                invoke("getSelectedLocale", "English"));
        assertEquals(new Locale("fi", "FI"),
                invoke("getSelectedLocale", "Suomi"));
        assertEquals(new Locale("sv", "SE"),
                invoke("getSelectedLocale", "Svenska"));
        assertEquals(new Locale("ja", "JP"),
                invoke("getSelectedLocale", "日本語"));
        assertEquals(new Locale("ar", "SA"),
                invoke("getSelectedLocale", "العربية"));
    }

    @Test
    void unknownLanguageFallsBackToEnglish() throws Exception {
        assertEquals(new Locale("en", "US"),
                invoke("getSelectedLocale", "Unknown"));
    }

    @Test
    void nodeOrientationIsRightToLeftOnlyForArabic() {
        assertEquals(NodeOrientation.RIGHT_TO_LEFT,
                TemperatureController.getNodeOrientation(
                        new Locale("ar", "SA")));
        assertEquals(NodeOrientation.LEFT_TO_RIGHT,
                TemperatureController.getNodeOrientation(Locale.US));
        assertEquals(NodeOrientation.LEFT_TO_RIGHT,
                TemperatureController.getNodeOrientation(new Locale("fi", "FI")));
    }

    @Test
    void englishUnitNamesAreResolvedFromLocalizedMessages() throws Exception {
        assertEquals("Celsius", invoke("getEnglishUnit",
                messages().getString("unit.celsius")));
        assertEquals("Fahrenheit", invoke("getEnglishUnit",
                messages().getString("unit.fahrenheit")));
        assertEquals("Kelvin", invoke("getEnglishUnit",
                messages().getString("unit.kelvin")));
        assertEquals("Kelvin", invoke("getEnglishUnit", "Unexpected unit"));
    }

    @Test
    void symbolsAreReturnedForAllTemperatureUnits() throws Exception {
        assertEquals("°C", invoke("getSymbol", "Celsius"));
        assertEquals("°F", invoke("getSymbol", "Fahrenheit"));
        assertEquals("K", invoke("getSymbol", "Kelvin"));
        assertEquals("K", invoke("getSymbol", "Unexpected unit"));
    }

    @Test
    void speedDistanceTimeUnitsAreTranslatedAndUnknownNamesArePreserved()
            throws Exception {
        assertEquals(messages().getString("unit.meter"),
                invoke("translateUnit", "Meter"));
        assertEquals(messages().getString("unit.kilometer"),
                invoke("translateUnit", "Kilometer"));
        assertEquals(messages().getString("unit.mile"),
                invoke("translateUnit", "Mile"));
        assertEquals(messages().getString("unit.second"),
                invoke("translateUnit", "Second"));
        assertEquals(messages().getString("unit.minute"),
                invoke("translateUnit", "Minute"));
        assertEquals(messages().getString("unit.hour"),
                invoke("translateUnit", "Hour"));
        assertEquals(messages().getString("unit.mps"),
                invoke("translateUnit", "Meters per second"));
        assertEquals(messages().getString("unit.kmh"),
                invoke("translateUnit", "Kilometers per hour"));
        assertEquals(messages().getString("unit.mph"),
                invoke("translateUnit", "Miles per hour"));
        assertEquals("Unknown unit", invoke("translateUnit", "Unknown unit"));
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void startLoadsLanguageViewAndConfiguresInitialLanguage() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            controller.start(stage);

            ComboBox<String> languageChoice = field(controller, "languageChoice");
            assertEquals("Select Language", stage.getTitle());
            assertEquals("English", languageChoice.getValue());
            assertTrue(stage.isShowing());

            stage.close();
            return null;
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void startLanguageSelectionUpdatesLocalizedTextsAndTitle() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            controller.start(stage);

            ComboBox<String> languageChoice = field(controller, "languageChoice");
            Label languagePrompt = field(controller, "languagePrompt");
            Button startButton = field(controller, "startButton");
            languageChoice.setValue("Suomi");
            languageChoice.getOnAction().handle(null);

            ResourceBundle finnish = ResourceBundle.getBundle(
                    "MessagesBundle", new Locale("fi", "FI"));
            assertEquals(finnish.getString("language.prompt"),
                    languagePrompt.getText());
            assertEquals(finnish.getString("button.continue"),
                    startButton.getText());
            assertEquals(finnish.getString("language.title"),
                    stage.getTitle());

            stage.close();
            return null;
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void showConverterLoadsUnitsHistoryAndLocalizedView() throws Exception {
        TestController testController = new TestController();
        setField(testController, "locale", new Locale("ar", "SA"));
        setField(testController, "messages",
                ResourceBundle.getBundle("MessagesBundle",
                        new Locale("ar", "SA")));

        runOnFxThread(() -> {
            Stage stage = new Stage();
            invoke(testController, "showConverter", stage);

            ComboBox<Unit> fromUnit = field(testController, "fromUnit");
            ComboBox<Unit> toUnit = field(testController, "toUnit");
            ListView<String> historyList = field(testController, "historyList");
            Label title = field(testController, "title");

            assertEquals(3, fromUnit.getItems().size());
            assertEquals(3, toUnit.getItems().size());
            assertEquals("Celsius", fromUnit.getValue().getName());
            assertEquals("Fahrenheit", toUnit.getValue().getName());
            assertEquals(1, historyList.getItems().size());
            assertEquals(ResourceBundle.getBundle(
                            "MessagesBundle", new Locale("ar", "SA"))
                    .getString("app.title"), title.getText());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT,
                    stage.getScene().getRoot().getNodeOrientation());
            assertTrue(stage.isShowing());

            stage.close();
            return null;
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void showConverterInitializesResultAndHistoryControls() throws Exception {
        TestController testController = new TestController();
        setField(testController, "locale", Locale.US);
        setField(testController, "messages",
                ResourceBundle.getBundle("MessagesBundle", Locale.US));

        runOnFxThread(() -> {
            Stage stage = new Stage();
            invoke(testController, "showConverter", stage);

            Label resultLabel = field(testController, "resultLabel");
            Label historyLabel = field(testController, "historyLabel");
            Button convertButton = field(testController, "convertButton");
            TextField temperatureInput = field(testController,
                    "temperatureInput");
            assertEquals("Result", resultLabel.getText());
            assertEquals("History", historyLabel.getText());
            assertFalse(convertButton.getText().isBlank());
            assertEquals("", temperatureInput.getText());

            stage.close();
            return null;
        });
    }

    private ResourceBundle messages() {
        return ResourceBundle.getBundle("MessagesBundle", Locale.US);
    }

    private Object invoke(String methodName, Object argument)
            throws ReflectiveOperationException {
        Method method = TemperatureController.class.getDeclaredMethod(
                methodName, String.class);
        method.setAccessible(true);
        return method.invoke(controller, argument);
    }

    private static void invoke(TemperatureController target, String methodName,
                               Stage stage) throws ReflectiveOperationException {
        Method method = TemperatureController.class.getDeclaredMethod(
                methodName, Stage.class);
        method.setAccessible(true);
        method.invoke(target, stage);
    }

    private static void setField(Object target, String name, Object value)
            throws ReflectiveOperationException {
        Field field = TemperatureController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(TemperatureController target, String name)
            throws ReflectiveOperationException {
        Field field = TemperatureController.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static <T> T runOnFxThread(ThrowingSupplier<T> action)
            throws Exception {
        FutureTask<T> task = new FutureTask<>(action::get);
        Platform.runLater(task);
        return task.get();
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static final class TestController extends TemperatureController {

        @Override
        protected TemperatureUnitDAO createTemperatureUnitDAO() {
            return new TemperatureUnitDAO() {
                @Override
                public List<Unit> getUnits(String language) {
                    return List.of(
                            new Unit(1, "Celsius", "Celsius"),
                            new Unit(2, "Fahrenheit", "Fahrenheit"),
                            new Unit(3, "Kelvin", "Kelvin"));
                }

                @Override
                public String getUnitName(int unitId) {
                    return switch (unitId) {
                        case 1 -> "Celsius";
                        case 2 -> "Fahrenheit";
                        case 3 -> "Kelvin";
                        default -> throw new IllegalArgumentException();
                    };
                }
            };
        }

        @Override
        protected TempRecordDAO createTempRecordDAO() {
            return new TempRecordDAO() {
                @Override
                public List<TempRecord> getLastFiveRecords() {
                    return List.of(new TempRecord(
                            20, "Celsius", 68, "Fahrenheit",
                            "en", "test"));
                }

                @Override
                public void saveRecord(double temperature, int unitId,
                                       double convertedTemperature,
                                       int convertedUnitId, String language) {
                }
            };
        }

        @Override
        protected SpeedDistanceTimeUnitDAO createSpeedDistanceTimeUnitDAO() {
            return new SpeedDistanceTimeUnitDAO();
        }

        @Override
        protected SpeedDistanceTimeDAO createSpeedDistanceTimeDAO() {
            return new SpeedDistanceTimeDAO();
        }
    }
}
