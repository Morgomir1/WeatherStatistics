package weatherStatistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import weatherStatistics.util.DayTimeIntervals;

@SpringBootApplication
public class ServingWebContentApplication {

    public static void main(String[] args) {
        DayTimeIntervals.init();
        SpringApplication.run(ServingWebContentApplication.class, args);
    }

}