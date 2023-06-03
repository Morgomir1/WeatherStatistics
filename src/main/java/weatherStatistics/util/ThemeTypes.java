package weatherStatistics.util;

public enum ThemeTypes {
    BLUE("blue", "Бирюзовая"),
    BLACK("black", "Тёмная");

    private String name;
    private String displayName;

    ThemeTypes(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }


    public String getThemeName() {
        return name;
    }
}
