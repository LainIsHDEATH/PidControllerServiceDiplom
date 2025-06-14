package diplom.work.controllerservice.service;

import diplom.work.controllerservice.dto.PIDConfigRequest;
import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PidConfigDTO;
import diplom.work.controllerservice.feign.StoragePidConfigClient;
import diplom.work.controllerservice.model.PIDState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PIDControllerService {

    private final Map<Long, PIDState> pidStates = new ConcurrentHashMap<>();
    private final StoragePidConfigClient storagePidConfigClient;
//    private final Map<Long, PIDAutoTunerService> autoTunerServices = new ConcurrentHashMap<>();

    public double calculateOutput(PIDRequest request) {
        PIDState pidState = pidStates.get(request.pidConfigId());
        System.out.println(pidState);
        if (pidState == null) {
            PidConfigDTO pidConfigDTO = storagePidConfigClient.getConfig(request.pidConfigId()).getBody();
            if (pidConfigDTO != null) {
                pidState = new PIDState(
                        pidConfigDTO.kp(),
                        pidConfigDTO.ki(),
                        pidConfigDTO.kd());
                pidStates.put(request.pidConfigId(), pidState);
            } else throw new RuntimeException("PID config not found");
        }
        System.out.println(
                "Kp: " + pidState.getKp() +
                        " Ki: " + pidState.getKi() +
                        " Kd: " + pidState.getKd() +
                        " integral: " + pidState.getIntegral() +
                        " previous error " + pidState.getPreviousError());

        return pidState.update(request.targetTemperature(), request.currentTemperature(), request.deltaTime(), 2000);
    }
}
