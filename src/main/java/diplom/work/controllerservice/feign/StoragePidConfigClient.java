package diplom.work.controllerservice.feign;

import diplom.work.controllerservice.dto.PidConfigDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "storage-service", url = "${feign.client.config.storage-service.url}")
public interface StoragePidConfigClient {

    @GetMapping("/api/pid-configs/{id}")
    ResponseEntity<PidConfigDTO> getConfig(@PathVariable Long id);
}
