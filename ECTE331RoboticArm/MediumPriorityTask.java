package ECTE331RoboticArm;

/**
 * Dedicated medium-priority thread for the controlled priority-inversion
 * scenario (Tasks 3-6). Deliberately does <b>not</b> touch the shared
 * resource -- it represents unrelated medium-priority work that, in a
 * system without priority management, is free to run ahead of a
 * lower-priority resource holder and thereby delay it, indirectly starving
 * any high-priority task that is waiting on that resource.
 *
 * <p>See {@link MotorController} for how this interference is modelled
 * deterministically via {@link MotorController#setMediumInterfering}.</p>
 */
public class MediumPriorityTask extends Thread {

    private final MotorController controller;
    private final EventLog log;
    private final long durationMillis;

    public MediumPriorityTask(MotorController controller, EventLog log, long durationMillis) {
        this.controller = controller;
        this.log = log;
        this.durationMillis = durationMillis;
        setName("MediumPriorityTask(Medium)");
        setPriority(Thread.NORM_PRIORITY);
    }

    @Override
    public void run() {
        log.log(getName() + " starting unrelated CPU-bound work (does not use MotorController)");
        controller.setMediumInterfering(true);
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            controller.setMediumInterfering(false);
            log.log(getName() + " finished");
        }
    }
}
