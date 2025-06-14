package diplom.work.controllerservice.util;

import java.util.ArrayList;
import java.util.List;

public class PeakDetector {

    public static List<Double> findPeakTimestamps(List<Double> values, List<Double> timestamps) {
        List<Double> peakTimes = new ArrayList<>();

        for (int i = 1; i < values.size() - 1; i++) {
            double prev = values.get(i - 1);
            double current = values.get(i);
            double next = values.get(i + 1);

            if (current > prev && current > next) {
                peakTimes.add(timestamps.get(i));
            }
        }
        return peakTimes;
    }

    public static double calculatePu(List<Double> values, List<Double> timestamps) {
        List<Double> peaks = findPeakTimestamps(values, timestamps);

        if (peaks.size() < 2) {
            throw new IllegalArgumentException("Недостаточно пиков для расчета периода!");
        }

        List<Double> periods = new ArrayList<>();
        for (int i = 0; i < peaks.size() - 1; i++) {
            periods.add(peaks.get(i + 1) - peaks.get(i));
        }

        double sum = 0;
        for (double p : periods) {
            sum += p;
        }

        return sum / periods.size();
    }
}

