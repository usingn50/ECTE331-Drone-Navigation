package ECTE331RoboticArm;

/**
 * Low-priority thread that acquires the MotorController and simulates work,
 * potentially causing priority inversion.
 */
public class LowPriorityTask extends Thread {
    private MotorController motorController;
    private volatile boolean holdingResource = false;

    public LowPriorityTask(MotorController controller) {
        this.motorController = controller;
        setName("LowPriorityTask");
        setPriority(Thread.MIN_PRIORITY); // Low Priority
    }

    @Override
    public void run() {
        System.out.println(System.currentTimeMillis() + " - " + getName() + ": Starting.");
        try {
            // Simulate some initial work
            Thread.sleep(50);

            System.out.println(System.currentTimeMillis() + " - " + getName() + ": Attempting to acquire MotorController.");
            motorController.acquireResource(getName());
            holdingResource = true;
            System.out.println(System.currentTimeMillis() + " - " + getName() + ": Acquired MotorController. Holding for a while.");

            // Simulate holding the resource for a long time
            Thread.sleep(500); 

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(System.currentTimeMillis() + " - " + getName() + ": Interrupted.");
        } finally {
            if (holdingResource) {
                motorController.releaseResource(getName());
                holdingResource = false;
                System.out.println(System.currentTimeMillis() + " - " + getName() + ": Released MotorController.");
            }
            System.out.println(System.currentTimeMillis() + " - " + getName() + ": Finished.");
        }
    }
}
