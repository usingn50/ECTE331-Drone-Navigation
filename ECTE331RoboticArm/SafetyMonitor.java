package ECTE331RoboticArm;

/**
 * High-priority real-time thread. In the basic demo (Tasks 1-2) it
 * periodically checks for an emergency and stops the arm. In the
 * priority-inversion demo (Tasks 3-6) a single instance is used to measure
 * how long a high-priority thread must wait to access the shared resource.
 */
public class SafetyMonitor extends Thread {

    private final MotorController controller;
    private final EventLog log;
    private volatile long lastWaitMs = -1;

    public SafetyMonitor(MotorController controller, EventLog log) {
        this.controller = controller;
        this.log = log;
        setName("SafetyMonitor(High)");
        setPriority(Thread.MAX_PRIORITY);
    }

    /** The measured waiting time (ms) of the most recent resource access. */
    public long getLastWaitMs() {
        return lastWaitMs;
    }

    @Override
    public void run() {
        try {
            log.log(getName() + " requesting emergency stop access to MotorController");
            lastWaitMs = controller.access(getName(), 0, 50);
            log.log(getName() + " completed emergency stop (waited " + lastWaitMs + " ms)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
