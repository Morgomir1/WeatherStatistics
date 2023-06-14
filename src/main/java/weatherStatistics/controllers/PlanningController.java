package weatherStatistics.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.DayTimeIntervals;
import weatherStatistics.util.Plan;
import weatherStatistics.util.ThemeTypes;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping
public class PlanningController {

    List<WeatherStat> weatherStats = new ArrayList<>();

    @Autowired
    private WeatherStatRepo weatherStatistics;

    @GetMapping(value = "/planningRedirect")
    public ModelAndView planningRedirect(HttpServletRequest request) {
        return planning(request, null, null, true);
    }

    //private HashMap<String, ArrayList<Plan>> plans = new HashMap<>();
   // private HashMap<String, HashMap<Plan, WeatherStat>> plansResults = new HashMap<>();
    //private boolean creatingPlans = true;

    public static LinkedHashMap<String, ArrayList<Plan>> sortByMonthList(HashMap<String, ArrayList<Plan>> list) {
        LinkedHashMap<String, ArrayList<Plan>> sorted = new LinkedHashMap<String, ArrayList<Plan>>();
        while (!list.isEmpty()) {
            int minDay = Integer.MAX_VALUE;
            int minMonth = Integer.MAX_VALUE;
            String statWithMinDay = null;
            ArrayList<Plan> object = null;
            for (Map.Entry<String, ArrayList<Plan>> stat : list.entrySet()) {
                int month = Integer.parseInt(stat.getKey().substring(5, 7));
                int day = Integer.parseInt(stat.getKey().substring(8, 10));
                if (minMonth > month) {
                    minMonth = month;
                    minDay = day;
                    statWithMinDay = stat.getKey();
                    object = stat.getValue();
                }
                if (minDay > day && minMonth == month) {
                    minDay = day;
                    statWithMinDay = stat.getKey();
                    object = stat.getValue();
                }
            }
            if (statWithMinDay != null) {
                list.remove(statWithMinDay);
                sorted.put(statWithMinDay, object);
            }
        }
        return sorted;
    }

    public static LinkedHashMap sortByMonth(HashMap<String, HashMap<Plan, WeatherStat>> list) {
        LinkedHashMap<String, Object> sorted = new LinkedHashMap<String, Object>();
        while (!list.isEmpty()) {
            int minDay = Integer.MAX_VALUE;
            int minMonth = Integer.MAX_VALUE;
            String statWithMinDay = null;
            Object object = null;
            for (Map.Entry<String, HashMap<Plan, WeatherStat>> stat : list.entrySet()) {
                int month = Integer.parseInt(stat.getKey().substring(5, 7));
                int day = Integer.parseInt(stat.getKey().substring(8, 10));
                if (minMonth > month) {
                    minMonth = month;
                    minDay = day;
                    statWithMinDay = stat.getKey();
                    object = stat.getValue();
                    continue;
                }
                if (minDay > day) {
                    minDay = day;
                    statWithMinDay = stat.getKey();
                    object = stat.getValue();
                }
            }
            if (statWithMinDay != null) {
                list.remove(statWithMinDay);
                sorted.put(statWithMinDay, object);
            }
        }
        return sorted;
    }

    @GetMapping("/planning")
    public ModelAndView planning(HttpServletRequest request,
                                 HashMap<String, ArrayList<Plan>> plans,
                                 HashMap<String, HashMap<Plan, WeatherStat>> planResults,
                                 boolean creatingPlans) {

        HashMap<String, Object> model = new HashMap<>();
        model.put("timeIntervals", DayTimeIntervals.values());
        if (plans == null) {
            plans = new HashMap<String, ArrayList<Plan>>();
        }
        if (planResults == null) {
            planResults = new HashMap<String, HashMap<Plan, WeatherStat>>();
        }
        plans = sortByMonthList(plans);
        planResults = sortByMonth(planResults);

        model.put("plans", plans);
        model.put("plansResults", planResults);
        model.put("creatingPlans", creatingPlans);
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.put("admin", !(authentication instanceof AnonymousAuthenticationToken));
        return new ModelAndView("planning", model);
    }


    private HashMap<String, ArrayList<Plan>> parsePlansFromStr(String plans) throws UnsupportedEncodingException {
        String encodedWithISO88591 = plans;
        plans = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        plans = plans.replaceAll("\\{", "");
        plans = plans.replaceAll("\\}", "");
        plans = plans.replaceAll("\\[", "");
        String[] plansForDays = plans.split("], ");
        HashMap<String, ArrayList<Plan>> newPlans = new HashMap<>();
        if (!plans.equals("")) {
            for (String planForDay : plansForDays) {
                String[] dayAndPlans = planForDay.split("=");
                String day = dayAndPlans[0];
                String dayPlans = "";
                ArrayList<Plan> dayPlansList = new ArrayList<>();
                if (dayAndPlans.length == 2) {
                    dayPlans = dayAndPlans[1];
                    dayPlans = dayPlans.replaceAll("\\]", "");
                    for (String plan : dayPlans.split(", ")) {
                        if (!plan.equals("")) {
                            plan = plan.replaceAll("\\(", "");
                            plan = plan.replaceAll("\\)", "");
                            String[] intervalAndPlan = plan.split(";");
                            dayPlansList.add(new Plan(Integer.parseInt(intervalAndPlan[0]), Integer.parseInt(intervalAndPlan[1]), intervalAndPlan[2]));
                        }
                    }
                }
                newPlans.put(day, dayPlansList);
            }
        }
        return  newPlans;
    }

    @PostMapping("/addPlansForNextDay")
    public ModelAndView addPlansForNextDay(HttpServletRequest request,
                                           @RequestParam(name = "calendar") String calendar,
                                           @RequestParam(name = "plans") String plans) throws UnsupportedEncodingException {
        HashMap<String, ArrayList<Plan>> newPlans = parsePlansFromStr(plans);
        //System.out.println(plans);

        if (!calendar.equals("") && newPlans.size() < 10) {
            newPlans.put(calendar, new ArrayList<>());
        }
        return planning(request, newPlans, null, true);
    }

    @PostMapping("/removePlansForDay")
    public ModelAndView removePlansForDay(HttpServletRequest request,
                                          @RequestParam(name = "plans") String plans,
                                          @RequestParam(name = "key") String key) throws UnsupportedEncodingException {
        HashMap<String, ArrayList<Plan>> newPlans = parsePlansFromStr(plans);
        newPlans.remove(key);
        return planning(request, newPlans, null, true);
    }

    @PostMapping("/addPlan")
    public ModelAndView addPlan(HttpServletRequest request,
                                @RequestParam(name = "key", required = false) String key,
                                @RequestParam(name = "timeStart", required = false) Integer timeStart,
                                @RequestParam(name = "timeEnd", required = false) Integer timeEnd,
                                @RequestParam(name = "plan", required = false) String plan,
                                @RequestParam(name = "plans") String plans) throws UnsupportedEncodingException {
        HashMap<String, ArrayList<Plan>> newPlans = parsePlansFromStr(plans);
        //System.out.println(plans);
        if (plan != null) {
            String encodedWithISO88591 = plan;
            plan = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        }
        if (key == null || timeStart == null || timeEnd == null || plan == null
                || timeEnd < 0
                || timeEnd > 24
                || timeStart < 0
                || timeStart.equals(timeEnd)
                || timeEnd < timeStart) {
            return planning(request, newPlans, null, true);
        }
        if (newPlans.size() < 10) {
            if (newPlans.get(key) != null) {
                ArrayList<Plan> plansForThisDay = newPlans.get(key);
                if (!this.alreadyHasPlan(plansForThisDay, plan, timeStart, timeEnd)) {
                    plansForThisDay.add(new Plan(timeStart, timeEnd, plan));
                    newPlans.put(key, plansForThisDay);
                }
            } else {
                ArrayList<Plan> plansForThisDay = new ArrayList<>();
                if (!this.alreadyHasPlan(plansForThisDay, plan, timeStart, timeEnd)) {
                    plansForThisDay.add(new Plan(timeStart, timeEnd, plan));
                    newPlans.put(key, plansForThisDay);
                }
            }
        }
        return planning(request, newPlans, null, true);
    }

    private boolean alreadyHasPlan(ArrayList<Plan> list, String newPlan, int startTime, int endTime) {
        boolean has = false;
        for (Plan plan : list) {
            if (plan.getPlan().equals(newPlan) && plan.getStartTime() == startTime && plan.getStartTime() == endTime) {
                has = true;
                break;
            }
        }
        return has;
    }

    @PostMapping("/removePlan")
    public ModelAndView removePlan(HttpServletRequest request,
                             @RequestParam(name = "plan") String plan,
                             @RequestParam(name = "key") String key,
                             @RequestParam(name = "plans") String plans) throws UnsupportedEncodingException {
        String encodedWithISO88591 = plan;
        plan = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        HashMap<String, ArrayList<Plan>> newPlans = parsePlansFromStr(plans);
        ArrayList<Plan> plansForDay = newPlans.get(key);
        String finalPlan = plan;
        plansForDay.removeIf(plan1 -> plan1.getPlan().equals(finalPlan));
        newPlans.put(key, plansForDay);
        return planning(request, newPlans, null, true);
    }

    @PostMapping("/showResults")
    public ModelAndView showResults(HttpServletRequest request,
                                    @RequestParam(name = "plans") String plans) throws UnsupportedEncodingException {
        this.weatherStats = weatherStatistics.findAll();
        boolean creatingPlans = Boolean.parseBoolean(request.getParameter("creatingPlans"));
        HashMap<String, ArrayList<Plan>> newPlans = parsePlansFromStr(plans);
        HashMap<String, HashMap<Plan, WeatherStat>> plansResults = new HashMap<>();
        creatingPlans = !creatingPlans;
        if (!creatingPlans) {
            System.out.println(newPlans);
            long time = System.currentTimeMillis();
            HashMap<Integer, ArrayList<Integer>> monthsAndDaysInMonths = new HashMap<>();
            for (Map.Entry<String, ArrayList<Plan>> entry : newPlans.entrySet()) {
                int month = Integer.parseInt(entry.getKey().substring(5, 7));
                int day = Integer.parseInt(entry.getKey().substring(8, 10));
                if (monthsAndDaysInMonths.get(month) != null) {
                    ArrayList<Integer> days = monthsAndDaysInMonths.get(month);
                    days.add(day);
                    monthsAndDaysInMonths.put(month, days);
                } else {
                    ArrayList<Integer> days = new ArrayList<>();
                    days.add(day);
                    monthsAndDaysInMonths.put(month, days);
                }

            }
            Queue<WeatherStat> queue = new LinkedList<>(this.weatherStats);
            Queue<WeatherStat> queueCopy = new LinkedList<>(queue);
            while (!queueCopy.isEmpty()) {
                WeatherStat stat = queueCopy.poll();
                if (!monthsAndDaysInMonths.containsKey(stat.getMonth())) {
                    queue.remove(stat);
                }
                boolean anyDaysContains = false;
                for (Map.Entry<Integer, ArrayList<Integer>> entry : monthsAndDaysInMonths.entrySet()) {
                    if (entry.getKey() == stat.getMonth() && entry.getValue().contains(stat.getDay())) {
                        anyDaysContains = true;
                        break;
                    }
                }
                if (!anyDaysContains) {
                    queue.remove(stat);
                }

            }
            System.out.println(queue.size());
            while (!queue.isEmpty()) {
                WeatherStat stat = queue.poll();
                for (Map.Entry<String, ArrayList<Plan>> entry : newPlans.entrySet()) {
                    int month = Integer.parseInt(entry.getKey().substring(5, 7));
                    int day = Integer.parseInt(entry.getKey().substring(8, 10));
                    if (stat.getMonth() == month && stat.getDay() == day) {
                        for (Plan plan : entry.getValue()) {
                            if (plan.getStartTime() <= stat.getHour() && plan.getEndTime() >= stat.getHour()
                                    || ((plan.getStartTime() - stat.getHour()) * (stat.getHour() + 3 - plan.getStartTime()) >= 0)
                                    || ((plan.getEndTime() - stat.getHour()) * (stat.getHour() + 3 - plan.getEndTime()) >= 0)) {
                                HashMap<Plan, WeatherStat> dayPlans = plansResults.get(entry.getKey());
                                if (dayPlans != null) {
                                    if (dayPlans.get(plan) != null) {
                                        dayPlans.put(plan, WeatherStat.connectTwoStats(dayPlans.get(plan), stat));
                                    } else {
                                        dayPlans.put(plan, stat);
                                    }
                                } else {
                                    HashMap<Plan, WeatherStat> dayPlansNew = new HashMap<>();
                                    dayPlansNew.put(plan, stat);
                                    plansResults.put(entry.getKey(), dayPlansNew);
                                }
                            }
                        }
                    }
                }
            }
            for (HashMap<Plan, WeatherStat> map : plansResults.values()) {
                for (WeatherStat stat : map.values()) {
                    stat.calcWeatherChances();
                }
            }
            long executionTime = System.currentTimeMillis() - time;
            System.out.println("Execution time: " + TimeUnit.MILLISECONDS.toSeconds(executionTime));
            System.out.println(queue);
        }
        return planning(request, newPlans, plansResults, creatingPlans);
    }


}
