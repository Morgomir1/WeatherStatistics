package weatherStatistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import weatherStatistics.util.DayTimeIntervals;

import java.util.Locale;

@SpringBootApplication
public class WeatherStatisticsApplication {

    public static void main(String[] args) {
        DayTimeIntervals.init();
        SpringApplication.run(WeatherStatisticsApplication.class, args);
    }

}