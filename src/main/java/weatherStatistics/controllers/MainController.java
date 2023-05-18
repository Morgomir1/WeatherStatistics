package weatherStatistics.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.CloudTypes;
import weatherStatistics.util.DayTimeIntervals;
import weatherStatistics.util.Plan;
import weatherStatistics.util.WeatherTypes;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
public class MainController {
    List<WeatherStat> weatherStats = new ArrayList<>();
    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping("/information")
    public String information() {
        return "information";
    }

    private Date currentDate = new Date();

    @GetMapping
    public String main(Map<String, Object> model) {
        this.weatherStats = weatherStatistics.findAll();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("ddMM");
        String currentDate = formatForDateNow.format(this.currentDate);
        HashMap<Integer, WeatherStat> weatherStatHashMap = new HashMap<>();
        for (DayTimeIntervals interval : DayTimeIntervals.values()) {
            weatherStatHashMap.put(interval.getHour(), null);
        }
        for (WeatherStat stat : weatherStats) {
            if (stat.isDateEqualTo(this.currentDate)) {
                for (DayTimeIntervals dayTimeIntervals : DayTimeIntervals.values()) {
                    if (dayTimeIntervals.getHour() == stat.getHour()) {
                        if (weatherStatHashMap.get(dayTimeIntervals.getHour()) != null) {
                            weatherStatHashMap.put(dayTimeIntervals.getHour(),
                                    WeatherStat.connectTwoStats(weatherStatHashMap.get(dayTimeIntervals.getHour()), stat));
                        } else {
                            weatherStatHashMap.put(dayTimeIntervals.getHour(), stat);
                        }
                        break;
                    }
                }
            }
        }
        ArrayList<WeatherStat> list = getListWithWeatherChances(weatherStatHashMap);
        model.put("weatherStats", sortByHour(list));

        SimpleDateFormat formatForClient = new SimpleDateFormat("d LLLL");
        currentDate = formatForClient.format(this.currentDate);
        model.put("currentDate", currentDate);
        return "main";
    }


    public static ArrayList<WeatherStat> sortByHour(List<WeatherStat> list) {
        ArrayList<WeatherStat> newList = new ArrayList<>();
        for (DayTimeIntervals interval : DayTimeIntervals.values()) {
            for (WeatherStat stat : list) {
                if (stat.getHour() == interval.getHour()) {
                    stat.setNextHour(interval.getNext().getHour());
                    newList.add(stat);
                }
            }
        }
        return newList;
    }

    @GetMapping(value = "/mainRedirect")
    public String mainRedirect() {
        return "redirect:/main";
    }

    @GetMapping(value = "/planningRedirect")
    public String planningRedirect() {
        return "redirect:/planning";
    }

    @GetMapping(value = "/settingsRedirect")
    public String settingsRedirect() {
        return "redirect:/settings";
    }

    @GetMapping(value = "/adminRedirect")
    public String adminRedirect() {
        return "redirect:/admin";
    }

    @GetMapping(value = "/informationRedirect")
    public String informationRedirect() {
        return "redirect:/information";
    }

    @GetMapping(value = "/dayFindRedirect")
    public String dayFindRedirect() {
        return "redirect:/dayFind";
    }

    @GetMapping(value = "/nextDay")
    public String nextDay() {
        this.currentDate = new Date(this.currentDate.getTime() + MILLIS_IN_A_DAY);
        return "redirect:/main";
    }

    @GetMapping(value = "/prevDay")
    public String prevDay() {
        this.currentDate = new Date(this.currentDate.getTime() - MILLIS_IN_A_DAY);
        return "redirect:/main";
    }

    String cloudiness = "";
    String precipitation = "";
    int temperature = 0;
    int startFindId = 0;
    int endFindID = 3;

    @GetMapping("/dayFind")
    public String dayFind(Map<String, Object> model) {
        if (this.foundedDays.size() != 0) {
            ArrayList<WeatherStat> finalStats = new ArrayList<>();
            for (int i = startFindId; i < endFindID; i++) {
                finalStats.add(this.foundedDays.get(i));
            }
            model.put("results", finalStats);
        }
        model.put("weatherTypes", WeatherTypes.values());
        model.put("cloudTypes", CloudTypes.values());
        model.put("timeIntervals", DayTimeIntervals.values());
        return "dayFind";
    }

    @GetMapping(value = "/nextFindDays")
    public String nextFindDays() {
        if (this.startFindId + 1 < this.foundedDays.size() - 3 && this.endFindID + 1 < this.foundedDays.size()) {
            this.startFindId++;
            this.endFindID++;
        }
        return "redirect:/dayFind";
    }

    @GetMapping(value = "/prevFindDays")
    public String prevFindDays() {
        if (this.endFindID - 1 >= 3 && this.startFindId - 1 >= 0) {
            this.endFindID--;
            this.startFindId--;
        }
        return "redirect:/dayFind";
    }

    ArrayList<WeatherStat> foundedDays = new ArrayList<>();

    @PostMapping("/findDate")
    public String findDate(
            @RequestParam(name = "cloudiness", required = false) String cloudiness,
            @RequestParam(name = "precipitation", required = false) String precipitation,
            @RequestParam(name = "timeInterval", required = false) Integer timeInterval,
            @RequestParam(name = "temperature", required = false) Integer temperature,
            Map<String, Object> model) {
        if (this.weatherStats.size() == 0) {
            this.weatherStats = weatherStatistics.findAll();
        }
        if (temperature != null && precipitation != null && timeInterval != null) {

            HashMap<String, WeatherStat> connectedStats = connectStats(this.weatherStats, timeInterval);
            ArrayList<WeatherStat> sortedStatsWithWeather = getListWithWeatherChances(connectedStats);
            this.foundedDays = sortByDay(sortedStatsWithWeather, this.currentDate);
            Queue<WeatherStat> queue = new LinkedList<>(this.foundedDays);

            System.out.println("Size before find:" + this.foundedDays.size());
            HashMap<Double, WeatherStat> sortedHashMap = new HashMap<>();
            while (!queue.isEmpty()) {
                WeatherStat stat = queue.poll();
                double weatherChance = 0.0;
                boolean remove = true;
                for (WeatherTypes entry: stat.getWeatherTypes().keySet()) {
                    if (entry.getWeatherDisplayName().equals(precipitation)) {
                        weatherChance = stat.getWeatherTypes().get(entry);
                        remove = false;
                    }
                }
                if (remove) {
                    this.foundedDays.remove(stat);
                    continue;
                }
                double chanceDiff = 100 - weatherChance;
                double tempDiff = Math.abs(stat.getT() - temperature);
                sortedHashMap.put(tempDiff + chanceDiff, stat);
            }
            List<Double> chances = new ArrayList<>(sortedHashMap.keySet());
            Collections.sort(chances);
            this.foundedDays.clear();
            for (Double value : chances) {
                this.foundedDays.add(sortedHashMap.get(value));
            }
            System.out.println(sortedHashMap);
            System.out.println("Size after find:" + this.foundedDays.size());
        }
        return "redirect:/dayFind";
    }

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
            list.add(stat);
        }
        return list;
    }

    public static ArrayList<WeatherStat> sortByDay(List<WeatherStat> list, Date date) {
        ArrayList<WeatherStat> sorted = new ArrayList<>();
        Date currentDateCopy = date;
        while (sorted.size() < list.size()) {
            for (WeatherStat stat : list) {
                if (stat.isDateEqualTo(currentDateCopy)) {
                    sorted.add(stat);
                    currentDateCopy = new Date(currentDateCopy.getTime() + MILLIS_IN_A_DAY);
                }
            }
            currentDateCopy = new Date(currentDateCopy.getTime() + MILLIS_IN_A_DAY);
        }
        return sorted;
    }

    public static HashMap<String, WeatherStat> connectStats(List<WeatherStat> weatherStats, int hour) {
        Queue<WeatherStat> statCopy = new LinkedList<>(weatherStats);
        HashMap<String, WeatherStat> connectedStats = new HashMap<>();
        long time = System.currentTimeMillis();
        while (!statCopy.isEmpty()) {
            WeatherStat stat = statCopy.poll();
            Queue<WeatherStat> statCopy2 = new LinkedList<>(statCopy);
            if (stat.getHour() != hour) {
                continue;
            }
            while (!statCopy2.isEmpty()) {
                WeatherStat stat1 = statCopy2.poll();
                if (stat1.getHour() != hour) {
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

    private HashMap<String, ArrayList<Plan>> plans = new HashMap<>();
    private HashMap<String, HashMap<Plan, WeatherStat>> plansResults = new HashMap<>();
    private boolean creatingPlans = true;

    @GetMapping("/planning")
    public String planning(Map<String, Object> model) {
        /*if (plans.size() == 0) {
            SimpleDateFormat formatForClient = new SimpleDateFormat("d LLLL");
            String currentDate = formatForClient.format(this.currentDate);
            plans.put(currentDate, new ArrayList<>());
        }*/
        model.put("timeIntervals", DayTimeIntervals.values());
        model.put("plans", plans);
        model.put("creatingPlans", creatingPlans);
        model.put("plansResults", plansResults);
        return "planning";
    }

    @PostMapping("/addPlansForNextDay")
    public String addPlansForNextDay(@RequestParam(name = "calendar") String calendar) {
        if (!calendar.equals("")) {
            plans.put(calendar, new ArrayList<>());
        }
        return "redirect:/planning";
    }

    @PostMapping("/removePlansForDay")
    public String removePlansForDay(@RequestParam(name = "key") String key) {
        plans.remove(key);
        return "redirect:/planning";
    }

    @PostMapping("/addPlan")
    public String addPlan(@RequestParam(name = "key", required = false) String key,
                          @RequestParam(name = "time", required = false) Integer time,
                          @RequestParam(name = "plan", required = false) String plan) {
        if (key == null || time == null || plan == null) {
            return "redirect:/planning";
        }
        System.out.println(key);
        if (plans.get(key) != null) {
            ArrayList<Plan> plansForThisDay = plans.get(key);
            if (!this.alreadyHasPlan(plansForThisDay, plan, time)) {
                plansForThisDay.add(new Plan(time, plan));
                plans.put(key, plansForThisDay);
            }
        } else {
            ArrayList<Plan> plansForThisDay = new ArrayList<>();
            if (!this.alreadyHasPlan(plansForThisDay, plan, time)) {
                plansForThisDay.add(new Plan(time, plan));
                plans.put(key, plansForThisDay);
            }
        }
        return "redirect:/planning";
    }

    private boolean alreadyHasPlan(ArrayList<Plan> list, String newPlan, int time) {
        boolean has = false;
        for (Plan plan : list) {
            if (plan.getPlan().equals(newPlan) && plan.getInterval() == time) {
                has = true;
                break;
            }
        }
        return has;
    }

    @PostMapping("/removePlan")
    public String removePlan(@RequestParam(name = "plan") String plan, @RequestParam(name = "key") String key) {
        ArrayList<Plan> plansForDay = plans.get(key);
        plansForDay.removeIf(plan1 -> plan1.getPlan().equals(plan));
        plans.put(key, plansForDay);
        return "redirect:/planning";
    }

    @PostMapping("/showResults")
    public String showResults() {
        this.creatingPlans = !this.creatingPlans;
        if (!this.creatingPlans) {
            System.out.println(plans);
            long time = System.currentTimeMillis();
            HashMap<Integer, ArrayList<Integer>> monthsAndDaysInMonths = new HashMap<>();
            for (Map.Entry<String, ArrayList<Plan>> entry : this.plans.entrySet()) {
                int month = Integer.parseInt(entry.getKey().substring(5, 7));
                int day = Integer.parseInt(entry.getKey().substring(8, 10));
                if (monthsAndDaysInMonths.get(month) != null) {
                    ArrayList<Integer> days = monthsAndDaysInMonths.get(month);
                    days.add(day);
                    monthsAndDaysInMonths.put(month, days);
                } else {
                    ArrayList<Integer> days = new ArrayList<>();
                    days.add(day);
                    monthsAndDaysInMonths.put(month, days);
                }

            }
            Queue<WeatherStat> queue = new LinkedList<>(this.weatherStats);
            Queue<WeatherStat> queueCopy = new LinkedList<>(queue);
            while (!queueCopy.isEmpty()) {
                WeatherStat stat = queueCopy.poll();
                if (!monthsAndDaysInMonths.containsKey(stat.getMonth())) {
                    queue.remove(stat);
                }
                boolean anyDaysContains = false;
                for (Map.Entry<Integer, ArrayList<Integer>> entry : monthsAndDaysInMonths.entrySet()) {
                    if (entry.getKey() == stat.getMonth() && entry.getValue().contains(stat.getDay())) {
                        anyDaysContains = true;
                        break;
                    }
                }
                if (!anyDaysContains) {
                    queue.remove(stat);
                }

            }
            while (!queue.isEmpty()) {
                WeatherStat stat = queue.poll();
                for (Map.Entry<String, ArrayList<Plan>> entry : plans.entrySet()) {
                    int month = Integer.parseInt(entry.getKey().substring(5, 7));
                    int day = Integer.parseInt(entry.getKey().substring(8, 10));
                    if (stat.getMonth() == month && stat.getDay() == day) {
                        for (Plan plan : entry.getValue()) {
                            if (plan.getInterval() == stat.getHour()) {
                                HashMap<Plan, WeatherStat> dayPlans = plansResults.get(entry.getKey());
                                if (dayPlans != null) {
                                    if (dayPlans.get(plan) != null) {
                                        dayPlans.put(plan, WeatherStat.connectTwoStats(dayPlans.get(plan), stat));
                                    } else {
                                        dayPlans.put(plan, stat);
                                    }
                                } else {
                                    HashMap<Plan, WeatherStat> dayPlansNew = new HashMap<>();
                                    dayPlansNew.put(plan, stat);
                                    plansResults.put(entry.getKey(), dayPlansNew);
                                }
                            }
                        }
                    }
                }
            }
            for (HashMap<Plan, WeatherStat> map : plansResults.values()) {
                for (WeatherStat stat : map.values()) {
                    stat.calcWeatherChances();
                }
            }
            long executionTime = System.currentTimeMillis() - time;
            System.out.println("Execution time: " + TimeUnit.MILLISECONDS.toSeconds(executionTime));
            System.out.println(queue);
        } else {
            this.plansResults.clear();
        }
        return "redirect:/planning";
    }
}