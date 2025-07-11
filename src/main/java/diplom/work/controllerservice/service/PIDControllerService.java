package diplom.work.controllerservice.service;

import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PidConfigDTO;
import diplom.work.controllerservice.feign.StoragePidConfigClient;
import diplom.work.controllerservice.model.PidImpl.MiniPID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PIDControllerService {

    private final Map<Long, MiniPID> pidStates = new ConcurrentHashMap<>();
    private final StoragePidConfigClient storagePidConfigClient;

    public double calculateOutput(PIDRequest request) {
        MiniPID pidState = pidStates.get(request.simulationId());
        if (pidState == null) {
            PidConfigDTO pidConfigDTO = storagePidConfigClient.getConfig(request.pidConfigId()).getBody();
            if (pidConfigDTO != null) {
                pidState = new MiniPID(pidConfigDTO.kp(), pidConfigDTO.ki(), pidConfigDTO.kd(), request.deltaTime());
                pidState.setOutputLimits(0.0, 100.0);
                pidState.setMaxIOutput(30.0);
                pidStates.put(request.simulationId(), pidState);
            } else throw new RuntimeException("PID config not found");
        }
        log.info("Kp: {} Ki: {} Kd: {}", pidState.getP(), pidState.getI(), pidState.getD());

        return pidState.getOutput(request.currentTemperature(), request.targetTemperature(), request.deltaTime());
    }
}
