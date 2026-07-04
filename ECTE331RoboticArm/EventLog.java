package ECTE331RoboticArm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Small thread-safe utility that timestamps every event and writes it both
 * to the console and to a shared log file, so that execution order and
 * blocking/priority-change events can be inspected after a run.
 */
public class EventLog {

    private final PrintWriter writer;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    public EventLog(String fileName) {
        PrintWriter w;
        try {
            w = new PrintWriter(new FileWriter(fileName, true));
        } catch (IOException e) {
            System.err.println("Could not open log file " + fileName + ": " + e.getMessage());
            w = null;
        }
        this.writer = w;
    }

    /**
     * Logs a single timestamped event to console and file.
     * @param message the event description
     */
    public synchronized void log(String message) {
        String line = "[" + format.format(new Date()) + "] " + message;
        System.out.println(line);
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    public synchronized void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
