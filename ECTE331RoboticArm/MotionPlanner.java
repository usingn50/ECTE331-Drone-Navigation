package ECTE331RoboticArm;

/**
 * Medium-priority real-time thread used in the basic multi-threaded demo
 * (Tasks 1-2). Repeatedly plans a new arm position and sends it to the
 * shared {@link MotorController}.
 */
public class MotionPlanner extends Thread {

    private final MotorController controller;
    private final EventLog log;
    private volatile boolean running = true;

    public MotionPlanner(MotorController controller, EventLog log) {
        this.controller = controller;
        this.log = log;
        setName("MotionPlanner(Medium)");
        setPriority(Thread.NORM_PRIORITY);
    }

    public void stopRunning() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        int position = 0;
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                position = (position + 10) % 100;
                controller.access(getName(), position, 60);
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
