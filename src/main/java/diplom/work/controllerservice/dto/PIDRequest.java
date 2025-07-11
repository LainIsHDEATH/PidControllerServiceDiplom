package diplom.work.controllerservice.dto;

public record PIDRequest(
        Long simulationId,
        Long pidConfigId,
        double targetTemperature,
        double currentTemperature,
        double deltaTime
) {}