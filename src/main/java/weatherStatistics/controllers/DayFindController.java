package weatherStatistics.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping
public class DayFindController {

    List<WeatherStat> weatherStats = new ArrayList<>();

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping(value = "/dayFindRedirect")
    public ModelAndView dayFindRedirect(HttpServletRequest request) {
        return dayFind(request, null, null, 0, 0, null);
    }

    @GetMapping("/dayFind")
    public ModelAndView dayFind(HttpServletRequest request,
                                @RequestParam(required = false) Integer startFindId,
                                @RequestParam(required = false) Integer endFindID,
                                @RequestParam(required = false) Integer startTime,
                                @RequestParam(required = false) Integer endTime,
                                @RequestParam(required = false) HashMap<String, Object> model) {
        if (model == null) {
            model = new HashMap<>();
        }
        ArrayList<WeatherStat> foundedDays = new ArrayList<>();
        if (startFindId == null) {
            startFindId = 0;
        }
        if (endFindID == null) {
            endFindID = 3;
        }
        if (model.get("foundedDays") != null) {
            foundedDays = new ArrayList<WeatherStat>((Collection<? extends WeatherStat>) model.get("foundedDays"));
            ArrayList<WeatherStat> finalStats = new ArrayList<>();
            for (int i = startFindId; i < endFindID; i++) {
                finalStats.add(foundedDays.get(i));
            }
            model.put("results", finalStats);
        }
        model.put("allResults", foundedDays);


        model.put("timeStart", startTime);
        model.put("timeEnd", endTime);

        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("startFindId", startFindId);
        model.put("endFindID", endFindID);
        model.put("weatherTypes", WeatherTypes.values());
        model.put("timeIntervals", DayTimeIntervals.values());
        System.out.println("Theme type: " + theme);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("dayFind", model);
    }

    ArrayList<WeatherStat> getListFromStr(String allResults) throws UnsupportedEncodingException {
        String encodedWithISO88591 = allResults;
        allResults = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        allResults = allResults.replaceAll("\\[", "");
        allResults = allResults.replaceAll("\\]", "");
        allResults = allResults.replaceAll("\\{", "");
        String[] results = allResults.split("}, ");
        ArrayList<WeatherStat> stats = new ArrayList<>();
        for (String string : results) {
            stats.add(new WeatherStat(string));
        }
        return stats;
    }

    @PostMapping("/nextFindDays")
    public ModelAndView nextFindDays(HttpServletRequest request, @RequestParam String allResultsNext) throws UnsupportedEncodingException {
        ArrayList<WeatherStat> stats = getListFromStr(allResultsNext);
        int size = allResultsNext.split("}, ").length;
        int startFindId = Integer.parseInt(request.getParameter("startFindId"));
        int endFindID = Integer.parseInt(request.getParameter("endFindID"));
        if (startFindId < size - 3 && endFindID < size) {
            startFindId++;
            endFindID++;
        }
        HashMap<String, Object> model = new HashMap<>();
        model.put("foundedDays", stats);

        Integer timeStart = Integer.valueOf(request.getParameter("timeStart"));
        Integer timeEnd = Integer.valueOf(request.getParameter("timeEnd"));
        return dayFind(request, startFindId, endFindID, timeStart, timeEnd, model);
    }

    @PostMapping("/prevFindDays")
    public ModelAndView prevFindDays(HttpServletRequest request, @RequestParam String allResultsPrev) throws UnsupportedEncodingException {
        ArrayList<WeatherStat> stats = getListFromStr(allResultsPrev);
        int startFindId = Integer.parseInt(request.getParameter("startFindId"));
        int endFindID = Integer.parseInt(request.getParameter("endFindID"));
        if (endFindID - 1 >= 3 && startFindId - 1 >= 0) {
            endFindID--;
            startFindId--;
        }
        HashMap<String, Object> model = new HashMap<>();
        model.put("foundedDays", stats);
        Integer timeStart = Integer.valueOf(request.getParameter("timeStart"));
        Integer timeEnd = Integer.valueOf(request.getParameter("timeEnd"));
        return dayFind(request, startFindId, endFindID, timeStart, timeEnd, model);
    }

    @PostMapping("/findDate")
    public ModelAndView findDate(HttpServletRequest request, @RequestParam String precipitation) throws UnsupportedEncodingException {
        String encodedWithISO88591 = precipitation;
        precipitation = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        HashMap<String, Object> model = new HashMap<>();
        //String precipitation = WeatherTypes.valueOf(precipitationName).getName();
        Integer timeStart = Integer.valueOf(request.getParameter("timeStart"));
        Integer timeEnd = Integer.valueOf(request.getParameter("timeEnd"));
        Integer temperature = Integer.valueOf(request.getParameter("temperature"));
        String day = request.getParameter("calendar");
        int dayNumber = Integer.parseInt(day.split("-")[2]);
        int monthNumber = Integer.parseInt(day.split("-")[1]);
        LocalDate date = LocalDate.of(2020, monthNumber, dayNumber);
        if (this.weatherStats.size() == 0) {
            this.weatherStats = weatherStatistics.findAll();
        }
        ArrayList<WeatherStat> foundedDays = new ArrayList<>();
        if (timeEnd < 0 || timeEnd > 24 || timeStart < 0 || timeStart.equals(timeEnd) || timeEnd < timeStart) {
            return dayFind(request, 0, 3, 0, 0, model);
        }
        if (temperature != null && precipitation != null && timeStart != null && timeEnd != null) {
            HashMap<String, WeatherStat> connectedStats = Algoritms.connectStats(this.weatherStats, timeStart, timeEnd);
            ArrayList<WeatherStat> sortedStatsWithWeather = Algoritms.getListWithWeatherChances(connectedStats);
            foundedDays = Algoritms.sortByDay(sortedStatsWithWeather, date);
            Queue<WeatherStat> queue = new LinkedList<>(foundedDays);

            System.out.println("Size before find:" + foundedDays.size());
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
                    foundedDays.remove(stat);
                    continue;
                }
                double chanceDiff = 100 - weatherChance;
                double tempDiff = Math.abs(stat.getT() - temperature);
                sortedHashMap.put(tempDiff + chanceDiff, stat);
            }
            List<Double> chances = new ArrayList<>(sortedHashMap.keySet());
            Collections.sort(chances);
            foundedDays.clear();
            for (Double value : chances) {
                foundedDays.add(sortedHashMap.get(value));
            }
            System.out.println(sortedHashMap);
            System.out.println("Size after find:" + foundedDays.size());
        }
        for (WeatherStat stat : foundedDays) {
            stat.setWW("null");
            stat.setDD("null");
            stat.setW1("null");
            stat.setW2("null");
        }
        foundedDays = Algoritms.sortByDay(foundedDays, date);
        model.put("foundedDays", foundedDays);
        return dayFind(request, 0, 3,  timeStart, timeEnd, model);
    }
}
