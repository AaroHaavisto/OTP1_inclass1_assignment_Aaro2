import javafx.geometry.NodeOrientation;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.temperatureconverter.Controller.TemperatureController;

class MainTest {

    @Test
    void arabicLocaleUsesRightToLeftOrientation() {
        assertEquals(
                NodeOrientation.RIGHT_TO_LEFT,
                TemperatureController.getNodeOrientation(new Locale("ar", "SA"))
        );
    }

    @Test
    void nonArabicLocalesUseLeftToRightOrientation() {
        assertEquals(
                NodeOrientation.LEFT_TO_RIGHT,
                TemperatureController.getNodeOrientation(Locale.ENGLISH)
        );
        assertEquals(
                NodeOrientation.LEFT_TO_RIGHT,
                TemperatureController.getNodeOrientation(new Locale("fi", "FI"))
        );
    }
}
