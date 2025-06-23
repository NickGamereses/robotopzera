package armato;

import java.awt.Color;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class CarroArmatoDiFuoco extends AdvancedRobot {
  double d = 150.0D;
  
  double dir = 1.0D;
  
  boolean saindoDaParede = false;
  
  String alvo = "nenhum";
  
  double vidaAlvo;
  
  double distAlvo;
  
  boolean escolhendoAlvo = false;
  
  String tiroAnterior = "";
  
  String[] outros;
  
  double[] disOutros;
  
  public void run() {
    setColors(Color.RED, Color.BLACK, Color.RED, new Color(200, 25, 0), new Color(150, 150, 200));
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    prepararParaEscolherAlvo();
    while (true) {
      setAhead((this.distAlvo / 4.0D + 25.0D) * this.dir);
      if (!this.saindoDaParede && vaiBater(3.0D)) {
        this.dir *= -1.0D;
        this.saindoDaParede = true;
      } else if (!vaiBater(3.2D)) {
        this.saindoDaParede = false;
      } 
      execute();
    } 
  }
  
  public void onScannedRobot(ScannedRobotEvent e) {
    if (getOthers() == 1) {
      this.alvo = e.getName();
    } else {
      if (this.escolhendoAlvo) {
        for (int i = 0; i < this.outros.length; i++) {
          if (this.outros[i].equals(e.getName()))
            return; 
          if (this.outros[i].equals("")) {
            this.outros[i] = e.getName();
            this.disOutros[i] = e.getDistance();
            this.out.println("Encontrado " + this.outros[i] + " Dist: " + (Math.round(this.disOutros[i] * 10.0D) / 10L) + ")");
            if (i == this.outros.length - 1) {
              this.out.println("Todos robanalizados, escolhendo alvo");
              escolherAlvo();
            } 
            i = this.outros.length;
          } 
        } 
        return;
      } 
      if (!e.getName().equals(this.alvo) && e.getEnergy() < this.vidaAlvo && e.getDistance() <= this.distAlvo) {
        this.out.println("Novo alvo por dist (" + (Math.round(e.getDistance() * 10.0D) / 10.0D) + "<" + (Math.round(this.distAlvo * 10.0D) / 10.0D) + ") e vida: " + e.getName() + " (" + (Math.round(e.getEnergy() * 10.0D) / 10.0D) + "<" + (Math.round(this.vidaAlvo * 10.0D) / 10.0D) + ")");
        this.alvo = e.getName();
      } 
    } 
    if (!e.getName().equals(this.alvo))
      return; 
    if (!this.saindoDaParede && this.vidaAlvo - e
      .getEnergy() >= 0.1D && this.vidaAlvo - e
      .getEnergy() <= 3.0D && (this.vidaAlvo - e
      .getEnergy() <= 0.57D || this.vidaAlvo - e.getEnergy() >= 0.63D))
      this.dir *= -1.0D; 
    if (!vaiBater(0.6D))
      if (e.getDistance() > getHeight() * 3.0D) {
        setTurnRight(e.getBearing() + 90.0D - 40.0D * this.dir);
      } else {
        setTurnRight(e.getBearing() + 90.0D - 10.0D * this.dir);
      }  
    double absBearing = e.getBearing() + getHeading();
    setTurnRadarRight(ajustarRadar(absBearing));
    setTurnGunRightRadians(ajustarArma(e) * 0.85D);
    setFire(calcularPoderTiro(e.getDistance()));
    this.vidaAlvo = e.getEnergy();
    this.distAlvo = e.getDistance();
  }
  
  public void onHitByBullet(HitByBulletEvent e) {
    if (getOthers() > 1) {
      if ((this.distAlvo > this.d || e.getName().equals(this.tiroAnterior)) && this.vidaAlvo > 18.0D) {
        this.out.println("Novo alvo por ser atingindo! " + e.getName());
        this.alvo = e.getName();
        double radarBearing = Utils.normalRelativeAngleDegrees(e.getBearing() + getHeading() - getRadarHeading());
        if (radarBearing > 0.0D) {
          setTurnRadarRight(Double.POSITIVE_INFINITY);
        } else {
          setTurnRadarRight(Double.NEGATIVE_INFINITY);
        } 
      } 
      if (!e.getName().equals(this.alvo))
        this.tiroAnterior = e.getName(); 
    } 
  }
  
  public void onHitRobot(HitRobotEvent e) {
    this.dir *= -1.0D;
    if (!this.escolhendoAlvo && !e.getName().equals(this.alvo) && e.getEnergy() < this.vidaAlvo) {
      this.out.println("Novo alvo por menor vida! " + e.getName() + " (" + (Math.round(e.getEnergy() * 10.0D) / 10.0D) + "<" + (Math.round(this.vidaAlvo * 10.0D) / 10.0D) + ")");
      this.alvo = e.getName();
      double radarBearing = Utils.normalRelativeAngleDegrees(e.getBearing() + getHeading() - getRadarHeading());
      if (radarBearing > 0.0D) {
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      } else {
        setTurnRadarRight(Double.NEGATIVE_INFINITY);
      } 
    } 
  }
  
  public void onHitWall(HitWallEvent e) {
    this.saindoDaParede = true;
    if (this.dir == -1.0D && Math.abs(e.getBearing()) >= 160.0D) {
      this.dir = 1.0D;
    } else if (this.dir == 1.0D && Math.abs(e.getBearing()) <= 20.0D) {
      this.dir = -1.0D;
    } else if (this.dir == 1.0D) {
      setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing()));
      this.dir = -1.0D;
    } else {
      setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 180.0D));
      this.dir = 1.0D;
    } 
  }
  
  public void onRobotDeath(RobotDeathEvent e) {
    if (e.getName().equals(this.alvo)) {
      if (getOthers() > 1) {
        this.out.println("Alvo morreu! Procurando novo inimigo (" + getOthers() + ")");
        prepararParaEscolherAlvo();
      } else {
        this.out.println("Alvo morreu! Procurando inimigo (" + getOthers() + ")");
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      } 
    } else if (this.escolhendoAlvo) {
      prepararParaEscolherAlvo();
    } 
  }
  
  void prepararParaEscolherAlvo() {
    this.outros = new String[getOthers()];
    this.disOutros = new double[getOthers()];
    for (int i = 0; i < this.outros.length; i++)
      this.outros[i] = ""; 
    this.escolhendoAlvo = true;
    setTurnRadarRight(Double.POSITIVE_INFINITY);
  }
  
  void escolherAlvo() {
    this.escolhendoAlvo = false;
    int roboEscolhido = -1;
    double menorDis = Double.MAX_VALUE;
    for (int i = 0; i < this.outros.length; i++) {
      if (this.disOutros[i] < menorDis) {
        roboEscolhido = i;
        menorDis = this.disOutros[i];
      } 
    } 
    this.out.println("Novo alvo: " + this.outros[roboEscolhido] + " (Dist" + (Math.round(this.disOutros[roboEscolhido] * 10.0D) / 10.0D) + ")");
    this.alvo = this.outros[roboEscolhido];
  }
  
  double ajustarRadar(double absBearing) {
    if (getOthers() > 1) {
      double correcaoRadar = Utils.normalRelativeAngleDegrees(absBearing - getRadarHeading());
      correcaoRadar += 22.5D * Math.signum(correcaoRadar);
      if (correcaoRadar > 45.0D) {
        correcaoRadar = 45.0D;
      } else if (correcaoRadar < -45.0D) {
        correcaoRadar = -45.0D;
      } else if (correcaoRadar > 0.0D && correcaoRadar < 20.0D) {
        correcaoRadar = 20.0D;
      } else if (correcaoRadar > -20.0D && correcaoRadar <= 0.0D) {
        correcaoRadar = -20.0D;
      } 
      return correcaoRadar;
    } 
    return Utils.normalRelativeAngleDegrees((absBearing - getRadarHeading()) * 2.0D);
  }
  
  double ajustarArma(ScannedRobotEvent e) {
    if (e.getEnergy() != 0.0D) {
      double absBearingRad = getHeadingRadians() + e.getBearingRadians();
      double compensacaoLinear = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearingRad) / Rules.getBulletSpeed(calcularPoderTiro(e.getDistance()));
      if (e.getDistance() <= 120.0D) {
        compensacaoLinear *= 0.5D;
      } else if (e.getDistance() <= 60.0D) {
        compensacaoLinear *= 0.3D;
      } 
      return Utils.normalRelativeAngle(absBearingRad - getGunHeadingRadians() + compensacaoLinear);
    } 
    return Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getGunHeadingRadians());
  }
  
  double calcularPoderTiro(double dAlvo) {
    if (this.vidaAlvo != 0.0D) {
      if (dAlvo < getHeight() * 1.5D)
        return 3.0D; 
      if (getEnergy() > 16.0D || (
        getOthers() == 1 && getEnergy() > 9.0D)) {
        if (dAlvo <= this.d * 2.0D)
          return 3.0D; 
        return Math.min(1.1D + this.d * 2.0D / dAlvo, 3.0D);
      } 
      if (getEnergy() > 2.2D)
        return 1.1D; 
      return Math.max(0.1D, getEnergy() / 3.0D);
    } 
    return 0.1D;
  }
  
  boolean vaiBater(double margem) {
    return (getX() + getHeight() * margem >= getBattleFieldWidth() || 
      getX() - getHeight() * margem <= 0.0D || 
      getY() + getHeight() * margem >= getBattleFieldHeight() || 
      getY() - getHeight() * margem <= 0.0D);
  }
}