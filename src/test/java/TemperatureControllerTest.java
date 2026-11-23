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
import javafx.scene.layout.VBox;
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
                Platform.setImplicitExit(false);
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
            assertEquals(messages().getString("result.label"),
                    resultLabel.getText());            assertEquals("Last 5 conversions", historyLabel.getText());
            assertFalse(convertButton.getText().isBlank());
            assertEquals("", temperatureInput.getText());

            stage.close();
            return null;
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void startButtonOpensConverterWithSelectedLocale() throws Exception {
        TestController testController = new TestController();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            testController.start(stage);

            ComboBox<String> languageChoice =
                    field(testController, "languageChoice");
            languageChoice.setValue("Suomi");
            Button startButton = field(testController, "startButton");
            startButton.fire();

            assertEquals(new Locale("fi", "FI"),
                    getField(testController, "locale"));
            assertEquals(ResourceBundle.getBundle(
                            "MessagesBundle", new Locale("fi", "FI"))
                    .getString("app.title"), stage.getTitle());
            stage.close();
            return null;
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void initializeAddsLanguagesOnlyToLanguageView() throws Exception {
        ComboBox<String> languageChoice = new ComboBox<>();
        setField(controller, "languageChoice", languageChoice);
        setField(controller, "title", null);
        invokeNoArgs(controller, "initialize");

        assertEquals(List.of("English", "Suomi", "Svenska", "日本語",
                "العربية"), languageChoice.getItems());

        setField(controller, "title", new Label());
        invokeNoArgs(controller, "initialize");
        assertEquals(5, languageChoice.getItems().size());
    }

    @Test
    void defaultDaoFactoriesCreateProductionDaos() throws Exception {
        TemperatureController target = new TemperatureController();
        assertTrue(invokeNoArgs(target, "createTemperatureUnitDAO")
                instanceof TemperatureUnitDAO);
        assertTrue(invokeNoArgs(target, "createTempRecordDAO")
                instanceof TempRecordDAO);
        assertTrue(invokeNoArgs(target, "createSpeedDistanceTimeUnitDAO")
                instanceof SpeedDistanceTimeUnitDAO);
        assertTrue(invokeNoArgs(target, "createSpeedDistanceTimeDAO")
                instanceof SpeedDistanceTimeDAO);
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void converterButtonHandlesEveryTemperatureConversion() throws Exception {
        TestController testController = new TestController();
        setField(testController, "locale", Locale.US);
        setField(testController, "messages",
                ResourceBundle.getBundle("MessagesBundle", Locale.US));

        runOnFxThread(() -> {
            Stage stage = new Stage();
            invoke(testController, "showConverter", stage);
            ComboBox<Unit> from = field(testController, "fromUnit");
            ComboBox<Unit> to = field(testController, "toUnit");
            TextField input = field(testController, "temperatureInput");
            Button convert = field(testController, "convertButton");
            Label result = field(testController, "resultLabel");

            assertConversion(from, to, input, convert, result, 1, 2,
                    "212.0 °F");
            assertConversion(from, to, input, convert, result, 2, 1,
                    "0.0 °C");
            assertConversion(from, to, input, convert, result, 3, 1,
                    "0.0 °C");
            assertConversion(from, to, input, convert, result, 1, 3,
                    "273.15 K");
            assertConversion(from, to, input, convert, result, 2, 3,
                    "273.15 K");
            assertConversion(from, to, input, convert, result, 3, 2,
                    "32.0 °F");

            input.setText("not a number");
            convert.fire();
            assertEquals(messages().getString("error.invalid"), result.getText());
            stage.close();
            return null;
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runJavaFxTests", matches = "true")
    void speedDistanceTimeViewHandlesInputAndDatabaseErrors() throws Exception {
        TestController testController = new TestController();
        setField(testController, "locale", Locale.US);
        setField(testController, "messages",
                ResourceBundle.getBundle("MessagesBundle", Locale.US));

        runOnFxThread(() -> {
            Stage stage = new Stage();
            invoke(testController, "showSpeedDistanceTime", stage);
            VBox root = (VBox) stage.getScene().getRoot();
            TextField distance = (TextField) root.getChildren().get(1);
            TextField time = (TextField) root.getChildren().get(4);
            TextField speed = (TextField) root.getChildren().get(7);
            Button save = (Button) root.getChildren().get(9);
            Label result = (Label) root.getChildren().get(10);
            save.fire();
            assertEquals(messages().getString("sdt.error"), result.getText());

            distance.setText("10");
            time.setText("2");
            speed.setText("5");
            save.fire();
            assertEquals(messages().getString("sdt.success"), result.getText());
            stage.close();
            return null;
        });
    }

    private static void assertConversion(ComboBox<Unit> from,
                                         ComboBox<Unit> to,
                                         TextField input,
                                         Button convert,
                                         Label result,
                                         int fromId,
                                         int toId,
                                         String expected) {
        from.setValue(from.getItems().get(fromId - 1));
        to.setValue(to.getItems().get(toId - 1));
        input.setText(switch (fromId * 10 + toId) {
            case 12 -> "100";
            case 21 -> "32";
            case 31 -> "273.15";
            case 13 -> "0";
            case 23 -> "32";
            case 32 -> "273.15";
            default -> "0";
        });
        convert.fire();
        assertTrue(result.getText().endsWith(expected),
                result.getText());
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

    private static Object getField(Object target, String name)
            throws ReflectiveOperationException {
        Field field = TemperatureController.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object invokeNoArgs(TemperatureController target,
                                      String methodName)
            throws ReflectiveOperationException {
        Method method = TemperatureController.class
                .getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
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
            return new TestSpeedDistanceTimeUnitDAO();
        }

        @Override
        protected SpeedDistanceTimeDAO createSpeedDistanceTimeDAO() {
            return new TestSpeedDistanceTimeDAO();
        }
    }

    private static class TestSpeedDistanceTimeDAO extends SpeedDistanceTimeDAO {

        @Override
        public void saveRecord(double distanceValue, int distanceUnitId,
                               double timeValue, int timeUnitId,
                               double speedValue, int speedUnitId) {
            // Ei tallenneta oikeaan tietokantaan testissä.
        }
    }
    private static final class TestSpeedDistanceTimeUnitDAO
            extends SpeedDistanceTimeUnitDAO {

        @Override
        public List<Unit> getUnits(String unitType, String language) {
            if (unitType.equals("distance")) {
                return List.of(
                        new Unit(1, "Meter", "Meter"),
                        new Unit(2, "Kilometer", "Kilometer"),
                        new Unit(3, "Mile", "Mile")
                );
            } else if (unitType.equals("time")) {
                return List.of(
                        new Unit(4, "Second", "Second"),
                        new Unit(5, "Minute", "Minute"),
                        new Unit(6, "Hour", "Hour")
                );
            } else {
                return List.of(
                        new Unit(7, "Meters per second", "Meters per second"),
                        new Unit(8, "Kilometers per hour", "Kilometers per hour"),
                        new Unit(9, "Miles per hour", "Miles per hour")
                );
            }
        }
    }
}
