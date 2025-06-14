package diplom.work.controllerservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PID-контроллер с автотюнингом по методу Коэна–Куна прямо в потоке вызовов.
 * На начальном этапе контроллер работает в open-loop режиме:
 * 1) Ждет установившегося состояния при базовой мощности (basePower).
 * 2) Применяет ступеньку (basePower + stepChange) и собирает реакцию.
 * 3) После стабилизации вычисляет параметры PID и переходит в замкнутый режим.
 */
@Getter
@Setter
public class CohenCoonPidController implements PowerController {
    private enum Stage{WAIT,STEP,COLLECT,CONTROL}
    private Stage stage=Stage.WAIT;
    private final double basePower, stepChange, stabilityThr; private final int stableWin;
    private final List<Double> tBuf=new ArrayList<>(), yBuf=new ArrayList<>();
    private double tStepStart;
    private double kp,ki,kd,integral,prevErr; private final double setpoint;
    private static final double MAX_POWER=2_000;
    private static final int MIN_STEP_SEC = 3600;   // 30 мин
    private static final double TARGET_RISE = 1.0; // °C
    private static final double DT_DTHR=0.01;

    public CohenCoonPidController(double setpoint, double basePower, double stepChange, int stableWin, double stabilityThr){
        this.setpoint=setpoint; this.basePower=basePower; this.stepChange=stepChange; this.stableWin=stableWin; this.stabilityThr=stabilityThr; }

    private boolean stable(List<Double> data){
        if(data.size()<stableWin) return false;
        double max= Collections.max(data.subList(data.size()-stableWin,data.size()));
        double min=Collections.min(data.subList(data.size()-stableWin,data.size()));
        return max-min<=stabilityThr; }

    @Override public synchronized double compute(double target,double cur,double dt,long ts){
        double t=ts/1000.0;
        switch(stage){
            case WAIT-> { tBuf.add(t); yBuf.add(cur); if(stable(yBuf)){ stage=Stage.STEP; return basePower+stepChange; } return basePower; }
            case STEP-> { tStepStart=t; tBuf.clear(); yBuf.clear(); tBuf.add(0.0); yBuf.add(cur); stage=Stage.COLLECT; return basePower+stepChange; }
            case COLLECT-> {
                double rel=t-tStepStart; tBuf.add(rel); yBuf.add(cur);
                boolean plateau=false; if(tBuf.size()>1){ int n=tBuf.size(); double dTdt=(yBuf.get(n-1)-yBuf.get(n-2))/(tBuf.get(n-1)-tBuf.get(n-2)); plateau=Math.abs(dTdt)<DT_DTHR; }
                boolean longEnough=rel>=MIN_STEP_SEC;
                if(stable(yBuf)&&plateau&&longEnough){
                    CohenCoonAutotuner.PIDParams p=new CohenCoonAutotuner(stepChange).tune(tBuf,yBuf);
                    kp=p.kp(); ki=p.ki(); kd=p.kd(); integral=0; prevErr=target-cur; stage=Stage.CONTROL; return clamp(kp*prevErr); }
                return basePower+stepChange; }
            case CONTROL-> {
                double err=target-cur; integral+=err*dt; double dTerm=kd*((err-prevErr)/dt); prevErr=err;
                dTerm=Math.max(-0.2*MAX_POWER,Math.min(0.2*MAX_POWER,dTerm));
                return clamp(kp*err+ki*integral+dTerm); }
        }
        throw new IllegalStateException(); }
    private double clamp(double v){ return Math.max(0,Math.min(MAX_POWER,v)); }
    public boolean isTuned(){ return stage==Stage.CONTROL; }
    public double getKp(){return kp;} public double getKi(){return ki;} public double getKd(){return kd;}
}
