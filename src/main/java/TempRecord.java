public class TempRecord {

    private double temperature;
    private String fromUnit;
    private double convertedTemperature;
    private String toUnit;
    private String language;
    private String createdAt;

    public TempRecord(double temperature, String fromUnit,
                      double convertedTemperature, String toUnit,
                      String language, String createdAt) {

        this.temperature = temperature;
        this.fromUnit = fromUnit;
        this.convertedTemperature = convertedTemperature;
        this.toUnit = toUnit;
        this.language = language;
        this.createdAt = createdAt;
    }

    public String toString() {
        return temperature + " " + fromUnit
                + " → " + convertedTemperature + " " + toUnit
                + " (" + language + ") - " + createdAt;
    }
}