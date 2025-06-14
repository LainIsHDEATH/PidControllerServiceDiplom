package diplom.work.controllerservice.controller;

import diplom.work.controllerservice.dto.PIDConfigRequest;
import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PIDResponse;
import diplom.work.controllerservice.model.CohenCoonPidController;
import diplom.work.controllerservice.service.PIDControllerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pid")
@RequiredArgsConstructor
public class PIDController {

    private final PIDControllerService pidService;

    @PostMapping("/compute")
    public PIDResponse compute(@RequestBody PIDRequest request) {
        System.out.println(request.toString());
        double outputPower = 0;
        try {
            outputPower = pidService.calculateOutput(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new PIDResponse(outputPower);
    }

    // Активный контроллер (внедряется или переключается через другой эндпоинт)
    private CohenCoonPidController activeController;

    @PostMapping("/control")
    public ResponseEntity<PIDResponse> handleControl(@RequestBody PIDRequest req) {
        double currentTemp = req.currentTemperature();
        double dt = req.deltaTime();
        long time = req.;
        // Вычисление управления через активный контроллер
        double power = activeController.computeOutput(currentTemp, dt, time);
        PIDResponse resp = new PIDResponse(power);
        return ResponseEntity.ok(resp);
    }

    // Эндпоинт для выбора стратегии автотюнинга
    @PostMapping("/autotune")
    public ResponseEntity<String> selectAutotuneStrategy(@RequestParam String strategy) {
        if ("cohen-coon".equalsIgnoreCase(strategy)) {
            activeController = new CohenCoonPidController(defaultSetpoint, defaultStepPower);
        } else {
            return ResponseEntity.badRequest().body("Unknown strategy");
        }
        return ResponseEntity.ok("Strategy set to " + strategy);
    }
}
