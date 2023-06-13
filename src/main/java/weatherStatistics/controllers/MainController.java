package weatherStatistics.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.*;


import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;


@RestController
@RequestMapping
public class MainController {

    List<WeatherStat> weatherStats = new ArrayList<>();

    @Autowired
    private WeatherStatRepo weatherStatistics;

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
        ArrayList<WeatherStat> list = Algoritms.getListWithWeatherChances(weatherStatHashMap);
        list = sortByHour(list);
        model.put("weatherStats", list);
        model.put("statsForDownload", list);
    }

    private void putMonthInModel(Map<String, Object> model, LocalDate currentDate) {
        int month = currentDate.getMonthValue();
        ArrayList<WeatherStat> weatherStatsCopy = new ArrayList<>(this.weatherStats);
        for (WeatherStat stat : this.weatherStats) {
            if (stat.month != month) {
                weatherStatsCopy.remove(stat);
            }
        }
        HashMap<String, WeatherStat> connectedStats = Algoritms.connectStats(weatherStatsCopy, null);
        for (WeatherStat stat : connectedStats.values()) {
            stat.calcWeatherChances();
        }
        ArrayList<WeatherStat> stats = new ArrayList<>(connectedStats.values());
        stats = Algoritms.sortByMonth(stats);
        model.put("weatherStats1", stats.subList(0, 8));
        model.put("weatherStats2", stats.subList(8, 16));
        model.put("weatherStats3", stats.subList(16, 24));
        model.put("weatherStats4", stats.subList(24, stats.size()));
        List<WeatherStat> list = Algoritms.getListWithWeatherChances(connectedStats);

        model.put("statsForDownload", list);
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
        HashMap<String, WeatherStat> connectedStats = Algoritms.connectStats(stats, null);
        for (WeatherStat stat : connectedStats.values()) {
            stat.calcWeatherChances();
        }
        stats = Algoritms.getListWithWeatherChances(connectedStats);
        //stats = new ArrayList<>(connectedStats.values());
        stats = Algoritms.sortByMonth(stats);
        //stats = sortByWeek(stats);
        model.put("weatherStats", stats);
        model.put("statsForDownload", stats);
    }

    @GetMapping
    public ModelAndView main(@RequestParam(required = false) LocalDate date, HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        this.weatherStats = weatherStatistics.findAll();

        String theme = request.getParameter("theme");
        String display = request.getParameter("display");

        DateTimeFormatter formatForClient;
        LocalDate currentDate;
        if (date != null) {
            currentDate = date;
        } else {
            currentDate = LocalDate.now();
        }
        if (display != null) {
            for (MainMenuDisplayTypes type : MainMenuDisplayTypes.values()) {
                if (type.name().equals(display)) {
                    display = type.name();
                }
            }
        } else {
            display = MainMenuDisplayTypes.DAYS.name();
        }

        if (display.equals(MainMenuDisplayTypes.DAYS.name())) {
            formatForClient = DateTimeFormatter.ofPattern("d LLLL");
            putDayInModel(model, currentDate);
        } else if (display.equals(MainMenuDisplayTypes.MONTH.name())){
            formatForClient = DateTimeFormatter.ofPattern("LLLL");
            putMonthInModel(model, currentDate);
        } else {
            formatForClient = DateTimeFormatter.ofPattern("LLLL");
            putWeekInModel(model, currentDate);
        }
        model.put("day", currentDate.getDayOfMonth());
        String month = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        model.put("month", month);
        model.put("monthNumber", currentDate.getMonthValue());
        model.put("dateDisplay", formatForClient.format(currentDate));
        model.put("isDay", display.equals(MainMenuDisplayTypes.DAYS.name()));
        model.put("isMonth", display.equals(MainMenuDisplayTypes.MONTH.name()));
        model.put("isWeek", display.equals(MainMenuDisplayTypes.WEEKS.name()));
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("main", model);
    }

    @RequestMapping("/nextDay")
    public ModelAndView nextDay(HttpServletRequest request) {
        String day = request.getParameter("day");
        int dayNumber = Integer.parseInt(day.split(" ")[0]);
        int monthNumber = Integer.parseInt(day.split(" ")[1]);
        LocalDate date = LocalDate.of(2020, monthNumber, dayNumber);
        String displayType = request.getParameter("display");
        MainMenuDisplayTypes type = MainMenuDisplayTypes.valueOf(displayType);
        if (type == MainMenuDisplayTypes.MONTH) {
            int month = date.getMonthValue();
            while (date.getMonthValue() == month) {
                date = date.plusDays(1);
            }
        } else if (type == MainMenuDisplayTypes.WEEKS) {
            date = date.plusDays(7);
        } else {
            date = date.plusDays(1);
        }
        return this.main(date, request);
    }

    @PostMapping("/prevDay")
    public ModelAndView prevDay(HttpServletRequest request) {
        String day = request.getParameter("day");
        int dayNumber = Integer.parseInt(day.split(" ")[0]);
        int monthNumber = Integer.parseInt(day.split(" ")[1]);
        LocalDate date = LocalDate.of(2020, monthNumber, dayNumber);
        String displayType = request.getParameter("display");
        MainMenuDisplayTypes type = MainMenuDisplayTypes.valueOf(displayType);
        if (type == MainMenuDisplayTypes.MONTH) {
            int month = date.getMonthValue();
            while (date.getMonthValue() == month) {
                date = date.minusDays(1);
            }
        } else if (type == MainMenuDisplayTypes.WEEKS) {
            date = date.minusDays(7);
        } else {
            date = date.minusDays(1);
        }
        return this.main(date, request);
    }

    @PostMapping("/downloadTable")
    public void downloadTable(HttpServletResponse response, HttpServletRequest request, @RequestParam(required = true) String statsForDownload) throws IOException {
        String encodedWithISO88591 = statsForDownload;
        statsForDownload = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        String csvFileName = "results.csv";
        response.setContentType("text/csv;charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
        response.setHeader(headerKey, headerValue);
        statsForDownload = statsForDownload.replaceAll("\\{", "");
        statsForDownload = statsForDownload.replaceAll("\\[", "");
        statsForDownload = statsForDownload.replaceAll("\\]", "");
        String[] statsArray = statsForDownload.split("}, ");
        List<WeatherStat> list = new ArrayList<WeatherStat>();
        for (String str : statsArray) {
            WeatherStat stat = new WeatherStat(str);
            list.add(stat);
        }
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] header = { "id", "day", "month", "year", "hour", "T", "Po", "P", "U", "DD", "WW", "W1", "W2", "weatherTypes"};
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
    public ModelAndView mainRedirect(HttpServletRequest request) {
        return main(null, request);
    }
}