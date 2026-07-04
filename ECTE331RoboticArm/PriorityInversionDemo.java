package ECTE331RoboticArm;

/**
 * Runs a single controlled priority-inversion scenario (Tasks 3-5):
 * <ol>
 *   <li>The low-priority task acquires the shared resource and holds it.</li>
 *   <li>Shortly after, the medium-priority task starts unrelated CPU work
 *       that -- under {@link PriorityMode#NONE} -- delays the low-priority
 *       task's release of the resource.</li>
 *   <li>The high-priority task then attempts to access the resource and
 *       blocks until it is released.</li>
 * </ol>
 * The elapsed time the high-priority task spends waiting is printed and
 * returned; this is the key metric used to demonstrate (and later, in
 * {@link PerformanceEvaluator}, measure) priority inversion and the effect
 * of the two mitigation protocols.
 */
public class PriorityInversionDemo {

    public static long run(PriorityMode mode, EventLog log) throws InterruptedException {
        // Ceiling priority = highest priority among all tasks that use the resource.
        MotorController controller = new MotorController(mode, Thread.MAX_PRIORITY, log);

        log.log("===== Running scenario with mode=" + mode + " =====");

        LowPriorityTask low = new LowPriorityTask(controller, log, 300);
        MediumPriorityTask medium = new MediumPriorityTask(controller, log, 400);
        SafetyMonitor high = new SafetyMonitor(controller, log);

        low.start();
        Thread.sleep(50);   // let Low acquire the resource first
        medium.start();
        Thread.sleep(50);   // let Medium start interfering
        high.start();       // High now attempts to access the (held) resource

        low.join();
        medium.join();
        high.join();

        long waitMs = high.getLastWaitMs();
        log.log("Result: High-priority thread waited " + waitMs + " ms under mode=" + mode);
        return waitMs;
    }

    public static void main(String[] args) throws InterruptedException {
        EventLog log = new EventLog("robotic_arm_log.txt");
        try {
            for (PriorityMode mode : PriorityMode.values()) {
                run(mode, log);
                System.out.println();
            }
        } finally {
            log.close();
        }
    }
}
