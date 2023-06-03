package weatherStatistics.entity;

import org.hibernate.annotations.GenericGenerator;
import weatherStatistics.util.WeatherTypes;

import jakarta.persistence.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Entity
@Table(name = "statsnew")
public class WeatherStat {

  @Id
  @GeneratedValue()
  @Column(name = "id")
  private Integer id;
  @Column(name = "day")
  public int day;
  @Column(name = "month")
  public int month;
  @Column(name = "year")
  private int year;
  @Column(name = "hour")
  private int hour;
  @Column(name = "T")
  private double T;
  @Column(name = "Po")
  private double Po;
  @Column(name = "P")
  private double P;
  @Column(name = "Pa")
  private double Pa;
  @Column(name = "U")
  private int U;
  @Column(name = "DD")
  private String DD;
  @Column(name = "WW")
  private String WW;
  @Column(name = "W1")
  private String W1;
  @Column(name = "W2")
  private String W2;

  @Transient
  private boolean positive;

  @Transient
  private int nextHour;

  @Transient
  private HashMap<WeatherTypes, Double> weatherTypes = new HashMap<>();

  @Transient
  private DayOfWeek dayOfWeek;

  @Transient
  private String dayOfWeekStr;

  public WeatherStat() {

  }

  public WeatherStat(String stat) {
    String[] params = stat.split(";");
    try {
      this.id = Integer.valueOf(params[0]);
      this.day = Integer.parseInt(params[1]);
      this.month = Integer.parseInt(params[2]);
      this.year = Integer.parseInt(params[3]);
      this.hour = Integer.parseInt(params[4]);
      this.T = Double.parseDouble(params[5]);
      this.Po = Double.parseDouble(params[6]);
      this.P = Double.parseDouble(params[7]);
      this.Pa = Double.parseDouble(params[8]);
      this.U = Integer.parseInt(params[9]);
      this.DD = params[10];
      this.WW = params[11];
      this.W1 = params[12];
      this.W2 = params[13];
      String weatherTypes = params[14];
      for (String weatherType : weatherTypes.split(", ")) {
        String[] split = weatherType.split("=");
        this.weatherTypes.put(WeatherTypes.getForDisplayName(split[0]), Double.valueOf(split[1].replaceAll("\\}", "")));
      }
    } catch (Exception e) {
      System.out.println("EXEPTION!!!");
      System.out.println(Arrays.toString(params));
    }

  }

  public WeatherStat(int id, int day, int month, int year, int hour, int t, double po, double p, double pa, int u, String DD, String WW,
                     String w1, String w2) {
    this.id = id;
    this.day = day;
    this.month = month;
    this.year = year;
    this.hour = hour;
    this.T = t / 10;
    this.Po = po / 10;
    this.P = p / 10;
    this.Pa = pa / 10;
    this.U = u;
    this.DD = DD;
    this.WW = WW;
    this.W1 = w1;
    this.W2 = w2;
  }

  public int getId() {
      return this.id;
  }

  public int getDay() {
      return this.day;
  }

  public int getMonth() {
      return this.month;
  }

  public int getYear() {
      return this.year;
  }

  public int getHour() {
      return this.hour;
  }

  public String getDate() {
      return this.day + "" + this.month;
  }

  public HashMap<WeatherTypes, Double> getWeatherTypes() {
    return weatherTypes;
  }

  public void setWeatherTypes(HashMap<WeatherTypes, Double> weatherTypes) {
      this.weatherTypes = weatherTypes;
  }

  public boolean isDateEqualTo(LocalDate date) {

      return this.day == date.getDayOfMonth() && this.month == date.getMonthValue();
  }

  public boolean isDateEqualTo(WeatherStat stat) {
    if (stat == null) {
      return false;
    }
    return this.day == stat.day && this.month == stat.month;
  }

  public double getT() {
    return T;
  }

  public double getPo() {
    return Po;
  }

  public double getP() {
    return P;
  }

  public double getPa() {
    return Pa;
  }

  public int getU() {
    return U;
  }

  public String getDD() {
    return DD;
  }

  public String getWW() {
    return WW;
  }

  public String getW1() {
    return W1;
  }

  public String getW2() {
    return W2;
  }


  public void setT(double t) {
      if (t >= 0) {
        positive = true;
      } else {
        positive = false;
      }
      this.T = t;
  }

  public void setPo(double po) {
    Po = po;
  }

  public void setP(double p) {
    P = p;
  }

  public void setPa(double pa) {
    Pa = pa;
  }

  public void setU(int u) {
    U = u;
  }

  public void setDD(String DD) {
    this.DD = DD;
  }

  public void setWW(String WW) {
    this.WW = WW;
  }

  public void setW1(String w1) {
    W1 = w1;
  }

  public void setW2(String w2) {
    W2 = w2;
  }

  public void setNextHour(int nextHour) {
    this.nextHour = nextHour;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public void setHour(int hour) {
    this.hour = hour;
  }

  public void setId(int id) {
    this.id = id;
  }

  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(DayOfWeek dayOfWeek) {
    if (dayOfWeek != null) {
      this.dayOfWeek = dayOfWeek;
      this.dayOfWeekStr = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
  }

  public static WeatherStat connectTwoStats(WeatherStat first, WeatherStat second) {
      WeatherStat stat = new WeatherStat();
      stat.setId(second.id);
      stat.setDay(second.day);
      stat.setMonth(second.month);
      stat.setYear(second.year);
      stat.setHour(second.hour);
      stat.setNextHour(second.hour + 6 == 24 ? 0 : second.hour + 6);
      stat.setDayOfWeek(second.getDayOfWeek());
      stat.setT(Math.round((first.getT() + second.getT()) / 2));
      stat.setPo((first.getPo() + second.getPo()) / 2);
      stat.setPa((first.getPa() + second.getPa()) / 2);
      stat.setP((first.getP() + second.getP()) / 2);
      stat.setU((first.getU() + second.getU()) / 2);
      stat.setDD(first.getDD() + second.getDD());
      stat.setWW(first.getWW() + ";" + second.getWW());

      stat.setW1(first.getW1() + ";" + second.getW1());
      stat.setW2(first.getW2() + ";" + second.getW2());
      //stat.calcWeatherChances();
      return stat;
  }

  public void calcWeatherChances() {
    this.getWeatherTypes().clear();
    for (String weather : this.getW1().split(";")) {
      for (WeatherTypes weather1 : WeatherTypes.values()) {
        if (weather.toLowerCase(Locale.ROOT).contains(weather1.getWeatherName())) {
          this.getWeatherTypes().merge(weather1, 1.0, Double::sum);
        }
      }
    }
    int sum = 0;
    for (Map.Entry<WeatherTypes, Double> weather : this.getWeatherTypes().entrySet()) {
      this.setW1(weather.getKey().getWeatherDisplayName());
      sum += weather.getValue();
    }
    for (Map.Entry<WeatherTypes, Double> weather : this.getWeatherTypes().entrySet()) {
      this.getWeatherTypes().put(weather.getKey(), (double) Math.round(weather.getValue() / sum * 100));
    }
  }

  @Override
  public String toString() {
    return this.id + ";" + this.day  + ";" + this.month + ";" + this.month + ";" + this.hour + ";" + T
            + ";" + Po + ";" + P + ";" + Pa  + ";" + U + ";" + DD + ";" + WW + ";" + W1  + ";" + W2 + ";" + this.weatherTypes;
  }
}