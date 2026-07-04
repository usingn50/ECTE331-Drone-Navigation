/**
 * Represents user thread B, executing FuncB1, FuncB2, FuncB3 in that order.
 *
 * <p>Dependency handling (see Figure 2.1):</p>
 * <ul>
 *   <li>FuncB1 has no dependency and can run immediately.</li>
 *   <li>FuncB2 needs A1, so it must wait until ThreadA signals {@code a1Ready}.</li>
 *   <li>FuncB3 needs A2, so it must wait until ThreadA signals {@code a2Ready}.</li>
 * </ul>
 */
public class ThreadB extends Thread {

    private final SharedState state;

    public ThreadB(SharedState state) {
        super("ThreadB");
        this.state = state;
    }

    @Override
    public void run() {
        try {
            // FuncB1: B1 = sum(0..250) -- no dependency
            state.B1 = SumUtil.sum(250);

            // FuncB2: B2 = A1 + sum(0..200) -- needs A1 from ThreadA (FuncA1)
            state.a1Ready.await();
            state.B2 = state.A1 + SumUtil.sum(200);
            state.b2Ready.signal(); // Let ThreadA know B2 is ready (needed for FuncA2)

            // FuncB3: B3 = A2 + sum(0..400) -- needs A2 from ThreadA (FuncA2)
            state.a2Ready.await();
            state.B3 = state.A2 + SumUtil.sum(400);
            state.b3Ready.signal(); // Let ThreadA know B3 is ready (needed for FuncA3)

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
