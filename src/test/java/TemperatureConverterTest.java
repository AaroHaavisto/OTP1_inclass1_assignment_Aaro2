import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemperatureConverterTest {

    TemperatureConverter converter = new TemperatureConverter();

    @Test
    void testFahrenheitToCelsius() {
        assertEquals(0, converter.fahrenheitToCelsius(32), 0.01);
        assertEquals(100, converter.fahrenheitToCelsius(212), 0.01);
        assertEquals(-40, converter.fahrenheitToCelsius(-40), 0.01);

        assertEquals(10, converter.fahrenheitToCelsius(50), 0.01);
        assertEquals(20, converter.fahrenheitToCelsius(68), 0.01);
        assertEquals(-10, converter.fahrenheitToCelsius(14), 0.01);
        assertEquals(37.78, converter.fahrenheitToCelsius(100), 0.01);
    }

    @Test
    void testCelsiusToFahrenheit() {
        assertEquals(32, converter.celsiusToFahrenheit(0), 0.01);
        assertEquals(212, converter.celsiusToFahrenheit(100), 0.01);
        assertEquals(-40, converter.celsiusToFahrenheit(-40), 0.01);

        assertEquals(50, converter.celsiusToFahrenheit(10), 0.01);
        assertEquals(68, converter.celsiusToFahrenheit(20), 0.01);
        assertEquals(14, converter.celsiusToFahrenheit(-10), 0.01);
        assertEquals(98.6, converter.celsiusToFahrenheit(37), 0.01);
    }

    @Test
    void testIsExtremeTemperature() {
        Assertions.assertFalse(converter.isExtremeTemperature(-40));
        Assertions.assertFalse(converter.isExtremeTemperature(50));

        Assertions.assertTrue(converter.isExtremeTemperature(-41));
        Assertions.assertTrue(converter.isExtremeTemperature(51));

        Assertions.assertFalse(converter.isExtremeTemperature(20));
        Assertions.assertFalse(converter.isExtremeTemperature(0));
        Assertions.assertFalse(converter.isExtremeTemperature(30));

        Assertions.assertTrue(converter.isExtremeTemperature(-100));
        Assertions.assertTrue(converter.isExtremeTemperature(100));
    }

    @Test
    void testKelvinToCelsius() {
        TemperatureConverter converter = new TemperatureConverter();

        assertEquals(26.85, converter.kelvinToCelsius(300), 0.001);
    }
}