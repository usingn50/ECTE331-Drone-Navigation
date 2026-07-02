package ECTE331RoboticArm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Low-priority thread that records system activity to a log file.
 */
public class Logger extends Thread {
    private static final String LOG_FILE = "robotic_arm_log.txt";
    private PrintWriter writer;
    private MotorController motorController;

    public Logger(MotorController controller) {
        this.motorController = controller;
        setName("Logger");
        setPriority(Thread.MIN_PRIORITY); // Low Priority
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true));
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Simulate logging activity
                Thread.sleep(200); // Log every 200ms
                log("System heartbeat - Motor position: " + motorController.getCurrentPosition());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(System.currentTimeMillis() + " - " + getName() + ": Interrupted.");
            } catch (Exception e) {
                System.err.println("Logger error: " + e.getMessage());
            }
        }
        if (writer != null) {
            writer.close();
        }
    }

    public synchronized void log(String message) {
        if (writer != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            writer.println("[" + timestamp + "] " + message);
            writer.flush();
        }
    }
}
