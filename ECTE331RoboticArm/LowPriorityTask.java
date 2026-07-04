package ECTE331RoboticArm;

/**
 * Dedicated low-priority thread for the controlled priority-inversion
 * scenario (Tasks 3-6). Acquires the shared resource and holds it for a
 * fixed duration, representing the classic "low-priority task holding a
 * resource needed by a high-priority task" setup.
 */
public class LowPriorityTask extends Thread {

    private final MotorController controller;
    private final EventLog log;
    private final long holdMillis;

    public LowPriorityTask(MotorController controller, EventLog log, long holdMillis) {
        this.controller = controller;
        this.log = log;
        this.holdMillis = holdMillis;
        setName("LowPriorityTask(Low)");
        setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        try {
            log.log(getName() + " attempting to acquire MotorController");
            controller.access(getName(), 42, holdMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
