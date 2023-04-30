package weatherStatistics.entity;

import antlr.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "stats")
public class WeatherStat {

  @Id
  @GenericGenerator(name = "increment", strategy = "increment")
  @GeneratedValue(strategy=GenerationType.AUTO)
  private  String time;
  private  int T;
  private  double Po;
  private  double P;
  private  double Pa;
  private  int U;
  private  String DD, WW, W1, W2;

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

  public String getTime() {
    return time;
  }

  public boolean isDateEqualTo(String date) {
    String thisDate = this.time.substring(0, 5);
    thisDate = StringUtils.stripFront(thisDate, ".");
    return thisDate.equals(date);
  }

  public int getT() {
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

  public void setT(int t) {
    T = t;
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

}