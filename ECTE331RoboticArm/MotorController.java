package ECTE331RoboticArm;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared resource representing the robotic arm's motor. Only one thread may
 * hold it at a time (critical section), which is enforced with a
 * {@link ReentrantLock} (Task 2).
 *
 * <p>The controller can operate in three modes (see {@link PriorityMode}),
 * selected at construction time:</p>
 * <ul>
 *   <li>{@code NONE} — plain mutual exclusion, no priority management
 *       (Task 1/2/3: priority inversion can occur).</li>
 *   <li>{@code INHERITANCE} — when a higher-priority thread blocks waiting
 *       for the resource, the current holder's priority is temporarily
 *       raised to match it (Task 4). Restored on release.</li>
 *   <li>{@code CEILING} — the holder's priority is raised to the resource's
 *       ceiling priority for the entire duration it holds the resource
 *       (Task 5), regardless of whether any thread is currently waiting.</li>
 * </ul>
 *
 * <p><b>Simulating "medium-priority interference":</b> Java thread priorities
 * are only a scheduling <i>hint</i> to the OS/JVM and are not guaranteed to
 * preempt threads deterministically on general-purpose (non real-time)
 * operating systems. To reliably and reproducibly demonstrate priority
 * inversion (Task 3) regardless of the machine this code runs on, the holder's
 * critical section explicitly checks the {@link #mediumInterfering} flag: if
 * an unrelated medium-priority task is currently executing <b>and</b> the
 * holder's own priority has not been raised above medium priority, the
 * critical section is extended for as long as the interference lasts. This
 * models the classic priority-inversion scenario (a low-priority holder being
 * starved of CPU time by an unrelated medium-priority thread) in a way that is
 * deterministic and independent of the underlying scheduler. Both
 * {@code INHERITANCE} and {@code CEILING} modes raise the holder's priority
 * above medium priority, so the check no longer applies and the extension
 * never happens — which is exactly the protection those protocols are meant
 * to provide.</p>
 */
public class MotorController {

    private final PriorityMode mode;
    private final int ceilingPriority;
    private final EventLog log;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean mediumInterfering = new AtomicBoolean(false);

    private volatile Thread owner = null;
    private int currentPosition = 0;

    public MotorController(PriorityMode mode, int ceilingPriority, EventLog log) {
        this.mode = mode;
        this.ceilingPriority = ceilingPriority;
        this.log = log;
    }

    /** Called by the medium-priority interference task to mark that it is running. */
    public void setMediumInterfering(boolean value) {
        mediumInterfering.set(value);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Acquires the resource, holds it for {@code workMillis} (simulating
     * the motor movement), then releases it. Returns the time (ms) the
     * calling thread spent waiting to acquire the lock, which is the metric
     * used to detect and measure priority inversion.
     */
    public long access(String taskName, int newPosition, long workMillis) throws InterruptedException {
        Thread current = Thread.currentThread();
        long requestTime = System.nanoTime();

        // --- Priority inheritance: boost the current holder if it has lower priority ---
        if (mode == PriorityMode.INHERITANCE) {
            Thread holder = owner;
            if (holder != null && holder.getPriority() < current.getPriority()) {
                int old = holder.getPriority();
                holder.setPriority(current.getPriority());
                log.log(taskName + " is blocked -> inherits priority to " + holder.getName()
                        + " (" + old + " -> " + current.getPriority() + ")");
            }
        }

        lock.lock();
        long waitMs = (System.nanoTime() - requestTime) / 1_000_000;
        int originalPriority = current.getPriority();
        try {
            owner = current;
            if (mode == PriorityMode.CEILING) {
                current.setPriority(ceilingPriority);
                log.log(taskName + " acquired resource, raised to ceiling priority " + ceilingPriority);
            } else {
                log.log(taskName + " acquired resource (waited " + waitMs + " ms)");
            }

            long elapsed = 0;
            final long step = 10;
            while (elapsed < workMillis) {
                Thread.sleep(step);
                // Only count this tick toward completion if we are not currently
                // being "interfered with" at an unprotected (non-boosted) priority.
                boolean unprotected = current.getPriority() <= Thread.NORM_PRIORITY;
                if (mediumInterfering.get() && unprotected) {
                    // Being starved by the medium-priority task: no progress this tick.
                    continue;
                }
                elapsed += step;
            }

            currentPosition = newPosition;
            log.log(taskName + " finished work, arm now at " + currentPosition);
        } finally {
            if (mode == PriorityMode.CEILING || mode == PriorityMode.INHERITANCE) {
                current.setPriority(originalPriority);
            }
            owner = null;
            log.log(taskName + " released resource");
            lock.unlock();
        }
        return waitMs;
    }
}
