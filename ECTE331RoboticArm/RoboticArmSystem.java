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

        // Start threads
        logger.start();
        safetyMonitor.start();
        motionPlanner.start();

        // Let the simulation run for a while
        try {
            Thread.sleep(5000); // Run for 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted.");
        }

        // Stop threads
        motionPlanner.interrupt();
        safetyMonitor.interrupt();
        logger.interrupt();

        System.out.println("--- Robotic Arm System Simulation Finished ---");
    }
}
