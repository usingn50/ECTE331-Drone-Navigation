package ECTE331RoboticArm;

/**
 * Tasks 1-2: basic multi-threaded implementation. Three real-time threads
 * (SafetyMonitor-High, MotionPlanner-Medium, Logger-Low) repeatedly access
 * the shared {@link MotorController}, which enforces mutual exclusion.
 * No priority management is applied here -- see {@link PriorityInversionDemo}
 * and {@link PerformanceEvaluator} for Tasks 3-6.
 */
public class RoboticArmSystem {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Robotic Arm System: basic demo (Tasks 1-2) ---");

        EventLog log = new EventLog("robotic_arm_log.txt");
        MotorController controller = new MotorController(PriorityMode.NONE, Thread.MAX_PRIORITY, log);

        MotionPlanner motionPlanner = new MotionPlanner(controller, log);
        Logger logger = new Logger(controller, log);

        motionPlanner.start();
        logger.start();

        Thread.sleep(2000); // let the system run for a few seconds

        motionPlanner.stopRunning();
        logger.stopRunning();
        motionPlanner.join();
        logger.join();
        log.close();

        System.out.println("--- Robotic Arm System: basic demo finished ---");
    }
}
