package diplom.work.controllerservice.controller;

import diplom.work.controllerservice.dto.PIDRequest;
import diplom.work.controllerservice.dto.PIDResponse;
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
    public ResponseEntity<PIDResponse> compute(@RequestBody PIDRequest request) {
        System.out.println(request.toString());
        double outputPower = 0; // 0 - 100%
        try {
            outputPower = pidService.calculateOutput(request);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(new PIDResponse(outputPower));
    }
}
