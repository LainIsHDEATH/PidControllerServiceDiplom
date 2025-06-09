package diplom.work.controllerservice.dto;

public record PIDConfigRequest(
        String roomName,
        double kp,
        double ki,
        double kd
) {}
