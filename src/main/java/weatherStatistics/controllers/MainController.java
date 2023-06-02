package weatherStatistics.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.CloudTypes;
import weatherStatistics.util.DayTimeIntervals;
import weatherStatistics.util.Plan;
import weatherStatistics.util.WeatherTypes;


import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;


@RestController
@RequestMapping
public class MainController {

    List<WeatherStat> weatherStats = new ArrayList<>();

    MainController(WeatherStatRepo repository) {
        this.weatherStatistics = repository;
    }

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping("/information")
    public ModelAndView information() {
        HashMap<String, Object> model = new HashMap<>();
        model.put("theme", this.theme);
        return new ModelAndView("information", model);
    }

    private LocalDate currentDate = LocalDate.now();
    private List<WeatherStat> statsForDownLoad = new ArrayList<>();

    private void putDayInModel(Map<String, Object> model, LocalDate currentDate) {
        HashMap<Integer, WeatherStat> weatherStatHashMap = new HashMap<>();
        for (DayTimeIntervals interval : DayTimeIntervals.values()) {
            weatherStatHashMap.put(interval.getHour(), null);
        }
        for (WeatherStat stat : weatherStats) {
            if (stat.isDateEqualTo(currentDate)) {
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
        list = sortByHour(list);
        model.put("weatherStats", list);
        model.put("statsForDownLoad", list);
    }

    private void putMonthInModel(Map<String, Object> model, LocalDate currentDate) {
        int month = currentDate.getMonthValue();
        ArrayList<WeatherStat> weatherStatsCopy = new ArrayList<>(this.weatherStats);
        for (WeatherStat stat : this.weatherStats) {
            if (stat.month != month) {
                weatherStatsCopy.remove(stat);
            }
        }
        HashMap<String, WeatherStat> connectedStats = connectStats(weatherStatsCopy, null);
        for (WeatherStat stat : connectedStats.values()) {
            stat.calcWeatherChances();
        }
        ArrayList<WeatherStat> stats = new ArrayList<>(connectedStats.values());
        stats = sortByMonth(stats);
        model.put("statsForDownLoad", stats);
        model.put("weatherStats1", stats.subList(0, 8));
        model.put("weatherStats2", stats.subList(8, 16));
        model.put("weatherStats3", stats.subList(16, 24));
        model.put("weatherStats4", stats.subList(24, stats.size()));
    }

    private void putWeekInModel(Map<String, Object> model, LocalDate currentDate) {
        ArrayList<LocalDate> week = new ArrayList<>();
        week.add(currentDate);
        while (week.size() != 7) {
            week.add(week.get(week.size() - 1).plusDays(1));
        }
        List<WeatherStat> stats = new LinkedList<>(this.weatherStats);
        Queue<WeatherStat> statsCopy = new LinkedList<>(this.weatherStats);
        while (!statsCopy.isEmpty()) {
            WeatherStat stat = statsCopy.poll();
            boolean delete = true;
            for (LocalDate date : week) {
                if (stat.isDateEqualTo(date)) {
                    stat.setDayOfWeek(date.getDayOfWeek());
                    delete = false;
                    break;
                }
            }
            if (delete) {
                stats.remove(stat);
            }
        }
        HashMap<String, WeatherStat> connectedStats = connectStats(stats, null);
        for (WeatherStat stat : connectedStats.values()) {
            stat.calcWeatherChances();
        }
        stats = new ArrayList<>(connectedStats.values());
        stats = sortByMonth(stats);
        stats = sortByWeek(stats);
        model.put("weatherStats", stats);
        model.put("statsForDownLoad", stats);
    }

    @GetMapping
    @ResponseBody
    public ModelAndView main(@RequestParam(required = false) LocalDate date) {
        Map<String, Object> model = new HashMap<>();
        this.weatherStats = weatherStatistics.findAll();
        DateTimeFormatter formatForClient;
        LocalDate currentDate;
        if (date != null) {
            currentDate = date;
        } else {
            currentDate = LocalDate.now();
        }
        if (this.displayType == MainMenuDisplayTypes.DAYS) {
            formatForClient = DateTimeFormatter.ofPattern("d LLLL");
            putDayInModel(model, currentDate);
        } else if (this.displayType == MainMenuDisplayTypes.MONTH){
            formatForClient = DateTimeFormatter.ofPattern("LLLL");
            putMonthInModel(model, currentDate);
        } else {
            formatForClient = DateTimeFormatter.ofPattern("LLLL");
            putWeekInModel(model, currentDate);
        }

        model.put("day", currentDate.getDayOfMonth());
        model.put("month", currentDate.getMonth());
        model.put("monthNumber", currentDate.getMonthValue());
        model.put("isDay", this.displayType == MainMenuDisplayTypes.DAYS);
        model.put("isMonth", this.displayType == MainMenuDisplayTypes.MONTH);
        model.put("isWeek", this.displayType == MainMenuDisplayTypes.WEEKS);
        model.put("theme", this.theme);
        return new ModelAndView("main", model);
    }

    @PostMapping("/nextDay")
    public ModelAndView nextDay(@RequestParam String day) {
        int dayNumber = Integer.parseInt(day.split(" ")[0]);
        int monthNumber = Integer.parseInt(day.split(" ")[1]);
        LocalDate date = LocalDate.of(2020, monthNumber, dayNumber);
        date = date.plusDays(1);
        /*if (this.displayType == MainMenuDisplayTypes.MONTH) {
            int month = date.getMonthValue();
            while (month == this.currentDate.getMonthValue()) {
                this.currentDate = this.currentDate.minusDays(1);
            }
        } else {
            this.currentDate = this.currentDate.minusDays(1);
        } */

        return this.main(date);
    }

    @PostMapping("/prevDay")
    public ModelAndView prevDay(@RequestParam String day) {
        int dayNumber = Integer.parseInt(day.split(" ")[0]);
        int monthNumber = Integer.parseInt(day.split(" ")[1]);
        LocalDate date = LocalDate.of(2020, monthNumber, dayNumber);
        date = date.minusDays(1);
        /*if (this.displayType == MainMenuDisplayTypes.MONTH) {
            int month = date.getMonthValue();
            while (month == this.currentDate.getMonthValue()) {
                this.currentDate = this.currentDate.minusDays(1);
            }
        } else {
            this.currentDate = this.currentDate.minusDays(1);
        } */

        return this.main(date);
    }

    @PostMapping("/downloadTable")
    public void downloadTable(HttpServletResponse response, @RequestParam ArrayList<WeatherStat> statsForDownLoad) throws IOException {
        String csvFileName = "results.csv";
        response.setContentType("text/csv;charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
        response.setHeader(headerKey, headerValue);
        List<WeatherStat> list = new ArrayList<>(statsForDownLoad);
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] header = { "id", "day", "month", "year", "hour", "T", "Po", "P", "Pa", "U", "DD", "WW", "W1", "W2"};
        csvWriter.writeHeader(header);
        for (WeatherStat stat : list) {
            csvWriter.write(stat, header);
        }
        csvWriter.close();
    }

    public static ArrayList<WeatherStat> sortByWeek(List<WeatherStat> list) {
        ArrayList<WeatherStat> newList = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            for (WeatherStat stat : list) {
                if (stat.getDayOfWeek() == dayOfWeek) {
                    newList.add(stat);
                }
            }
        }
        return newList;
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

    @GetMapping(value = "/informationRedirect")
    public String informationRedirect() {
        return "redirect:/information";
    }

    @GetMapping(value = "/dayFindRedirect")
    public String dayFindRedirect() {
        return "redirect:/dayFind";
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
        model.put("theme", this.theme);
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
            WeatherStat statWithMinDay = null;
            for (WeatherStat stat : list) {
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

    private HashMap<String, ArrayList<Plan>> plans = new HashMap<>();
    private HashMap<String, HashMap<Plan, WeatherStat>> plansResults = new HashMap<>();
    private boolean creatingPlans = true;

    @GetMapping("/planning")
    public String planning(Map<String, Object> model) {
        model.put("timeIntervals", DayTimeIntervals.values());
        model.put("plans", plans);
        model.put("creatingPlans", creatingPlans);
        model.put("plansResults", plansResults);
        model.put("theme", this.theme);
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

    /*
        Settings
     */

    private enum MainMenuDisplayTypes {
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

    private enum ThemeTypes {
        BLUE("style", "Бирюзовая"),
        BLACK("black", "Тёмная");

        private String name;
        private String displayName;

        ThemeTypes(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        public String getName() {
            return name;
        }
    }

    private MainMenuDisplayTypes displayType = MainMenuDisplayTypes.DAYS;
    private ThemeTypes theme = ThemeTypes.BLUE;

    @GetMapping(value = "/settingsRedirect")
    public String settingsRedirect() {

        return "redirect:/settings";
    }

    @PostMapping("/setupSettings")
    public String setupSettings(@RequestParam(name = "displayType", required = false) String displayType,
                                @RequestParam(name = "theme", required = false) String theme) {
        if (displayType != null) {
            this.displayType = MainMenuDisplayTypes.valueOf(displayType);
            if (this.displayType == MainMenuDisplayTypes.WEEKS) {
                LocalDate now = LocalDate.now();
                while (now.getDayOfWeek() != DayOfWeek.MONDAY) {
                    now = now.minusDays(1);
                }
                this.currentDate = now;
            }
        }
        if (theme != null) {
            this.theme = ThemeTypes.valueOf(theme);
        }
        return "redirect:/settings";
    }

    @GetMapping("/settings")
    public String settings(HashMap<String, Object> model) {
        model.put("displayTypes", MainMenuDisplayTypes.values());
        model.put("themeTypes", ThemeTypes.values());
        model.put("theme", this.theme);
        return "settings";
    }

    int startTableId = 0;
    int endTableId = 6;

    @PostMapping(value = "/adminTableNext")
    public String adminTableNext() {
        if (this.startTableId + 1 < this.weatherStats.size() - 6 && this.endTableId + 1 < this.weatherStats.size()) {
            this.startTableId++;
            this.endTableId++;
        }
        return "redirect:/admin";
    }

    @PostMapping(value = "/adminTablePrev")
    public String adminTablePrev() {
        if (this.endTableId - 1 >= 6 && this.startTableId - 1 >= 0) {
            this.endTableId--;
            this.startTableId--;
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String admin(HashMap<String, Object> model) {
        model.put("stats", this.weatherStats.subList(startTableId, endTableId));
        model.put("theme", this.theme);
        return "admin";
    }

    @GetMapping("/login")
    public String login(HashMap<String, Object> model) {
        model.put("theme", this.theme);
        return "login";
    }
}