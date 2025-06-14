package diplom.work.controllerservice.controller;

import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PIDResponse;
import diplom.work.controllerservice.service.PIDControllerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pid")
@RequiredArgsConstructor
public class PIDController {

    private final PIDControllerService pidService;

    /** Основной энд-поинт, на который регулярно стучится симулятор. */
    @PostMapping("/compute")
    public PIDResponse compute(@RequestBody PIDRequest request) {

        // ➋ вызов сервиса — он сам решит, нужен ли автотюн
        double power = pidService.calculate(request);

        return new PIDResponse(power);
    }
}
