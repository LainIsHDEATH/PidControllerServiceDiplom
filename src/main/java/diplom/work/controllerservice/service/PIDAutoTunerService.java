package diplom.work.controllerservice.service;

import diplom.work.controllerservice.model.PIDState;
import diplom.work.controllerservice.util.PeakDetector;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PIDAutoTunerService {

    private final List<Double> outputValues = new ArrayList<>();
    private final List<Double> timestamps = new ArrayList<>();
    private double currentKp = 0.5;
    private final double KpStep = 0.01;
    private final double maxKp = 100.0;
    private boolean tuningComplete = false;
    private double Ku;
    private double Pu;

    public void record(double outputValue, double timestamp) {
        if (tuningComplete) return;

        outputValues.add(outputValue);
        timestamps.add(timestamp);

        // Проверяем если достаточно данных
        if (timestamps.size() >= 50) { // например после 50 измерений
            if (detectSustainedOscillations()) {
                tuningComplete = true;
                Ku = currentKp;
                Pu = PeakDetector.calculatePu(outputValues, timestamps);
            } else {
                // Если нет колебаний — увеличиваем Kp
                currentKp += KpStep;
                if (currentKp > maxKp) {
                    throw new IllegalStateException("Auto-tuning failed: Kp exceeded limit without achieving oscillations.");
                }
                outputValues.clear();
                timestamps.clear();
            }
        }
    }

    public boolean isTuningComplete() {
        return tuningComplete;
    }

    public PIDState getTunedPID() {
        if (!tuningComplete) {
            throw new IllegalStateException("Tuning is not complete!");
        }
        // Формулы Зиглера-Николса для PID
        double kp = 0.6 * Ku;
        double ki = 2.0 * kp / Pu;
        double kd = kp * Pu / 8.0;
        System.out.println("kp: " + kp + ", ki: " + ki + ", kd: " + kd);
        return new PIDState(kp, ki, kd);
    }

    public double getCurrentKp() {
        return currentKp;
    }

    private boolean detectSustainedOscillations() {
        // Упрощённо: считаем, что есть устойчивые колебания, если
        // зафиксировано не менее 3-4 пиков за последнюю часть выборки
        try {
            List<Double> peaks = PeakDetector.findPeakTimestamps(outputValues, timestamps);
            return peaks.size() >= 4;
        } catch (Exception e) {
            return false;
        }
    }
}
