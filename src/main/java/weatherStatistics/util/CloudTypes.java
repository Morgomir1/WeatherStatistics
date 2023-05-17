package weatherStatistics.util;


/*
{{#weatherTypes}}
    <option selected value="{{weatherDisplayName}}">{{weatherDisplayName}}</option>
{{/weatherTypes}}
 */
public enum CloudTypes {
    SUNNY("солнечно", "Солнечно"),
    CLOUDY("облачно", "Облачно"),
    PARTY_CLOUDY("переменная облачность", "Переменная облачность");

    private String name;

    private String displayName;

    CloudTypes(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
