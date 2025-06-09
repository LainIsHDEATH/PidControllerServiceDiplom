package diplom.work.controllerservice.controller;

import diplom.work.controllerservice.dto.PIDConfigRequest;
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
    public PIDResponse compute(@RequestBody PIDRequest request) {
        double output = 0;
        try {
            output = pidService.calculateOutput(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new PIDResponse(request.roomName(), output);
    }

    @PutMapping("/config")
    public ResponseEntity<String> configurePID(@RequestBody PIDConfigRequest config) {
        pidService.setPIDConfig(config);
        return ResponseEntity.ok("PID config set for room: " + config.roomName());
    }

    @PostMapping("/start-tuning/{roomName}")
    public ResponseEntity<String> startTuning(@PathVariable String roomName) {
        pidService.startAutoTuning(roomName);
        return ResponseEntity.ok("Auto-tuning started for room: " + roomName);
    }

    @GetMapping("/status/{roomName}")
    public ResponseEntity<String> tuningStatus(@PathVariable String roomName) {
        boolean complete = pidService.isAutoTuningComplete(roomName);
        if (complete) {
            return ResponseEntity.ok("Tuning complete for room: " + roomName);
        } else {
            return ResponseEntity.ok("Tuning in progress for room: " + roomName);
        }
    }

    @PostMapping("/apply-tuning/{roomName}")
    public ResponseEntity<String> applyTuning(@PathVariable String roomName) {
        pidService.applyTunedPID(roomName);
        return ResponseEntity.ok("Tuned PID applied for room: " + roomName);
    }
}
