import com.example.temperatureconverter.Model.TemperatureConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemperatureConverterTest {

    private static final double DELTA = 0.001;
    private final TemperatureConverter converter = new TemperatureConverter();

    @ParameterizedTest(name = "{0} °F = {1} °C")
    @CsvSource({
            "32, 0",
            "212, 100",
            "-40, -40",
            "50, 10",
            "68, 20",
            "14, -10",
            "100, 37.7777778",
            "451, 232.7777778"
    })
    void fahrenheitToCelsiusConvertsValues(double fahrenheit, double expectedCelsius) {
        assertEquals(expectedCelsius, converter.fahrenheitToCelsius(fahrenheit), DELTA);
    }

    @ParameterizedTest(name = "{0} °C = {1} °F")
    @CsvSource({
            "0, 32",
            "100, 212",
            "-40, -40",
            "10, 50",
            "20, 68",
            "-10, 14",
            "37, 98.6",
            "37.7777778, 100"
    })
    void celsiusToFahrenheitConvertsValues(double celsius, double expectedFahrenheit) {
        assertEquals(expectedFahrenheit, converter.celsiusToFahrenheit(celsius), DELTA);
    }

    @Test
    void celsiusToFahrenheitAndBackPreservesTemperature() {
        double celsius = 23.5;

        double fahrenheit = converter.celsiusToFahrenheit(celsius);

        assertEquals(celsius, converter.fahrenheitToCelsius(fahrenheit), DELTA);
    }

    @ParameterizedTest(name = "{0} K = {1} °C")
    @CsvSource({
            "273.15, 0",
            "300, 26.85",
            "233.15, -40",
            "323.15, 50",
            "0, -273.15"
    })
    void kelvinToCelsiusConvertsValues(double kelvin, double expectedCelsius) {
        assertEquals(expectedCelsius, converter.kelvinToCelsius(kelvin), DELTA);
    }

    @ParameterizedTest(name = "{0} °C is not extreme")
    @CsvSource({
            "-40",
            "0",
            "20",
            "50"
    })
    void isExtremeTemperatureReturnsFalseWithinInclusiveLimits(double celsius) {
        assertFalse(converter.isExtremeTemperature(celsius));
    }

    @ParameterizedTest(name = "{0} °C is extreme")
    @CsvSource({
            "-40.01",
            "-100",
            "50.01",
            "100"
    })
    void isExtremeTemperatureReturnsTrueOutsideLimits(double celsius) {
        assertTrue(converter.isExtremeTemperature(celsius));
    }
}