package diplom.work.controllerservice.model;

public class PIDState {
    private double kp, ki, kd;
    private double previousError = 0.0;
    private double integral = 0.0;

    public PIDState(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public double update(double target, double current, double dt) {
        double error = target - current;
        integral += error * dt;
        double derivative = (error - previousError) / dt;
        previousError = error;
        return Math.max(0.0, kp * error + ki * integral + kd * derivative);
    }
}
