package weatherStatistics.util;

public class Plan {

    int interval;
    private String plan;

    public Plan(int time, String plan) {
        this.interval = time;
        this.plan = plan;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    @Override
    public String toString() {
        return this.getInterval() + ":00 - " + (interval + 6 == 24 ? 0 : interval + 6) + ":00";
    }
}
