package diplom.work.controllerservice.service;

import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PidConfigDTO;
import diplom.work.controllerservice.feign.StoragePidConfigClient;
import diplom.work.controllerservice.model.CohenCoonPidController;
import diplom.work.controllerservice.model.FixedPidController;
import diplom.work.controllerservice.model.PowerController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service @RequiredArgsConstructor
public class PIDControllerService {
    private static final Logger log= LoggerFactory.getLogger(PIDControllerService.class);
    private final StoragePidConfigClient storage;
    private final Map<Long,PowerController> controllers=new ConcurrentHashMap<>();

    public double calculate(PIDRequest req){
        PowerController pc = controllers.computeIfAbsent(req.pidConfigId(), id->create(id,req));
        double out;
        try{
            long millis=req.timestamp().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            out=pc.compute(req.targetTemperature(),req.currentTemperature(),req.deltaTime(),millis);
        }catch(IllegalStateException ex){ log.warn("Autotune warming: {}",ex.getMessage()); return 0; }
        if(pc instanceof CohenCoonPidController c && c.isTuned()){
            PidConfigDTO dto=new PidConfigDTO(c.getKp(),c.getKi(),c.getKd(),"COHEN_COON_AUTO",true);
            storage.updateConfig(req.pidConfigId(),dto);
            controllers.put(req.pidConfigId(),new FixedPidController(c.getKp(),c.getKi(),c.getKd()));
        }
        return out; }

    private PowerController create(Long id, PIDRequest req){
        PidConfigDTO cfg= Objects.requireNonNull(storage.getConfig(id).getBody());
        boolean empty=cfg.kp()==0&&cfg.ki()==0&&cfg.kd()==0;
        if(empty){
            log.info("PID config {} empty – starting Cohen‑Coon autotune",id);
            return new CohenCoonPidController(req.targetTemperature(),100,1000,10,0.2);
        }
        return new FixedPidController(cfg.kp(),cfg.ki(),cfg.kd());
    }
}
