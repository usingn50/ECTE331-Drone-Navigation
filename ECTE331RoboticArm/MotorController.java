package ECTE331RoboticArm;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.HashMap;
import java.util.Map;



/**
 * Shared resource representing the robotic arm's motor controller.
 * Only one thread can access this resource at a time.
 */
public class MotorController {
    private int currentPosition = 0;
    private Thread owner = null;
    private int originalOwnerPriority = -1;
    private Map<Thread, Integer> threadOriginalPriorities = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    private int ceilingPriority = Thread.MAX_PRIORITY; // Default to highest possible priority


    public void moveArm(String threadName, int newPosition) {
        lock.lock();
        try {
            Thread currentThread = Thread.currentThread();
            if (owner == null) {
                owner = currentThread;
                originalOwnerPriority = currentThread.getPriority();
                threadOriginalPriorities.put(currentThread, originalOwnerPriority);
            }

            System.out.println(System.currentTimeMillis() + " - " + threadName + " acquired MotorController. Current Position: " + currentPosition);
            // Simulate arm movement
            Thread.sleep(50); 
            currentPosition = newPosition;
            System.out.println(System.currentTimeMillis() + " - " + threadName + " moved arm to: " + currentPosition);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(threadName + " interrupted while moving arm.");
        } finally {
            Thread currentThread = Thread.currentThread();
            if (owner == currentThread) {
                owner = null;
                currentThread.setPriority(threadOriginalPriorities.getOrDefault(currentThread, Thread.NORM_PRIORITY));
                threadOriginalPriorities.remove(currentThread);
            }
            lock.unlock();
            System.out.println(System.currentTimeMillis() + " - " + threadName + " released MotorController.");
        }
    }

    public void acquireResource(String threadName) {
        lock.lock();
        try {
            Thread currentThread = Thread.currentThread();
            if (owner == null) {
                owner = currentThread;
                originalOwnerPriority = currentThread.getPriority();
                threadOriginalPriorities.put(currentThread, originalOwnerPriority);
            }
            System.out.println(System.currentTimeMillis() + " - " + threadName + ": Acquired MotorController resource.");
        } finally {
            // Lock is released by releaseResource
        }
    }

    public void releaseResource(String threadName) {
        Thread currentThread = Thread.currentThread();
        if (owner == currentThread) {
            owner = null;
            currentThread.setPriority(threadOriginalPriorities.getOrDefault(currentThread, Thread.NORM_PRIORITY));
            threadOriginalPriorities.remove(currentThread);
        }
        lock.unlock();
        System.out.println(System.currentTimeMillis() + " - " + threadName + ": Released MotorController resource.");
    }

    public synchronized void requestResource(Thread requestingThread) {
        if (owner != null && owner.getPriority() < requestingThread.getPriority()) {
            System.out.println(System.currentTimeMillis() + " - " + requestingThread.getName() + " requests resource. Boosting priority of " + owner.getName() + " from " + owner.getPriority() + " to " + requestingThread.getPriority());
            owner.setPriority(requestingThread.getPriority());
        }
    }

    public synchronized void setCeilingPriority(int priority) {
        this.ceilingPriority = priority;
        System.out.println(System.currentTimeMillis() + " - MotorController: Ceiling priority set to " + this.ceilingPriority);
    }

    public synchronized int getCeilingPriority() {
        return this.ceilingPriority;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public Thread getOwner() {
        return owner;
    }

    public boolean canAcquire(Thread requestingThread) {
        if (owner == null) {
            return true;
        }
        // If the requesting thread's priority is higher than or equal to the ceiling, it can acquire.
        // Or if the requesting thread is the current owner.
        return requestingThread.equals(owner) || requestingThread.getPriority() >= ceilingPriority;
    }
}
