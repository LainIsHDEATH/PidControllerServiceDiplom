package diplom.work.controllerservice.dto;

public record PidConfigDTO(
        Double kp,
        Double ki,
        Double kd,
        String tunedMethod,
        Boolean active
) {
}
