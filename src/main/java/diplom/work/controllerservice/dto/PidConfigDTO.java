package diplom.work.controllerservice.dto;

public record PidConfigDTO(
        Long   id,
        Double kp,
        Double ki,
        Double kd,
        String tunedMethod
) {
}
