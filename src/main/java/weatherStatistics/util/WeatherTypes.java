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

    private String name;

    private String weatherDisplayName;

    WeatherTypes(String weatherName, String weatherDisplayName) {
        this.weatherName = weatherName;
        this.weatherDisplayName = weatherDisplayName;
        this.name = name();
    }

    public String getWeatherName() {
        return weatherName;
    }

    public String getName() {
        return name;
    }

    public String getWeatherDisplayName() {
        return weatherDisplayName;
    }

    public static WeatherTypes getForDisplayName(String name) {
        for (WeatherTypes type : WeatherTypes.values()) {
            if (type.getWeatherDisplayName().equals(name)) {
                return type;
            }
        }
        return NO_PRECIPITATION;
    }


    @Override
    public String toString() {
        return weatherDisplayName;
    }
}
