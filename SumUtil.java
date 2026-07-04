/**
 * Utility class providing a static method to compute the sum
 *     sum_{i=0}^{n} i
 * using an explicit loop (as required by the assignment, part c).
 */
public final class SumUtil {

    private SumUtil() {
        // Utility class: prevent instantiation.
    }

    /**
     * Computes the sum of integers from 0 to n (inclusive) using a loop.
     *
     * @param n the upper bound of the summation (inclusive)
     * @return the sum 0 + 1 + 2 + ... + n
     */
    public static long sum(int n) {
        long total = 0L;
        for (int i = 0; i <= n; i++) {
            total += i;
        }
        return total;
    }
}
