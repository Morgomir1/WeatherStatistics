package weatherStatistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Controller
public class MainController {

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
  public String main(Map<String, Object> model) {

    Iterable<WeatherStat> weatherStats = weatherStatistics.findAll();
    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM");
    String currentDate = formatForDateNow.format(this.currentDate);
    System.out.println(formatForDateNow.format(this.currentDate));
    ArrayList<WeatherStat> currentDayStats = new ArrayList<>();
    for (WeatherStat stat : weatherStats) {
        if (stat.isDateEqualTo(currentDate)) {
            currentDayStats.add(stat);
        }
    }
    model.put("weatherStats", currentDayStats);
    SimpleDateFormat formatForClient = new SimpleDateFormat("d LLLL");
    currentDate = formatForClient.format(this.currentDate);
    model.put("currentDate", currentDate);
    return "main";
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

}