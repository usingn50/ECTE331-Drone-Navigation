/**
 * Holds the six shared integer variables (A1, A2, A3, B1, B2, B3) used by
 * ThreadA and ThreadB, together with the {@link SyncEvent} objects that
 * enforce the execution dependencies shown in Figure 2.1 of the assignment:
 *
 * <pre>
 *   FuncA1 ---------------------\
 *                                 v
 *   FuncB1        FuncB2 (needs A1) ---\
 *                                        v
 *                  FuncA2 (needs B2) ---\
 *                                         v
 *                  FuncB3 (needs A2) ---\
 *                                         v
 *                  FuncA3 (needs B3)
 * </pre>
 *
 * Each event is signaled exactly once per iteration by the thread that
 * produces the corresponding value, and awaited by the thread that consumes it.
 */
public class SharedState {

    // Shared variables updated by FuncA1..FuncA3 and FuncB1..FuncB3
    public volatile long A1, A2, A3;
    public volatile long B1, B2, B3;

    // Events encoding the dependency edges of Figure 2.1
    public final SyncEvent a1Ready = new SyncEvent();
    public final SyncEvent b2Ready = new SyncEvent();
    public final SyncEvent a2Ready = new SyncEvent();
    public final SyncEvent b3Ready = new SyncEvent();

    /**
     * Resets all shared variables and events so this object can be reused
     * for another iteration of the simulation (see part d).
     */
    public void reset() {
        A1 = A2 = A3 = 0;
        B1 = B2 = B3 = 0;
        a1Ready.reset();
        b2Ready.reset();
        a2Ready.reset();
        b3Ready.reset();
    }
}
