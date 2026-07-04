/**
 * A simple one-shot synchronization event, similar in spirit to a latch.
 *
 * <p>One thread calls {@link #signal()} once a dependency has been satisfied;
 * any other thread waiting on {@link #await()} is released at that point.
 * The implementation relies purely on the intrinsic monitor of this object
 * (the {@code synchronized} keyword together with {@code wait()}/{@code notifyAll()}),
 * so it never busy-waits and never uses {@code Thread.sleep()}.</p>
 *
 * <p>The event is reusable across iterations via {@link #reset()}, which is
 * used when running the simulation for a large number of iterations
 * (part d of the assignment).</p>
 */
public class SyncEvent {

    private boolean signaled = false;

    /**
     * Blocks the calling thread until {@link #signal()} has been called.
     * Uses a {@code while} loop around {@code wait()} to correctly handle
     * spurious wakeups, as recommended practice in Java.
     *
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public synchronized void await() throws InterruptedException {
        while (!signaled) {
            wait();
        }
    }

    /**
     * Marks this event as satisfied and wakes up any thread(s) blocked in
     * {@link #await()}.
     */
    public synchronized void signal() {
        signaled = true;
        notifyAll();
    }

    /**
     * Resets the event back to the "not signaled" state so that it can be
     * reused in a subsequent iteration of the simulation.
     */
    public synchronized void reset() {
        signaled = false;
    }
}
