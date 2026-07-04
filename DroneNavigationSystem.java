import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Simulates a fault-tolerant autonomous drone navigation system.
 * Uses Triple Modular Redundancy (TMR) for altitude estimation.
 */
public class DroneNavigationSystem {
    private static final int MIN_ALTITUDE = 0;
    private static final int MAX_ALTITUDE = 200;
    private static final String LOG_FILE = "log.txt";
    private static final int FAULT_CHANCE_PERCENT = 15;
    private static final int CORRUPTION_CHANCE_PERCENT = 30;
    private static final int VOTING_TOLERANCE = 0; // Exact-match voting, per spec: "if two valid sensors outputs are equal"
    
    private int lastValidAltitude = 0;
    private int consecutiveFailures = 0;
    private Random random = new Random();

    /**
     * Main entry point for the Drone Navigation System simulation.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        DroneNavigationSystem system = new DroneNavigationSystem();
        system.runSimulation();
    }

    /**
     * Main simulation loop.
     */
    public void runSimulation() {
        System.out.println("==========================================");
        System.out.println("   Drone Navigation System Initialized    ");
        System.out.println("==========================================");
        try (BufferedWriter logger = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            log(logger, "System initialization.");

            for (int i = 1; i <= 20; i++) {
                System.out.println("\nIteration: " + i);
                try {
                    processIteration(logger);
                } catch (SystemReliabilityException e) {
                    System.err.println("CRITICAL ERROR: " + e.getMessage());
                    log(logger, "SAFE MODE ACTIVATED: " + e.getMessage());
                    activateEmergencyLanding();
                    break;
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                }
            }
            System.out.println("\n--- Simulation Summary ---");
            System.out.println("Last Altitude: " + lastValidAltitude + "m");
            System.out.println("Status: Completed Successfully");
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }

    /**
     * Processes a single navigation iteration.
     */
    private void processIteration(BufferedWriter logger) throws SystemReliabilityException, IOException {
        int[] readings = new int[3];
        boolean[] valid = new boolean[3];
        String[] sensorIds = {"Sensor A", "Sensor B", "Sensor C"};
        int validCount = 0;

        // 1. Generate Sensor Readings
        for (int i = 0; i < 3; i++) {
            try {
                readings[i] = readSensor(sensorIds[i], logger);
                if (readings[i] >= MIN_ALTITUDE && readings[i] <= MAX_ALTITUDE) {
                    valid[i] = true;
                    validCount++;
                } else {
                    valid[i] = false;
                    System.out.println(sensorIds[i] + ": Corrupted reading (" + readings[i] + ")");
                    log(logger, sensorIds[i] + " corrupted reading: " + readings[i]);
                }
            } catch (SensorReadException e) {
                valid[i] = false;
                System.out.println(sensorIds[i] + ": Failed - " + e.getMessage());
                log(logger, sensorIds[i] + " failure: " + e.getMessage());
            }
        }

        System.out.print("Readings: ");
        for(int r : readings) System.out.print(r + " ");
        System.out.println();

        // 2. Majority Voting and Reliability Check
        Integer result = determineAltitude(readings, valid);
        
        if (result != null) {
            consecutiveFailures = 0; // Reset only on a genuine successful majority decision
            lastValidAltitude = result;
            System.out.println("Majority Decision: " + result + "m");
            
            // Outlier Detection
            StringBuilder outliers = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                if (valid[i] && readings[i] != result) {
                    outliers.append(sensorIds[i]).append(" ");
                }
            }
            String outlierMsg = outliers.length() > 0 ? " [Outliers: " + outliers.toString().trim() + "]" : "";
            if (!outlierMsg.isEmpty()) {
                System.out.println("Outlier detected: " + outliers.toString().trim());
            }
            log(logger, "Majority decision: " + result + "m" + outlierMsg);
        } else {
            consecutiveFailures++;
            if (validCount >= 2) {
                // All differ but at least 2 were valid
                System.out.println("No Majority. Fallback to: " + lastValidAltitude + "m");
                log(logger, "No majority found. Fallback to previous: " + lastValidAltitude + "m");
            } else {
                System.out.println("Reliability Failure: Insufficient valid sensors.");
                log(logger, "Reliability failure: Insufficient valid sensors.");
            }

            if (consecutiveFailures >= 2) {
                throw new SystemReliabilityException("Two consecutive reliability failures detected.");
            }
        }
    }

    /**
     * Simulates reading from a sensor with fault injection.
     */
    private int readSensor(String id, BufferedWriter logger) throws SensorReadException {
        int chance = random.nextInt(100);
        int baseline = 100;

        if (chance < FAULT_CHANCE_PERCENT) {
            throw new SensorReadException("Hardware fault in " + id);
        } else if (chance < CORRUPTION_CHANCE_PERCENT) {
            return -50 + random.nextInt(50); // Corrupted: outside [0:200]
        } else {
            return baseline + random.nextInt(10); // Valid
        }
    }

    /**
     * Implements TMR voting logic.
     */
    private Integer determineAltitude(int[] readings, boolean[] valid) {
        int validCount = 0;
        for (boolean b : valid) if (b) validCount++;

        if (validCount < 2) return null;

        // Check for majority: two valid readings must match exactly (VOTING_TOLERANCE = 0)
        if (valid[0] && valid[1] && Math.abs(readings[0] - readings[1]) <= VOTING_TOLERANCE) 
            return (readings[0] + readings[1]) / 2;
        if (valid[0] && valid[2] && Math.abs(readings[0] - readings[2]) <= VOTING_TOLERANCE) 
            return (readings[0] + readings[2]) / 2;
        if (valid[1] && valid[2] && Math.abs(readings[1] - readings[2]) <= VOTING_TOLERANCE) 
            return (readings[1] + readings[2]) / 2;

        return null; // No majority
    }

    /**
     * Helper for logging with timestamp.
     */
    private void log(BufferedWriter writer, String message) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        writer.write("[" + timestamp + "] " + message);
        writer.newLine();
        writer.flush();
    }

    private void activateEmergencyLanding() {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("   EMERGENCY LANDING SEQUENCE ACTIVATED   ");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("Descending safely to " + MIN_ALTITUDE + "m...");
        System.out.println("Drone grounded. System Terminated.");
    }
}
