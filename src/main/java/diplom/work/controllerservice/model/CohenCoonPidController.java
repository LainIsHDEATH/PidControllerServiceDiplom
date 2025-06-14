package diplom.work.controllerservice.model;

import diplom.work.controllerservice.controller.PIDController;

import java.util.ArrayList;
import java.util.List;

/**
 * PID-контроллер с автотюнингом по методу Коэна–Куна прямо в потоке вызовов.
 * На начальном этапе контроллер работает в open-loop режиме:
 * 1) Ждет установившегося состояния при базовой мощности (basePower).
 * 2) Применяет ступеньку (basePower + stepChange) и собирает реакцию.
 * 3) После стабилизации вычисляет параметры PID и переходит в замкнутый режим.
 */
public class CohenCoonPidController {
    private enum Stage { WAIT_BEFORE_STEP, COLLECT_AFTER_STEP, CONTROL }

    private Stage stage = Stage.WAIT_BEFORE_STEP;

    private final double basePower;
    private final double stepChange;
    private final int stableWindow;
    private final double stabilityThreshold;

    private final List<Double> times = new ArrayList<>();
    private final List<Double> temps = new ArrayList<>();

    private double stepStartTime;

    // PID state
    private double Kp, Ki, Kd;
    private double setpoint;
    private double integral = 0;
    private double prevError = 0;

    /**
     * @param setpoint целевая температура
     * @param basePower базовая мощность перед ступенькой
     * @param stepChange изменение мощности для исследования
     * @param stableWindow число последних точек для проверки стабильности
     * @param stabilityThreshold допустимое отклонение для признания стационарного состояния
     */
    public CohenCoonPidController(double setpoint,
                                  double basePower,
                                  double stepChange,
                                  int stableWindow,
                                  double stabilityThreshold) {
        this.setpoint = setpoint;
        this.basePower = basePower;
        this.stepChange = stepChange;
        this.stableWindow = stableWindow;
        this.stabilityThreshold = stabilityThreshold;
    }

    public synchronized double computeOutput(double currentTemp, double dt, long timestamp) {
        double t = timestamp / 1000.0; // приводим к секундам
        switch (stage) {
            case WAIT_BEFORE_STEP:
                // Сбор данных до ступеньки
                times.add(t);
                temps.add(currentTemp);
                if (isStable(temps, stabilityThreshold, stableWindow)) {
                    // Переход к шагу
                    stage = Stage.COLLECT_AFTER_STEP;
                    stepStartTime = t;
                    times.clear();
                    temps.clear();
                    // Первая точка после ступеньки
                    times.add(0.0);
                    temps.add(currentTemp);
                    return basePower + stepChange;
                }
                return basePower;

            case COLLECT_AFTER_STEP:
                // Сбор данных после ступеньки
                times.add(t - stepStartTime);
                temps.add(currentTemp);
                if (isStable(temps, stabilityThreshold, stableWindow) && times.size() >= stableWindow + 1) {
                    // Вычисление PID коэффициентов
                    CohenCoonAutotuner tuner = new CohenCoonAutotuner(stepChange);
                    CohenCoonAutotuner.PIDParams p = tuner.tune(times, temps);
                    this.Kp = p.Kp;
                    this.Ki = p.Ki;
                    this.Kd = p.Kd;
                    // Сброс состояния PID
                    this.integral = 0;
                    this.prevError = setpoint - temps.get(temps.size() - 1);
                    stage = Stage.CONTROL;
                    // Вычисляем первый управляющий сигнал
                    double err = setpoint - currentTemp;
                    double output = Kp * err;
                    // сохранение для derivative
                    prevError = err;
                    return clampOutput(output);
                }
                return basePower + stepChange;

            case CONTROL:
                // Стандартный PID-регулятор
                double error = setpoint - currentTemp;
                integral += error * dt;
                double derivative = (error - prevError) / dt;
                prevError = error;
                double out = Kp * error + Ki * integral + Kd * derivative;
                return clampOutput(out);

            default:
                throw new IllegalStateException("Unknown stage: " + stage);
        }
    }

    /**
     * Проверяет, что последние stableWindow точек темп имеют изменение меньше threshold
     */
    private boolean isStable(List<Double> data, double threshold, int stableWindow) {
        int n = data.size();
        if (n < stableWindow) return false;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (int i = n - stableWindow; i < n; i++) {
            double v = data.get(i);
            if (v > max) max = v;
            if (v < min) min = v;
        }
        return (max - min) <= threshold;
    }

    /**
     * Ограничивает выход PID, если требуется (например, 0-100% или мощность в ваттах 0- max)
     */
    private double clampOutput(double out) {
        // Здесь можно задать ограничения по мощности, например:
        // return Math.max(0, Math.min(maxPower, out));
        return out;
    }
}
