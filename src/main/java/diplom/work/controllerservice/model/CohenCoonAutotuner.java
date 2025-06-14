package diplom.work.controllerservice.model;

import java.util.List;

/**
 * Автотюнер PID-контроллера по методу Коэна–Куна
 */
public class CohenCoonAutotuner {
    record PIDParams(double kp, double ki, double kd){}
    private final double stepChange;
    CohenCoonAutotuner(double stepChange){ if(stepChange==0) throw new IllegalArgumentException(); this.stepChange=stepChange; }

    PIDParams tune(List<Double> t, List<Double> y){
        double y0 = y.getFirst();
        double yss = y.subList(Math.max(0,y.size()-5),y.size()).stream().mapToDouble(Double::doubleValue).average().orElse(y.getLast());
        double dY = yss-y0;
        if(dY<0.8) throw new IllegalStateException("ΔY "+dY+"°C — слишком мало для автотюна");
        double dead = interpolate(t,y, y0+0.10*dY);
        double t63 = interpolate(t,y, y0+0.632*dY);
        double T = t63-dead; if(T<=0) throw new IllegalStateException("bad T");
        double Kpr = dY/stepChange; double R = dead/T;
        double kp = (1/Kpr)*(T/dead + 0.333);
        double Ti = T*(30+3*R)/(9+20*R);
        double Td = T*R/(11+2*R);
        double ki = kp/Ti, kd = kp*Td;
        return new PIDParams(kp,ki,kd);
    }
    private double interpolate(List<Double> t,List<Double> y,double thr){
        for(int i=0;i<y.size()-1;i++){
            double y0=y.get(i),y1=y.get(i+1);
            if((y0<thr&&y1>=thr)||(y0>thr&&y1<=thr)){
                double frac=(thr-y0)/(y1-y0); return t.get(i)+frac*(t.get(i+1)-t.get(i)); }
        }
        return t.getLast();
    }
}

