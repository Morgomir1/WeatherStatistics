package weatherStatistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.DayTimeIntervals;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/planning")
    public String planning() {
        return "planning";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping("/information")
    public String information() {
        return "information";
    }

    @GetMapping("/dayFind")
    public String dayFind() {
        return "dayFind";
    }

    private Date currentDate = new Date();

    @GetMapping
    public String main(Map < String, Object > model) {
        Iterable < WeatherStat > weatherStats = weatherStatistics.findAll();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("ddMM");
        String currentDate = formatForDateNow.format(this.currentDate);
        HashMap < Integer, WeatherStat > weatherStatHashMap = new HashMap < > ();
        weatherStatHashMap.put(0, new WeatherStat());
        weatherStatHashMap.put(6, new WeatherStat());
        weatherStatHashMap.put(12, new WeatherStat());
        weatherStatHashMap.put(18, new WeatherStat());
        for (WeatherStat stat: weatherStats) {
            if (stat.isDateEqualTo(currentDate)) {
                for (DayTimeIntervals dayTimeIntervals: DayTimeIntervals.values()) {
                    if (dayTimeIntervals.getHour() == stat.getHour()) {
                        if (weatherStatHashMap.get(stat.getHour()) != null) {
                            weatherStatHashMap.put(stat.getHour(),
                                    WeatherStat.connectTwoStats(weatherStatHashMap.get(stat.getHour()), stat));

                        }
                        break;
                    }
                }
            }
        }
        ArrayList < WeatherStat > list = new ArrayList < > ();
        for (WeatherStat stat: weatherStatHashMap.values()) {
            list.add(stat);
        }
        model.put("weatherStats", sortByHour(list));
        SimpleDateFormat formatForClient = new SimpleDateFormat("d LLLL");
        currentDate = formatForClient.format(this.currentDate);
        model.put("currentDate", currentDate);
        return "main";
    }

    public static ArrayList < WeatherStat > sortByHour(List < WeatherStat > list) {
        ArrayList < WeatherStat > newList = new ArrayList < > ();
        for (DayTimeIntervals intervals: DayTimeIntervals.values()) {
            for (WeatherStat stat: list) {
                if (Integer.parseInt(stat.getTime()) == intervals.getHour()) {
                    stat.setNextHour(intervals.getNext().getHour());
                    double value = stat.getT() / 10;
                    double scale = Math.pow(10, 1);
                    stat.setT(Math.ceil(value * scale) / scale);
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

}