package diplom.work.controllerservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
        double derivative = (error - previousError) / dt;

        double integralCandidate = integral + error * dt;
        double outputCandidate = kp * error + ki * integralCandidate + kd * derivative;

        double output = Math.max(0.0, Math.min(100.0, outputCandidate));

        // Anti-windup
        if (output == outputCandidate) {
            integral = integralCandidate;
        }

        previousError = error;
        return output;
    }
}
