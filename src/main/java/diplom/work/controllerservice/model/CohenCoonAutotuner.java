package diplom.work.controllerservice.model;

import java.util.List;

/**
 * Автотюнер PID-контроллера по методу Коэна–Куна
 */
public class CohenCoonAutotuner {
    /**
     * Результат автотюнинга: коэффициенты PID
     */
    public static class PIDParams {
        public final double Kp;
        public final double Ki;
        public final double Kd;

        public PIDParams(double Kp, double Ki, double Kd) {
            this.Kp = Kp;
            this.Ki = Ki;
            this.Kd = Kd;
        }
    }

    private final double stepChange; // величина ступеньки управляющего воздействия (ΔU)

    /**
     * @param stepChange разовое изменение мощности (в ваттах), подаваемое для исследования переходного процесса
     */
    public CohenCoonAutotuner(double stepChange) {
        if (stepChange == 0) throw new IllegalArgumentException("stepChange must be non-zero");
        this.stepChange = stepChange;
    }

    /**
     * Основной метод автотюнинга: вычисляет коэффициенты PID по массивам времени и температуры.
     * @param times список временных отметок (в секундах или миллисекундах, в одном масштабе)
     * @param temps список измеренных температур, соответствующих times
     * @return PIDParams с рассчитанными Kp, Ki и Kd
     */
    public PIDParams tune(List<Double> times, List<Double> temps) {
        if (times == null || temps == null || times.size() != temps.size() || times.size() < 2) {
            throw new IllegalArgumentException("times and temps must be non-null, same length ≥2");
        }

        // начальное значение температуры (Y0)
        double initialY = temps.get(0);
        // оценка установившегося значения как среднее последних 5 точек
        int n = temps.size();
        int lastCount = Math.min(5, n);
        double sum = 0;
        for (int i = n - lastCount; i < n; i++) {
            sum += temps.get(i);
        }
        double finalY = sum / lastCount;
        double deltaY = finalY - initialY;
        if (deltaY == 0) {
            throw new IllegalArgumentException("No change in output detected for step input");
        }

        // 1) Определяем время запаздывания L как время достижения 5% ΔY
        double threshold5 = initialY + 0.05 * deltaY;
        double deadTime = interpolateTime(times, temps, threshold5);

        // 2) Определяем постоянную времени T: время достижения 63.2% ΔY минус deadTime
        double threshold632 = initialY + 0.632 * deltaY;
        double t632 = interpolateTime(times, temps, threshold632);
        double timeConstant = t632 - deadTime;
        if (timeConstant <= 0) {
            throw new IllegalArgumentException("Computed non-positive time constant");
        }

        // 3) Процессный коэффициент Kpr = ΔY / ΔU
        double Kpr = deltaY / stepChange;

        // 4) Вычисляем отношение R = L / T
        double R = deadTime / timeConstant;

        // 5) Формулы Коэна–Куна для PID
        double Kp = (1.0 / Kpr) * (timeConstant / deadTime + 0.333);
        double Ti = timeConstant * (30 + 3 * R) / (9 + 20 * R);
        double Td = (timeConstant * R) / (11 + 2 * R);
        double Ki = Kp / Ti;
        double Kd = Kp * Td;

        return new PIDParams(Kp, Ki, Kd);
    }

    /**
     * Линейная интерполяция для нахождения времени, когда сигнал впервые пересекает порог threshold.
     * Если точное значение не найдено, возвращает ближайшее большее время.
     * @param times список временных отметок
     * @param temps список значений выходного сигнала
     * @param threshold значение, при пересечении которого ищем время
     * @return interpolated time
     */
    private double interpolateTime(List<Double> times, List<Double> temps, double threshold) {
        for (int i = 0; i < temps.size() - 1; i++) {
            double y0 = temps.get(i);
            double y1 = temps.get(i + 1);
            if ((y0 < threshold && y1 >= threshold) || (y0 > threshold && y1 <= threshold)) {
                double t0 = times.get(i);
                double t1 = times.get(i + 1);
                // линейная интерполяция
                double frac = (threshold - y0) / (y1 - y0);
                return t0 + frac * (t1 - t0);
            }
        }
        // если порог не пересечён, возвращаем последнее время
        return times.get(times.size() - 1);
    }
}

