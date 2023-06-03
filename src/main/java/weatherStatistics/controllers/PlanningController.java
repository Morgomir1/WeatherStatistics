package weatherStatistics.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import weatherStatistics.entity.WeatherStat;
import weatherStatistics.repo.WeatherStatRepo;
import weatherStatistics.util.DayTimeIntervals;
import weatherStatistics.util.Plan;
import weatherStatistics.util.ThemeTypes;

import java.io.UnsupportedEncodingException;
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

    @GetMapping("/planning")
    public ModelAndView planning(HttpServletRequest request,
                                 HashMap<String, ArrayList<Plan>> plans,
                                 HashMap<String, HashMap<Plan, WeatherStat>> planResults,
                                 boolean creatingPlans) {

        Map<String, Object> model = new HashMap<>();
        model.put("timeIntervals", DayTimeIntervals.values());
        if (plans == null) {
            plans = new HashMap<String, ArrayList<Plan>>();
        }
        if (planResults == null) {
            planResults = new HashMap<String, HashMap<Plan, WeatherStat>>();
        }
        model.put("plans", plans);
        model.put("plansResults", planResults);
        model.put("creatingPlans", creatingPlans);
        String theme = request.getParameter("theme");
        String display = request.getParameter("display");
        model.put("display", display);
        model.put("theme", theme == null ? ThemeTypes.BLUE.getThemeName() : theme);
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
                            dayPlansList.add(new Plan(Integer.parseInt(intervalAndPlan[0]), intervalAndPlan[1]));
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
        System.out.println(plans);
        if (!calendar.equals("")) {
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
                                @RequestParam(name = "time", required = false) Integer time,
                                @RequestParam(name = "plan", required = false) String plan,
                                @RequestParam(name = "plans") String plans) throws UnsupportedEncodingException {
        HashMap<String, ArrayList<Plan>> newPlans = parsePlansFromStr(plans);
        System.out.println(plans);
        String encodedWithISO88591 = plan;
        plan = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
        if (key == null || time == null || plan == null) {
            return planning(request, newPlans, null, true);
        }
        if (newPlans.get(key) != null) {
            ArrayList<Plan> plansForThisDay = newPlans.get(key);
            if (!this.alreadyHasPlan(plansForThisDay, plan, time)) {
                plansForThisDay.add(new Plan(time, plan));
                newPlans.put(key, plansForThisDay);
            }
        } else {
            ArrayList<Plan> plansForThisDay = new ArrayList<>();
            if (!this.alreadyHasPlan(plansForThisDay, plan, time)) {
                plansForThisDay.add(new Plan(time, plan));
                newPlans.put(key, plansForThisDay);
            }
        }
        return planning(request, newPlans, null, true);
    }

    private boolean alreadyHasPlan(ArrayList<Plan> list, String newPlan, int time) {
        boolean has = false;
        for (Plan plan : list) {
            if (plan.getPlan().equals(newPlan) && plan.getInterval() == time) {
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
            while (!queue.isEmpty()) {
                WeatherStat stat = queue.poll();
                for (Map.Entry<String, ArrayList<Plan>> entry : newPlans.entrySet()) {
                    int month = Integer.parseInt(entry.getKey().substring(5, 7));
                    int day = Integer.parseInt(entry.getKey().substring(8, 10));
                    if (stat.getMonth() == month && stat.getDay() == day) {
                        for (Plan plan : entry.getValue()) {
                            if (plan.getInterval() == stat.getHour()) {
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
