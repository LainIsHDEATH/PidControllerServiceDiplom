package diplom.work.controllerservice.service;

import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PidConfigDTO;
import diplom.work.controllerservice.feign.StoragePidConfigClient;
import diplom.work.controllerservice.model.PIDState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PIDControllerService {

    private final Map<Long, PIDState> pidStates = new ConcurrentHashMap<>();
    private final StoragePidConfigClient storagePidConfigClient;

    public double calculateOutput(PIDRequest request) {
        PIDState pidState = pidStates.get(request.pidConfigId());
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
        log.info("Kp: {} Ki: {} Kd: {} integral: {} previous error {}",
                pidState.getKp(), pidState.getKi(), pidState.getKd(), pidState.getIntegral(), pidState.getPreviousError());

        return pidState.update(request.targetTemperature(), request.currentTemperature(), request.deltaTime());
    }
}
