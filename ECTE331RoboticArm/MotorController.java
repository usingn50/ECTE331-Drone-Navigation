package ECTE331RoboticArm;



/**
 * Shared resource representing the robotic arm's motor controller.
 * Only one thread can access this resource at a time.
 */
public class MotorController {
    private int currentPosition = 0;


    public synchronized void moveArm(String threadName, int newPosition) {
        try {
            System.out.println(System.currentTimeMillis() + " - " + threadName + " acquired MotorController. Current Position: " + currentPosition);
            // Simulate arm movement
            Thread.sleep(50); 
            currentPosition = newPosition;
            System.out.println(System.currentTimeMillis() + " - " + threadName + " moved arm to: " + currentPosition);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(threadName + " interrupted while moving arm.");
        }
        System.out.println(System.currentTimeMillis() + " - " + threadName + " released MotorController.");
    }

    public synchronized void acquireResource(String threadName) {
        System.out.println(System.currentTimeMillis() + " - " + threadName + ": Acquired MotorController resource.");
    }

    public synchronized void releaseResource(String threadName) {
        System.out.println(System.currentTimeMillis() + " - " + threadName + ": Released MotorController resource.");
    }

    public int getCurrentPosition() {
        return currentPosition;
    }
}
