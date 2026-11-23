import com.example.temperatureconverter.Controller.TemperatureController;
import javafx.geometry.NodeOrientation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemperatureControllerTest {

    private TemperatureController controller;

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
}
