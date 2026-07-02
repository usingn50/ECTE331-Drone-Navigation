package ECTE331RoboticArm;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared resource representing the robotic arm's motor controller.
 * Only one thread can access this resource at a time.
 */
public class MotorController {
    private int currentPosition = 0;
    private final Lock lock = new ReentrantLock();

    public void moveArm(String threadName, int newPosition) {
        lock.lock();
        try {
            System.out.println(System.currentTimeMillis() + " - " + threadName + " acquired MotorController. Current Position: " + currentPosition);
            // Simulate arm movement
            Thread.sleep(50); 
            currentPosition = newPosition;
            System.out.println(System.currentTimeMillis() + " - " + threadName + " moved arm to: " + currentPosition);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(threadName + " interrupted while moving arm.");
        } finally {
            lock.unlock();
            System.out.println(System.currentTimeMillis() + " - " + threadName + " released MotorController.");
        }
    }

    public int getCurrentPosition() {
        return currentPosition;
    }
}
