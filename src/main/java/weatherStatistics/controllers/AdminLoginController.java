package weatherStatistics.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.Admin;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.AdminRepo;
import weatherStatistics.util.ThemeTypes;

import java.util.*;

@RestController
@RequestMapping
public class AdminLoginController {

    List<WeatherStat> weatherStats = new ArrayList<>();

    @GetMapping("/adminRedirect")
    public ModelAndView registrationRedirect(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return admin(request);
        } else {
            return login(request);
        }
    }

    int startTableId = 0;
    int endTableId = 6;

    @PostMapping(value = "/adminTableNext")
    public ModelAndView adminTableNext(HttpServletRequest request) {
        if (this.startTableId + 1 < this.weatherStats.size() - 6 && this.endTableId + 1 < this.weatherStats.size()) {
            this.startTableId++;
            this.endTableId++;
        }
        return admin(request);
    }

    @PostMapping(value = "/adminTablePrev")
    public ModelAndView adminTablePrev(HttpServletRequest request) {
        if (this.endTableId - 1 >= 6 && this.startTableId - 1 >= 0) {
            this.endTableId--;
            this.startTableId--;
        }
        return admin(request);
    }

    @GetMapping("/admin")
    public ModelAndView admin(HttpServletRequest request) {
        HashMap<String, Object> model = new HashMap<>();
        model.put("stats", this.weatherStats.subList(startTableId, endTableId));
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("admin", model);
    }

    @GetMapping("/login")
    public ModelAndView login(HttpServletRequest request) {
        HashMap<String, Object> model = new HashMap<>();
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("login", model);
    }
}