package weatherStatistics.controllers;

import jakarta.persistence.GeneratedValue;
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

    private static final int TABLE_STEP = 100;

    private ArrayList<WeatherStat> allStats = new ArrayList<>();
    int allStatsSize = 0;

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping("/adminRedirect")
    public ModelAndView registrationRedirect(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return admin(request, null, null, 100);
        } else {
            return login(request);
        }
    }

    @PostMapping(value = "/adminTableNext")
    public ModelAndView adminTableNext(HttpServletRequest request,
                                       @RequestParam Integer size,
                                       @RequestParam(required = false) Integer tableStep) {

        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = 0;
        int diff = endTableId + tableStep - size;
        if (diff > 0) {
            startTableId = size - diff;
            endTableId = size;
        } else {
            startTableId = endTableId;
            endTableId += tableStep;
        }
        return admin(request, startTableId, endTableId, tableStep);
    }




    @PostMapping(value = "/adminTablePrev")
    public ModelAndView adminTablePrev(HttpServletRequest request,
                                       @RequestParam(required = false) Integer tableStep) {

        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int diff = startTableId - tableStep;
        if (diff < 0) {
            endTableId = tableStep;
            startTableId = 0;
        } else {
            endTableId = startTableId;
            startTableId -= tableStep;
        }
        return admin(request, startTableId, endTableId, tableStep);
    }

    private class TableStep {

        private int value;
        private boolean selected = false;

        public TableStep(int value, boolean selected) {
            this.value = value;
            this.selected = selected;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    @GetMapping("/admin")
    public ModelAndView admin(HttpServletRequest request,
                              @RequestParam(required = false) Integer startTableId,
                              @RequestParam(required = false) Integer endTableId,
                              @RequestParam(required = false) Integer tableStep) {
        HashMap<String, Object> model = new HashMap<>();
        ArrayList<WeatherStat> statsList = (ArrayList<WeatherStat>) weatherStatistics.findAll();
        Collections.reverse(statsList);
        if (startTableId == null) {
            startTableId = 0;
        }
        if (tableStep == null) {
            tableStep = 100;
        }
        if (endTableId == null) {
            endTableId = tableStep;
        }
        model.put("stats", statsList.subList(startTableId, endTableId));
        model.put("size", statsList.size());
        model.put("allSize", weatherStatistics.findAll().size());
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        ArrayList<TableStep> tableSteps = new ArrayList<>();
        tableSteps.add(new TableStep(100, 100 == tableStep));
        tableSteps.add(new TableStep(200, 200 == tableStep));
        tableSteps.add(new TableStep(300, 300 == tableStep));
        tableSteps.add(new TableStep(400, 400 == tableStep));
        tableSteps.add(new TableStep(500, 500 == tableStep));
        model.put("tableSteps", tableSteps);
        model.put("display", display);
        model.put("startTableId", startTableId);
        model.put("endTableId", endTableId);
        model.put("tableStep", tableStep);
        model.put("pagesSize", statsList.size() / tableStep);
        model.put("currentPage", (statsList.size() - (statsList.size() - endTableId)) / tableStep);
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

    @GeneratedValue
    @PostMapping("/onButtonClick")
    public ModelAndView onButtonClick(HttpServletRequest request,
                                      @RequestParam(required = false) ArrayList<String> statsId,
                                      @RequestParam(required = false) Integer tableStep,
                                      @RequestParam(required = false) String action,
                                      @RequestParam String stats
                                      ) throws UnsupportedEncodingException {
        String encodedWithISO88591 = stats;
        stats = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        encodedWithISO88591 = action;
        action = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        stats = stats.replaceAll("\\{", "");
        stats = stats.replaceAll("\\[", "");
        stats = stats.replaceAll("\\]", "");
        String[] statsArray = stats.split(",");
        List<WeatherStat> list = new ArrayList<WeatherStat>();
        ArrayList<String> statList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < statsArray.length; i++) {
            statList.add(statsArray[i]);
            if (index == 13) {
                index = 0;
                String[] statArr = new String[13];
                for (int j = 0; j < statArr.length; j++) {
                    statArr[j] = statList.get(j);
                }
                WeatherStat stat = new WeatherStat(statArr);
                statList.clear();
                list.add(stat);
                continue;
            }
            index++;
            //String[] statArray = statsArray.
        }
        if (action.equals("Удалить выбранное")) {
            if (statsId != null) {
                for (String stat : statsId) {
                    weatherStatistics.deleteById(Integer.valueOf(stat));
                }
            }
        } else {
            for (WeatherStat stat : list) {
                weatherStatistics.save(stat);
            }
        }

        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        return admin(request, startTableId, endTableId, tableStep);
    }

    @PostMapping("goToPage")
    public ModelAndView goToPage(HttpServletRequest request,
                                 @RequestParam(required = true) Integer tableStep,
                                 @RequestParam(required = true) Integer page,
                                 @RequestParam(required = true) Integer currentPage) {
        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        if (page.equals(currentPage) || page < 0 || page > Integer.parseInt(request.getParameter("pagesSize"))) {
            return admin(request, startTableId, endTableId, tableStep);
        } else {
            startTableId = page * tableStep;
            int maxSize = this.weatherStatistics.findAll().size();
            if (startTableId > maxSize) {
                startTableId = maxSize - tableStep;
            }
            endTableId = startTableId + tableStep;
            if (endTableId > maxSize) {
                endTableId = maxSize;
            }
        }
        return admin(request, startTableId, endTableId, tableStep);
    }


    @PostMapping("/setStep")
    public ModelAndView setStep(HttpServletRequest request, @RequestParam(required = false) Integer tableStep) {
        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        return admin(request, startTableId, endTableId, tableStep);
    }

    @PostMapping("/change")
    public ModelAndView change(HttpServletRequest request,
                                @RequestParam String stats,
                                @RequestParam(required = false) Integer tableStep) {
        try {
            WeatherStat stat = new WeatherStat(stats.split(","));
            weatherStatistics.save(stat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int endTableId = Integer.parseInt(request.getParameter("endTableId"));
        int startTableId = Integer.parseInt(request.getParameter("startTableId"));
        return admin(request, startTableId, endTableId, tableStep);
    }
}