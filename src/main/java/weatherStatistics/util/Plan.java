package weatherStatistics.util;


public class Plan {

    int startTime;
    int endTime;
    private String plan;

    public Plan(int startTime, int endTime, String plan) {
        this.startTime = startTime;
        this.plan = plan;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {

        return "(" + startTime + ";" + endTime + ";" + plan + ")";
    }
}
