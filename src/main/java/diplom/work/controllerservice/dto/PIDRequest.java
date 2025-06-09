package diplom.work.controllerservice.dto;

public record PIDRequest(
        String roomName,
        double targetTemperature,
        double currentTemperature,
        double deltaTime
) {}