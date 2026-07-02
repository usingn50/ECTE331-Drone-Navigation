package ECTE331RoboticArm;

/**
 * High-priority thread that monitors for emergency conditions.
 */
public class SafetyMonitor extends Thread {
    private MotorController motorController;
    private volatile boolean emergencyDetected = false;

    public SafetyMonitor(MotorController controller) {
        this.motorController = controller;
        setName("SafetyMonitor");
        setPriority(Thread.MAX_PRIORITY); // High Priority
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Simulate monitoring for emergency conditions
                Thread.sleep(10);
                if (emergencyDetected) {
                    System.out.println(System.currentTimeMillis() + " - " + getName() + ": EMERGENCY DETECTED! Stopping arm.");
                    System.out.println(System.currentTimeMillis() + " - " + getName() + ": Attempting to acquire MotorController to stop arm.");
                    motorController.requestResource(Thread.currentThread()); // Request resource to trigger priority inheritance
                    motorController.moveArm(getName(), motorController.getCurrentPosition()); // Stop arm at current position
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(System.currentTimeMillis() + " - " + getName() + ": Interrupted.");
            }
        }
    }

    public void triggerEmergency() {
        this.emergencyDetected = true;
    }
}
