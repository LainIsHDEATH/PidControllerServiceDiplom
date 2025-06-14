package diplom.work.controllerservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PIDRequest(
        Long pidConfigId,
        double targetTemperature,
        double currentTemperature,
        double deltaTime,
        LocalDateTime timestamp
) {}