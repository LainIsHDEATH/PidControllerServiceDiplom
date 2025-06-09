package diplom.work.controllerservice.service;

import diplom.work.controllerservice.dto.PIDConfigRequest;
import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.model.PIDState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PIDControllerService {

    private final Map<String, PIDState> pidStates = new ConcurrentHashMap<>();
    private final Map<String, PIDAutoTunerService> autoTunerServices = new ConcurrentHashMap<>();

    public double calculateOutput(PIDRequest request) {
        PIDState state = pidStates.get(request.roomName());
        PIDAutoTunerService tuner = autoTunerServices.get(request.roomName());

        if (tuner != null && !tuner.isTuningComplete()) {
            // Автотюнинг в процессе
            double kp = tuner.getCurrentKp();
            double error = request.targetTemperature() - request.currentTemperature();
            double outputPower = Math.max(0.0, kp * error);

            tuner.record(outputPower, request.deltaTime()); // Или timestamp, если он есть отдельно

            return outputPower;
        }

        if (state == null) {
            throw new IllegalStateException("PID config not set for room: " + request.roomName());
        }

        return state.update(request.targetTemperature(), request.currentTemperature(), request.deltaTime());
    }

    public void setPIDConfig(PIDConfigRequest config) {
        pidStates.put(config.roomName(), new PIDState(config.kp(), config.ki(), config.kd()));
    }

    public void startAutoTuning(String roomName) {
        PIDAutoTunerService tuner = new PIDAutoTunerService();
        autoTunerServices.put(roomName, tuner);
    }

    public boolean isAutoTuningComplete(String roomName) {
        PIDAutoTunerService tuner = autoTunerServices.get(roomName);
        return tuner != null && tuner.isTuningComplete();
    }

    public void applyTunedPID(String roomName) {
        PIDAutoTunerService tuner = autoTunerServices.get(roomName);
        if (tuner == null || !tuner.isTuningComplete()) {
            throw new IllegalStateException("Tuning is not complete for room: " + roomName);
        }
        PIDState tunedPID = tuner.getTunedPID();
        pidStates.put(roomName, tunedPID);

        autoTunerServices.remove(roomName); // Очистить после применения
    }
}
