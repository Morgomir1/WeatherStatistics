package weatherStatistics.util;

import weatherStatistics.entity.WeatherStat;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Algoritms {

    public static <T> ArrayList<WeatherStat> getListWithWeatherChances(HashMap<T, WeatherStat> stats) {
        ArrayList<WeatherStat> list = new ArrayList<>();
        for (WeatherStat stat : stats.values()) {
            for (String weather : stat.getW1().split(";")) {
                for (WeatherTypes weather1 : WeatherTypes.values()) {
                    if (weather.toLowerCase(Locale.ROOT).contains(weather1.getWeatherName())) {
                        stat.getWeatherTypes().merge(weather1, 1.0, Double::sum);
                    }
                }
            }
            int sum = 0;
            for (Map.Entry<WeatherTypes, Double> weather : stat.getWeatherTypes().entrySet()) {
                stat.setW1(weather.getKey().getWeatherDisplayName());
                sum += weather.getValue();
            }
            for (Map.Entry<WeatherTypes, Double> weather : stat.getWeatherTypes().entrySet()) {
                stat.getWeatherTypes().put(weather.getKey(), (double) Math.round(weather.getValue() / sum * 100));
            }
            stat.setDD("");
            stat.setWW("");
            stat.setW1("");
            stat.setW2("");
            list.add(stat);
        }
        return list;
    }

    public static HashMap<String, WeatherStat> connectStats(List<WeatherStat> weatherStats, Integer hour) {
        Queue<WeatherStat> statCopy = new LinkedList<>(weatherStats);
        HashMap<String, WeatherStat> connectedStats = new HashMap<>();
        long time = System.currentTimeMillis();
        while (!statCopy.isEmpty()) {
            WeatherStat stat = statCopy.poll();
            Queue<WeatherStat> statCopy2 = new LinkedList<>(statCopy);
            if (hour != null && stat.getHour() != hour) {
                continue;
            }
            while (!statCopy2.isEmpty()) {
                WeatherStat stat1 = statCopy2.poll();
                if (hour != null && stat1.getHour() != hour) {
                    statCopy.remove(stat1);
                    continue;
                }
                if (stat.isDateEqualTo(stat1)) {
                    if (connectedStats.get(stat.getDate()) != null) {
                        connectedStats.put(stat.getDate(), WeatherStat.connectTwoStats(connectedStats.get(stat.getDate()), WeatherStat.connectTwoStats(stat, stat1)));
                    } else {
                        connectedStats.put(stat.getDate(), WeatherStat.connectTwoStats(stat, stat1));
                    }
                    statCopy.remove(stat1);
                }
            }

        }
        long executionTime = System.currentTimeMillis() - time;
        System.out.println("Execution time: " + TimeUnit.MILLISECONDS.toSeconds(executionTime));
        return connectedStats;
    }
    public static HashMap<String, WeatherStat> connectStats(List<WeatherStat> weatherStats, Integer start, Integer end) {
        Queue<WeatherStat> statCopy = new LinkedList<>(weatherStats);
        HashMap<String, WeatherStat> connectedStats = new HashMap<>();
        long time = System.currentTimeMillis();
        while (!statCopy.isEmpty()) {
            WeatherStat stat = statCopy.poll();
            Queue<WeatherStat> statCopy2 = new LinkedList<>(statCopy);
            if (!(start <= stat.getHour() && end >= stat.getHour()
                    || ((start - stat.getHour()) * (stat.getHour() + 3 - start) >= 0)
                    || ((end - stat.getHour()) * (stat.getHour() + 3 - end) >= 0))) {
                continue;
            }
            while (!statCopy2.isEmpty()) {
                WeatherStat stat1 = statCopy2.poll();
                if (!(start <= stat1.getHour() && end >= stat1.getHour()
                        || ((start - stat1.getHour()) * (stat1.getHour() + 3 - start) >= 0)
                        || ((end - stat1.getHour()) * (stat1.getHour() + 3 - end) >= 0))) {
                    statCopy.remove(stat1);
                    continue;
                }
                if (stat.isDateEqualTo(stat1)) {
                    if (connectedStats.get(stat.getDate()) != null) {
                        connectedStats.put(stat.getDate(), WeatherStat.connectTwoStats(connectedStats.get(stat.getDate()), WeatherStat.connectTwoStats(stat, stat1)));
                    } else {
                        connectedStats.put(stat.getDate(), WeatherStat.connectTwoStats(stat, stat1));
                    }
                    statCopy.remove(stat1);
                }
            }

        }
        long executionTime = System.currentTimeMillis() - time;
        System.out.println("Execution time: " + TimeUnit.MILLISECONDS.toSeconds(executionTime));
        return connectedStats;
    }

    public static ArrayList<WeatherStat> sortByDay(List<WeatherStat> list, LocalDate date) {
        ArrayList<WeatherStat> sorted = new ArrayList<>();
        LocalDate currentDateCopy = date;
        while (sorted.size() < list.size()) {
            for (WeatherStat stat : list) {
                if (stat.isDateEqualTo(currentDateCopy)) {
                    sorted.add(stat);
                    currentDateCopy = currentDateCopy.plusDays(1);
                }
            }
            currentDateCopy = currentDateCopy.plusDays(1);
        }
        return sorted;
    }

    public static ArrayList<WeatherStat> sortByMonth(List<WeatherStat> list) {
        ArrayList<WeatherStat> sorted = new ArrayList<>();
        while (!list.isEmpty()) {
            int minDay = Integer.MAX_VALUE;
            int minMonth = Integer.MAX_VALUE;
            WeatherStat statWithMinDay = null;
            for (WeatherStat stat : list) {
                if (minMonth > stat.month) {
                    minMonth = stat.month;
                    minDay = stat.day;
                    statWithMinDay = stat;
                    continue;
                }
                if (minDay > stat.day) {
                    minDay = stat.day;
                    statWithMinDay = stat;
                }
            }
            if (statWithMinDay != null) {
                list.remove(statWithMinDay);
                sorted.add(statWithMinDay);
            }
        }
        return sorted;
    }
}
