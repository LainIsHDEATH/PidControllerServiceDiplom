package diplom.work.controllerservice.model;

/** Любой «движок», который на каждом шаге возвращает требуемую мощность. */
public interface PowerController {
    /**
     * @param target    – уставка (целевая температура)
     * @param current   – текущая температура
     * @param dt        – шаг симуляции, с
     * @param timestamp – время (epoch-millis)
     */
    double compute(double target, double current, double dt, long timestamp);
}
