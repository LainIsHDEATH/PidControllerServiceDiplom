package diplom.work.controllerservice.model;

public class FixedPidController implements PowerController {

    private final PIDState state;

    public FixedPidController(double kp, double ki, double kd) {
        this.state = new PIDState(kp, ki, kd);
    }

    @Override
    public double compute(double target, double current, double dt, long ts) {
        return state.update(target, current, dt, 2_000); // 2 кВт как пример лимита
    }
}
