package diplom.work.controllerservice.model.PidImpl;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MiniPID {

    private double P = 0;
    private double I = 0;
    private double D = 0;
    private double F = 0;
    private final double dt;
    private double maxIOutput = 0;
    private double maxError = 0;
    private double errorSum = 0;
    private double maxOutput = 0;
    private double minOutput = 0;
    @Setter
    private double setpoint = 0;
    private double lastActual = 0;
    private boolean firstRun = true;
    private boolean reversed = false;
    private double outputRampRate = 0;
    private double lastOutput = 0;
    private double outputFilter = 0;
    private double setpointRange = 0;


    public MiniPID(double p, double i, double d, double dt) {
        P = p; I = i; D = d;
        this.dt = dt;
        checkSigns();
    }

    public void setMaxIOutput(double maximum) {
        maxIOutput = maximum;
        if (I != 0) {
            maxError = maxIOutput / I;
        }
    }

    public void setOutputLimits(double minimum, double maximum) {
        if (maximum < minimum) return;
        maxOutput = maximum;
        minOutput = minimum;

        if (maxIOutput == 0 || maxIOutput > (maximum - minimum)) {
            setMaxIOutput(maximum - minimum);
        }
    }

    public double getOutput(double actual, double setpoint, double dt) {
        double output; double Poutput; double Ioutput; double Doutput; double Foutput;

        this.setpoint = setpoint;

        if (setpointRange != 0) {
            setpoint = constrain(setpoint, actual - setpointRange, actual + setpointRange);
        }

        double error = setpoint - actual;
        Foutput = F * setpoint;
        Poutput = P * error;

        if (firstRun) {
            lastActual = actual;
            lastOutput = Poutput + Foutput;
            firstRun = false;
        }

        double derivative = (actual - lastActual) / dt;
        Doutput = -D * derivative;
        lastActual = actual;

        Ioutput = I * errorSum;
        if (maxIOutput != 0) {
            Ioutput = constrain(Ioutput, -maxIOutput, maxIOutput);
        }

        output = Foutput + Poutput + Ioutput + Doutput;

        if (minOutput != maxOutput && !bounded(output, minOutput, maxOutput)) {
            errorSum = error;
        } else if (outputRampRate != 0 && !bounded(output, lastOutput - outputRampRate, lastOutput + outputRampRate)) {
            errorSum = error;
        } else if (maxIOutput != 0) {
            errorSum = constrain(errorSum + error * dt, -maxError, maxError);
        } else {
            errorSum += error * dt;
        }

        if (outputRampRate != 0) {
            output = constrain(output, lastOutput - outputRampRate, lastOutput + outputRampRate);
        }
        if (minOutput != maxOutput) {
            output = constrain(output, minOutput, maxOutput);
        }
        if (outputFilter != 0) {
            output = lastOutput * outputFilter + output * (1 - outputFilter);
        }

        lastOutput = output;
        return output;
    }

    private double constrain(double value, double min, double max) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

    private boolean bounded(double value, double min, double max) {
        return (min < value) && (value < max);
    }

    private void checkSigns() {
        if (reversed) {
            if (P > 0) P *= -1;
            if (I > 0) I *= -1;
            if (D > 0) D *= -1;
            if (F > 0) F *= -1;
        } else {
            if (P < 0) P *= -1;
            if (I < 0) I *= -1;
            if (D < 0) D *= -1;
            if (F < 0) F *= -1;
        }
    }
}
