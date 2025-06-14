package diplom.work.controllerservice.dto;

public record PIDRequest(
        Long pidConfigId,
        double targetTemperature,
        double currentTemperature,
        double deltaTime
) {}