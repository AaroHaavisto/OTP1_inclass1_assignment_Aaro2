import javafx.geometry.NodeOrientation;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void arabicLocaleUsesRightToLeftOrientation() {
        assertEquals(
                NodeOrientation.RIGHT_TO_LEFT,
                Main.getNodeOrientation(new Locale("ar", "SA"))
        );
    }

    @Test
    void nonArabicLocalesUseLeftToRightOrientation() {
        assertEquals(
                NodeOrientation.LEFT_TO_RIGHT,
                Main.getNodeOrientation(Locale.ENGLISH)
        );
        assertEquals(
                NodeOrientation.LEFT_TO_RIGHT,
                Main.getNodeOrientation(new Locale("fi", "FI"))
        );
    }
}
