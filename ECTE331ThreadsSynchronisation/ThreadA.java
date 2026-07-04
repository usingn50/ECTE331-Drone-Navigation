/**
 * Represents user thread A, executing FuncA1, FuncA2, FuncA3 in that order.
 *
 * <p>Dependency handling (see Figure 2.1):</p>
 * <ul>
 *   <li>FuncA1 has no dependency and can run immediately.</li>
 *   <li>FuncA2 needs B2, so it must wait until ThreadB signals {@code b2Ready}.</li>
 *   <li>FuncA3 needs B3, so it must wait until ThreadB signals {@code b3Ready}.</li>
 * </ul>
 */
public class ThreadA extends Thread {

    private final SharedState state;

    public ThreadA(SharedState state) {
        super("ThreadA");
        this.state = state;
    }

    @Override
    public void run() {
        try {
            // FuncA1: A1 = sum(0..500) -- no dependency
            state.A1 = SumUtil.sum(500);
            state.a1Ready.signal(); // Let ThreadB know A1 is ready (needed for FuncB2)

            // FuncA2: A2 = B2 + sum(0..300) -- needs B2 from ThreadB (FuncB2)
            state.b2Ready.await();
            state.A2 = state.B2 + SumUtil.sum(300);
            state.a2Ready.signal(); // Let ThreadB know A2 is ready (needed for FuncB3)

            // FuncA3: A3 = B3 + sum(0..400) -- needs B3 from ThreadB (FuncB3)
            state.b3Ready.await();
            state.A3 = state.B3 + SumUtil.sum(400);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
