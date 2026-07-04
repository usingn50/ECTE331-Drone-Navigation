package ECTE331RoboticArm;

/**
 * Low-priority real-time thread used in the basic multi-threaded demo
 * (Tasks 1-2). Periodically records the arm's current position, occasionally
 * accessing the shared {@link MotorController} to satisfy the requirement
 * that all real-time threads use the shared resource.
 */
public class Logger extends Thread {

    private final MotorController controller;
    private final EventLog log;
    private volatile boolean running = true;

    public Logger(MotorController controller, EventLog log) {
        this.controller = controller;
        this.log = log;
        setName("Logger(Low)");
        setPriority(Thread.MIN_PRIORITY);
    }

    public void stopRunning() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                log.log(getName() + " heartbeat, last known position=" + controller.getCurrentPosition());
                controller.access(getName(), controller.getCurrentPosition(), 20);
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
