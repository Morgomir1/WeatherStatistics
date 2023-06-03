package weatherStatistics.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.util.MainMenuDisplayTypes;
import weatherStatistics.util.ThemeTypes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;

@RestController
@RequestMapping
public class SettingsController {

    @GetMapping(value = "/settingsRedirect")
    public ModelAndView settingsRedirect(HttpServletRequest request) {
        return settings(request, null);
    }

    @PostMapping("/setupSettings")
    public ModelAndView setupSettings(HttpServletRequest request) {
        HashMap<String, Object> model = new HashMap<>();
        String displayTypeStr = request.getParameter("displayType");
        String theme = request.getParameter("themeType");
        LocalDate currentDate;
        if (theme != null) {
            ThemeTypes themeType = ThemeTypes.valueOf(theme);
            if (themeType != null) {
                model.put("theme", themeType.getThemeName());
            }
        }
        if (displayTypeStr != null) {
            MainMenuDisplayTypes displayType = MainMenuDisplayTypes.valueOf(displayTypeStr);
            LocalDate now = LocalDate.now();
            if (displayType == MainMenuDisplayTypes.WEEKS) {
                while (now.getDayOfWeek() != DayOfWeek.MONDAY) {
                    now = now.minusDays(1);
                }

            }
            model.put("display", displayType.name());
            currentDate = now;
            model.put("day", currentDate.getDayOfMonth());
            model.put("month", currentDate.getMonth());
            model.put("monthNumber", currentDate.getMonthValue());
        } else {
            model.put("display", MainMenuDisplayTypes.DAYS.name());
        }
        return settings(request, model);
    }

    @GetMapping("/settings")
    public ModelAndView settings(HttpServletRequest request, @RequestParam(required = false) HashMap<String, Object> model) {
        String theme;
        if (model == null) {
            theme = request.getParameter("theme");
            model = new HashMap<>();
            model.put("display", MainMenuDisplayTypes.DAYS.name());
        } else {
            theme = (String) model.get("theme");
        }
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        model.put("displayTypes", MainMenuDisplayTypes.values());
        model.put("themeTypes", ThemeTypes.values());
        return new ModelAndView("settings", model);
    }
}
