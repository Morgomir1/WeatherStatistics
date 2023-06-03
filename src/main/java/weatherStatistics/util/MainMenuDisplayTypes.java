package weatherStatistics.util;

public enum MainMenuDisplayTypes {
    DAYS("Дни"),
    WEEKS("Недели"),
    MONTH("Месяцы");

    private String name;

    MainMenuDisplayTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}