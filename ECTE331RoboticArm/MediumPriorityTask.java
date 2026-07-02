package ECTE331RoboticArm;

/**
 * Medium-priority thread that simulates work and can be affected by priority inversion.
 */
public class MediumPriorityTask extends Thread {

    public MediumPriorityTask() {
        setName("MediumPriorityTask");
        setPriority(Thread.NORM_PRIORITY); // Medium Priority
    }

    @Override
    public void run() {
        System.out.println(System.currentTimeMillis() + " - " + getName() + ": Starting and performing work.");
        try {
            // Simulate some CPU-bound work
            Thread.sleep(300); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(System.currentTimeMillis() + " - " + getName() + ": Interrupted.");
        }
        System.out.println(System.currentTimeMillis() + " - " + getName() + ": Finished work.");
    }
}
