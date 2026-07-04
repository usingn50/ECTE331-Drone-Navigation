package ECTE331RoboticArm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 6: Performance evaluation. Runs the controlled priority-inversion
 * scenario ({@link PriorityInversionDemo}) many times under each of the
 * three {@link PriorityMode}s, measuring the high-priority thread's waiting
 * time on every trial. Prints a summary table (min/avg/max) to the console
 * and writes the raw per-trial results to {@code performance_results.csv}
 * for charting.
 */
public class PerformanceEvaluator {

    public static void main(String[] args) throws Exception {
        int trials = args.length > 0 ? Integer.parseInt(args[0]) : 20;

        // Use a separate, quieter log file so the console stays readable.
        EventLog log = new EventLog("performance_evaluation_log.txt");

        List<Long> noneResults = new ArrayList<>();
        List<Long> inheritanceResults = new ArrayList<>();
        List<Long> ceilingResults = new ArrayList<>();

        for (int i = 0; i < trials; i++) {
            noneResults.add(PriorityInversionDemo.run(PriorityMode.NONE, log));
            inheritanceResults.add(PriorityInversionDemo.run(PriorityMode.INHERITANCE, log));
            ceilingResults.add(PriorityInversionDemo.run(PriorityMode.CEILING, log));
        }
        log.close();

        printSummary("NONE (baseline)", noneResults);
        printSummary("INHERITANCE", inheritanceResults);
        printSummary("CEILING", ceilingResults);

        writeCsv("performance_results.csv", noneResults, inheritanceResults, ceilingResults);
        System.out.println("\nRaw per-trial results written to performance_results.csv");
    }

    private static void printSummary(String label, List<Long> results) {
        long min = results.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = results.stream().mapToLong(Long::longValue).max().orElse(0);
        double avg = results.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("%-16s  min=%-4d  avg=%-8.1f  max=%-4d  (ms, high-priority wait time)%n",
                label, min, avg, max);
    }

    private static void writeCsv(String fileName, List<Long> none, List<Long> inheritance, List<Long> ceiling)
            throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(fileName, false))) {
            w.println("trial,none_ms,inheritance_ms,ceiling_ms");
            for (int i = 0; i < none.size(); i++) {
                w.println((i + 1) + "," + none.get(i) + "," + inheritance.get(i) + "," + ceiling.get(i));
            }
        }
    }
}
