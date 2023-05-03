package weatherStatistics.entity;

import antlr.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import weatherStatistics.util.DayTimeIntervals;

import javax.persistence.*;
import javax.xml.crypto.Data;
import java.util.Date;

@Entity
@Table(name = "stats")
public class WeatherStat {

  @Id
  @GenericGenerator(name = "increment", strategy = "increment")
  @GeneratedValue(strategy=GenerationType.AUTO)
  private  String time;

  private  double T;
  private  double Po;
  private  double P;
  private  double Pa;
  private  int U;
  private  String DD;
  private  String WW;
  private  String W1;
  private  String W2;

  @Transient
  private boolean positive;

  @Transient
  private int day;

  @Transient
  private int month;

  @Transient
  private int year;

  @Transient
  private int hour;

  @Transient
  private int nextHour;

  public WeatherStat() {

  }

  public WeatherStat(String time, int t, double po, double p, double pa, int u, String DD, String WW,
                     String w1, String w2) {
    this.time = time;
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

  public int getDay() {
      return Integer.parseInt(time.substring(0, 2));
  }

  public int getMonth() {
      return Integer.parseInt(time.substring(3, 5));
  }

  public int getYear() {
      return Integer.parseInt(time.substring(6, 10));
  }

  public int getHour() {
      return Integer.parseInt(time.substring(11, 13));
  }

  public String getTime() {
    return time;
  }

  public boolean isDateEqualTo(String date) {
    int day = Integer.parseInt(date.substring(0, 2));
    int month = Integer.parseInt(date.substring(2, 4));
    return this.getDay() == day && this.getMonth() == month;
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


  public void setTime(String time) {
    this.time = time;
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

  public static WeatherStat connectTwoStats(WeatherStat first, WeatherStat second) {

    WeatherStat stat = new WeatherStat();
    stat.hour = second.getHour();
    stat.setTime(String.valueOf(second.getHour()));
    stat.setT((first.getT() + second.getT()) / 2);
    stat.setPo((first.getPo() + second.getPo()) / 2);
    stat.setPa((first.getPa() + second.getPa()) / 2);
    stat.setP((first.getP() + second.getP()) / 2);
    stat.setU((first.getU() + second.getU()) / 2);
    stat.setDD(first.getDD() + second.getDD());
    stat.setWW(first.getWW() + second.getWW());
    stat.setW1(first.getW1() + second.getW1());
    stat.setW2(first.getW2() + second.getW2());
    return stat;
  }
}