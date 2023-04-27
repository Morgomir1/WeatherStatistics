package weatherStatistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;

import java.util.Map;

@Controller
public class MainController {

  @Autowired
  private WeatherStatRepo weatherStatistics;

  @GetMapping("/greeting")
  public String greeting(
          @RequestParam(name="name", required=false, defaultValue="World") String name,
          Map<String, Object> model
  ) {
    model.put("name", name);
    return "greeting";
  }

  @GetMapping
  public String main(Map<String, Object> model) {
    Iterable<WeatherStat> weatherStats = weatherStatistics.findAll();
    for (WeatherStat stat : weatherStats) {
      System.out.println(stat.getTime());
    }
    model.put("weatherStats", weatherStats);
    return "main";
  }
}