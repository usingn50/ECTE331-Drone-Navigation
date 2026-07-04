/**
 * Main application for Problem 2: Threads Synchronisation and Communication.
 *
 * <p>Part (a): the expected, mathematically-correct final values are:</p>
 * <pre>
 *   A1 = sum(0..500)         = 125250
 *   B1 = sum(0..250)         =  31375
 *   B2 = A1 + sum(0..200)    = 145350
 *   A2 = B2 + sum(0..300)    = 190500
 *   B3 = A2 + sum(0..400)    = 270700
 *   A3 = B3 + sum(0..400)    = 350900
 * </pre>
 *
 * <p>Part (d): {@code main} first runs one iteration with detailed console
 * output, then runs a large number of iterations (default 100,000) to verify
 * that the synchronization logic in {@link ThreadA} and {@link ThreadB}
 * produces the correct final values on every single run, regardless of how
 * the OS scheduler interleaves the two threads.</p>
 */
public class ThreadSyncApp {

    // Expected values, independently computed from the formulas (part a).
    private static final long EXPECTED_A1 = 125_250L;
    private static final long EXPECTED_B1 = 31_375L;
    private static final long EXPECTED_B2 = 145_350L;
    private static final long EXPECTED_A2 = 190_500L;
    private static final long EXPECTED_B3 = 270_700L;
    private static final long EXPECTED_A3 = 350_900L;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Single demonstration run ===");
        SharedState demo = new SharedState();
        runOnce(demo);
        System.out.println("A1=" + demo.A1 + "  B1=" + demo.B1);
        System.out.println("B2=" + demo.B2 + "  A2=" + demo.A2);
        System.out.println("B3=" + demo.B3 + "  A3=" + demo.A3);
        System.out.println(matchesExpected(demo) ? "Result: CORRECT" : "Result: INCORRECT");

        System.out.println();
        System.out.println("=== Stress test: verifying correctness over many iterations ===");
        int iterations = 100_000;
        SharedState state = new SharedState();
        int failures = 0;

        for (int i = 1; i <= iterations; i++) {
            state.reset();
            runOnce(state);
            if (!matchesExpected(state)) {
                failures++;
                System.out.println("Iteration " + i + " FAILED: "
                        + "A1=" + state.A1 + " B1=" + state.B1
                        + " B2=" + state.B2 + " A2=" + state.A2
                        + " B3=" + state.B3 + " A3=" + state.A3);
            }
        }

        System.out.println("Completed " + iterations + " iterations.");
        System.out.println("Failures: " + failures);
        System.out.println(failures == 0
                ? "All iterations produced the correct final values -> synchronization is correct."
                : "Some iterations produced incorrect values -> synchronization has a bug.");
    }

    /**
     * Starts ThreadA and ThreadB on the given shared state and waits for
     * both to finish before returning.
     */
    private static void runOnce(SharedState state) throws InterruptedException {
        ThreadA threadA = new ThreadA(state);
        ThreadB threadB = new ThreadB(state);
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
    }

    private static boolean matchesExpected(SharedState state) {
        return state.A1 == EXPECTED_A1
                && state.B1 == EXPECTED_B1
                && state.B2 == EXPECTED_B2
                && state.A2 == EXPECTED_A2
                && state.B3 == EXPECTED_B3
                && state.A3 == EXPECTED_A3;
    }
}
