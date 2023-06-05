package weatherStatistics.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.MainMenuDisplayTypes;
import weatherStatistics.util.ThemeTypes;

import java.io.UnsupportedEncodingException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;

@RestController
@RequestMapping
public class AdminController {

    private static final int TABLE_STEP = 5000;

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping("/adminRedirect")
    public ModelAndView registrationRedirect(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return admin(request, null, null);
        } else {
            return login(request);
        }
    }

    @PostMapping(value = "/adminTableNext")
    public ModelAndView adminTableNext(HttpServletRequest request,
                                       @RequestParam Integer size) {

        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = 0;
        int diff = endTableId + TABLE_STEP - size;
        if (diff > 0) {
            startTableId = size - diff;
            endTableId = size;
        } else {
            startTableId = endTableId;
            endTableId += TABLE_STEP;
        }
        return admin(request, startTableId, endTableId);
    }




    @PostMapping(value = "/adminTablePrev")
    public ModelAndView adminTablePrev(HttpServletRequest request,
                                       @RequestParam Integer size) {

        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int diff = startTableId - TABLE_STEP;
        if (diff < 0) {
            endTableId = TABLE_STEP;
            startTableId = 0;
        } else {
            endTableId = startTableId;
            startTableId -= TABLE_STEP;
        }
        return admin(request, startTableId, endTableId);
    }

    @GetMapping("/admin")
    public ModelAndView admin(HttpServletRequest request,
                              @RequestParam(required = false) Integer startTableId,
                              @RequestParam(required = false) Integer endTableId) {
        HashMap<String, Object> model = new HashMap<>();
        ArrayList<WeatherStat> statsList = (ArrayList<WeatherStat>) weatherStatistics.findAll();
        Collections.reverse(statsList);
        if (startTableId == null) {
            startTableId = 0;
        }
        if (endTableId == null) {
            endTableId = TABLE_STEP;
        }
        model.put("stats", statsList.subList(startTableId, endTableId));
        model.put("size", statsList.size());
        model.put("allSize", weatherStatistics.findAll().size());
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("startTableId", startTableId);
        model.put("endTableId", endTableId);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("admin", model);
    }



    @GetMapping("/login")
    public ModelAndView login(HttpServletRequest request) {
        HashMap<String, Object> model = new HashMap<>();
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        if (display == null) {
            display = MainMenuDisplayTypes.DAYS.name();
        }
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        return new ModelAndView("login", model);
    }

    @PostMapping("/logOut")
    public ModelAndView logOut(HttpServletRequest request) throws ServletException {
        request.logout();
        return login(request);
    }

    /*@PostMapping("/findStart")
    public ModelAndView findStart(HttpServletRequest request) throws ServletException {
        return findInTable(request, weatherStatistics.findAll().size());
    }

    @PostMapping("/findEnd")
    public ModelAndView findEnd(HttpServletRequest request) throws ServletException {
        return findInTable(request, 7);
    }

    @PostMapping("/findInTable")
    public ModelAndView findInTable(HttpServletRequest request, @RequestParam Integer id) throws ServletException {
        int endTableId = 0;
        int startTableId = 0;
        List<WeatherStat> stats = weatherStatistics.findAll();
        Collections.reverse(stats);
        for (WeatherStat stat1 : stats) {
            startTableId++;
            if (stat1.getId() == id) {
                break;
            }
        }
        endTableId = startTableId + TABLE_STEP;

        return admin(request, startTableId, endTableId);
    }*/

    private String decode(String str) throws UnsupportedEncodingException {
        String encodedWithISO88591 = str;
        return new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
    }

    @PostMapping("/onButtonClick")
    public ModelAndView onButtonClick(HttpServletRequest request, @RequestParam(required = false) ArrayList<String> statsId) throws UnsupportedEncodingException {
        for (String stat : statsId) {
            weatherStatistics.deleteById(Integer.valueOf(stat));
        }
        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        return admin(request, startTableId, endTableId);
    }
    @PostMapping("/change")
    public ModelAndView change(HttpServletRequest request, @RequestParam String stats) {
        try {
            WeatherStat stat = new WeatherStat(stats.split(","));
            weatherStatistics.save(stat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        return admin(request, startTableId, endTableId);
    }
}