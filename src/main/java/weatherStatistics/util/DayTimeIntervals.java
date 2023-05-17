package weatherStatistics.util;

import weatherStatistics.entity.WeatherStat;

import java.util.*;

public enum DayTimeIntervals {

    NIGHT(0),
    MORNING(6),
    DAY(12),
    EVENING(18);

    private int time;
    private DayTimeIntervals next;

    DayTimeIntervals(int time) {
        this.time = time;
    }

    public DayTimeIntervals getNext() {
        return next;
    }

    public int getHour() {
        return time;
    }



    public static void init() {
        DAY.next = EVENING;
        EVENING.next = NIGHT;
        NIGHT.next = MORNING;
        MORNING.next = DAY;
    }

    @Override
    public String toString() {
        return String.valueOf(this.time);
    }
}
