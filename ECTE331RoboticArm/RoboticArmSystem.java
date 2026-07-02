package ECTE331RoboticArm;

/**
 * Main class to simulate the Robotic Arm Control System.
 * Demonstrates basic multi-threaded implementation with shared resource.
 */
public class RoboticArmSystem {

    public static void main(String[] args) {
        System.out.println("--- Robotic Arm System Simulation Started ---");

        MotorController motorController = new MotorController();
        Logger logger = new Logger(motorController);
        SafetyMonitor safetyMonitor = new SafetyMonitor(motorController);
        MotionPlanner motionPlanner = new MotionPlanner(motorController);
        LowPriorityTask lowPriorityTask = new LowPriorityTask(motorController);
        // Set a ceiling priority for the resource, for example, NORM_PRIORITY + 1
        motorController.setCeilingPriority(Thread.NORM_PRIORITY + 1);
        MediumPriorityTask mediumPriorityTask = new MediumPriorityTask();

        // Start threads
        logger.start();
        safetyMonitor.start();
        motionPlanner.start();
        lowPriorityTask.start();

        // Simulate priority inversion scenario
        try {
            Thread.sleep(100); // Allow low priority task to acquire resource
            mediumPriorityTask.start(); // Medium priority task starts
            Thread.sleep(200); // Allow medium priority task to run and preempt low priority
            // In Priority Ceiling, the low priority task's priority is boosted to the ceiling priority
            // of the resource it holds, preventing medium priority tasks from preempting it.
            // The SafetyMonitor (high priority) will still request the resource.
            safetyMonitor.triggerEmergency(); // High priority task tries to acquire resource
            Thread.sleep(2000); // Wait for simulation to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted.");
        }

        // Stop threads
        motionPlanner.interrupt();
        safetyMonitor.interrupt();
        logger.interrupt();
        lowPriorityTask.interrupt();
        mediumPriorityTask.interrupt();

        System.out.println("--- Robotic Arm System Simulation Finished ---");
    }
}
