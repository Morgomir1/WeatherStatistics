package weatherStatistics.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.util.ThemeTypes;

import java.util.HashMap;

@RestController
@RequestMapping
public class InformationController {

    @GetMapping("/informationRedirect")
    public ModelAndView informationRedirect(HttpServletRequest request) {
        return information(request);
    }

    @GetMapping("/information")
    public ModelAndView information(HttpServletRequest request) {
        HashMap<String, Object> model = new HashMap<>();
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("information", model);
    }
}
