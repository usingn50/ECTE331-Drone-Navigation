package ECTE331RoboticArm;

/**
 * Medium-priority thread that plans and sends movement commands to the motor controller.
 */
public class MotionPlanner extends Thread {
    private MotorController motorController;

    public MotionPlanner(MotorController controller) {
        this.motorController = controller;
        setName("MotionPlanner");
        setPriority(Thread.NORM_PRIORITY); // Medium Priority
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Simulate planning a new position
                int newPosition = (int) (Math.random() * 100);
                System.out.println(System.currentTimeMillis() + " - " + getName() + ": Planning to move to " + newPosition);
                motorController.moveArm(getName(), newPosition);
                Thread.sleep(100); // Simulate work after moving
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(System.currentTimeMillis() + " - " + getName() + ": Interrupted.");
            }
        }
    }
}
