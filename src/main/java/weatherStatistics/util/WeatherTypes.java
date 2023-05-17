package weatherStatistics.util;


/*
{{#weatherTypes}}
    <option selected value="{{weatherDisplayName}}">{{weatherDisplayName}}</option>
{{/weatherTypes}}
 */
public enum WeatherTypes {
    RAIN("дождь", "Дождь"),
    RAIN_STRONG("ливень", "Ливень"),
    SNOW("снег", "Снег"),
    STORM("гроза", "Гроза"),
    NO_PRECIPITATION("", "Без осадков");

    private String weatherName;

    private String weatherDisplayName;

    WeatherTypes(String weatherName, String weatherDisplayName) {
        this.weatherName = weatherName;
        this.weatherDisplayName = weatherDisplayName;
    }

    public String getWeatherName() {
        return weatherName;
    }

    public String getWeatherDisplayName() {
        return weatherDisplayName;
    }

    @Override
    public String toString() {
        return weatherDisplayName;
    }
}
