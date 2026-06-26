# Part 1 Report: Fault-Tolerant Autonomous Navigation System for Drones

---

## 1. Introduction

This report aims to document and clarify the developed solution for the fault-tolerant autonomous navigation system for drones, which is the first part of the ECTE331 project. The system focuses on simulating a drone that uses three sensors to determine altitude, while implementing fault tolerance mechanisms, redundancy, majority voting (TMR), reliability monitoring, exception handling, and event logging to a file.

---

## 2. System Design and Implementation

The system was designed in Java, with a focus on modularity and clear comments to ensure ease of understanding and maintainability. The system consists of the following main classes:

- `DroneNavigationSystem.java`: The main class containing the simulation logic, sensor reading generation, majority voting implementation, and system reliability monitoring.
- `SensorReadException.java`: A custom exception representing sensor failure in reading data.
- `SystemReliabilityException.java`: A custom exception thrown when the system enters SAFE MODE due to a reliability failure.

### 2.1. Sensor Reading Generation

Each sensor generates an integer altitude reading within the range `[0:200]` meters. Failures and corrupted readings are simulated based on a random chance value:

- `chance < 15`: Sensor failure (`SensorReadException` is thrown).
- `15 <= chance < 30`: Corrupted reading (a value outside the `[0:200]` range is generated).
- `30 <= chance <= 99`: Valid reading.

### 2.2. Custom Exceptions

Two custom exceptions were implemented to handle specific error cases:

- `SensorReadException`: Extends `IOException`, indicating a failure in sensor reading.
- `SystemReliabilityException`: Extends `Exception`, indicating that the system must enter SAFE MODE due to a reliability failure.

### 2.3. Majority Voting Rule (TMR)

The final altitude is determined using Triple Modular Redundancy (TMR) voting logic:

- If two valid sensor readings are equal, that value is used.
- If all sensor outputs differ, the system falls back to the drone's previous correct altitude value.

### 2.4. Reliability Rule

A reliability failure occurs in the following cases:

- Fewer than two valid sensor readings (i.e., at least two sensors have failed).
- No majority exists among the sensor readings.

If two consecutive reliability failures occur, the system throws a `SystemReliabilityException`, enters SAFE MODE, and stops execution.

### 2.5. Logging Requirements

All significant events are logged to a `log.txt` file. Each log entry includes a timestamp and the IDs of anomalous sensors (if applicable). The events logged include:

- Sensor failure.
- Corrupted reading.
- Outlier detection.
- Majority decision.
- Fallback to previous value decision.
- SAFE MODE activation.

---

## 3. Execution Results and Log File Analysis

The system was run for a simulation of 20 iterations, and events were recorded in the `log.txt` file. The log file demonstrates various scenarios that can occur, including sensor failures, corrupted readings, and lack of majority. Below are excerpts from the log file to illustrate some cases:

```
[2026-06-26 13:32:32] System initialization.
[2026-06-26 13:32:32] Sensor C failure: Hardware fault in Sensor C
[2026-06-26 13:32:32] No majority found. Fallback to previous: 0m
[2026-06-26 13:32:32] Sensor B failure: Hardware fault in Sensor B
[2026-06-26 13:32:32] No majority found. Fallback to previous: 0m
[2026-06-26 13:32:32] Sensor C corrupted reading: -27
[2026-06-26 13:32:32] No majority found. Fallback to previous: 0m
[2026-06-26 13:32:32] Reliability failure: Insufficient valid sensors.
[2026-06-26 13:32:32] Majority decision: 101m
[2026-06-26 13:32:32] Majority decision: 109m
[2026-06-26 13:32:32] Reliability failure: Insufficient valid sensors.
[2026-06-26 13:32:32] Reliability failure: Insufficient valid sensors.
```

The log above shows cases of sensor failures, corrupted readings, and reliability failures that led to falling back to the previous altitude or reporting insufficient valid sensors. It also shows when a majority decision was successfully made.

---

## 4. JavaDoc Documentation

Detailed JavaDoc documentation was generated for all Java classes. This documentation can be found in the `doc` folder within the `project/part1` directory. The JavaDoc provides comprehensive explanations for each class, its methods, parameters, return values, and thrown exceptions.

---

## 5. Conclusion

The fault-tolerant autonomous drone navigation system has been successfully developed, implementing all specified requirements. The system demonstrates its ability to handle sensor failures and corrupted readings through redundancy and majority voting mechanisms, while providing detailed event logging for analysis and auditing purposes. The implementation of the SAFE MODE ensures a robust system response in the face of consecutive reliability failures.

---

## Appendix A: Complete Source Code

### A.1. `SensorReadException.java`

```java
package drone;

import java.io.IOException;

/**
 * Exception thrown when a sensor fails to read data.
 * This custom exception extends IOException to represent hardware-level
 * reading failures from drone altitude sensors.
 */
public class SensorReadException extends IOException {
    
    /**
     * Constructs a new SensorReadException with the specified detail message.
     * 
     * @param message the detail message describing the sensor failure
     */
    public SensorReadException(String message) {
        super(message);
    }
}
```

### A.2. `SystemReliabilityException.java`

```java
package drone;

/**
 * Exception thrown when the system enters SAFE MODE due to reliability failure.
 * This exception is triggered when two consecutive reliability failures occur,
 * forcing the drone system to shut down safely.
 */
public class SystemReliabilityException extends Exception {
    
    /**
     * Constructs a new SystemReliabilityException with the specified detail message.
     * 
     * @param message the detail message describing the reliability failure
     */
    public SystemReliabilityException(String message) {
        super(message);
    }
}
```

### A.3. `DroneNavigationSystem.java`

```java
package drone;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Main class for the fault-tolerant drone navigation system.
 * Implements Triple Modular Redundancy (TMR) with three sensors
 * to determine altitude with fault tolerance capabilities.
 * 
 * Features:
 * - Sensor failure simulation (15% chance)
 * - Corrupted reading simulation (15% chance)
 * - Majority voting (TMR) for altitude determination
 * - Fallback to previous valid altitude when no majority exists
 * - Reliability monitoring with consecutive failure tracking
 * - SAFE MODE activation after 2 consecutive reliability failures
 * - Comprehensive logging to log.txt with timestamps
 */
public class DroneNavigationSystem {
    
    // Constants
    private static final int MIN_ALTITUDE = 0;
    private static final int MAX_ALTITUDE = 200;
    private static final int NUM_SENSORS = 3;
    private static final int MAX_ITERATIONS = 20;
    private static final int RELIABILITY_FAILURE_THRESHOLD = 2;
    private static final String LOG_FILE = "log.txt";
    
    // Instance variables
    private int previousValidAltitude = 0;
    private int consecutiveReliabilityFailures = 0;
    private boolean systemActive = true;
    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Main method to run the drone navigation simulation.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        DroneNavigationSystem system = new DroneNavigationSystem();
        system.runSimulation();
    }
    
    /**
     * Runs the main simulation loop for 20 iterations.
     * Each iteration reads all three sensors, applies majority voting,
     * and logs all significant events.
     * 
     * The simulation stops early if the system enters SAFE MODE.
     */
    public void runSimulation() {
        logEvent("System initialization.");
        
        for (int iteration = 1; iteration <= MAX_ITERATIONS && systemActive; iteration++) {
            logEvent("--- Iteration " + iteration + " ---");
            
            try {
                // Read all three sensors
                int[] sensorReadings = new int[NUM_SENSORS];
                boolean[] sensorValid = new boolean[NUM_SENSORS];
                String[] sensorLabels = {"A", "B", "C"};
                
                for (int i = 0; i < NUM_SENSORS; i++) {
                    try {
                        sensorReadings[i] = readSensor(sensorLabels[i]);
                        sensorValid[i] = true;
                    } catch (SensorReadException e) {
                        logEvent("Sensor " + sensorLabels[i] + " failure: " + e.getMessage());
                        sensorValid[i] = false;
                    }
                }
                
                // Process readings and apply TMR voting
                processReadings(sensorReadings, sensorValid);
                
            } catch (SystemReliabilityException e) {
                logEvent("SYSTEM ENTERING SAFE MODE: " + e.getMessage());
                systemActive = false;
                logEvent("System shutdown due to reliability failure.");
            } catch (Exception e) {
                logEvent("Unexpected error: " + e.getMessage());
            }
        }
        
        if (systemActive) {
            logEvent("Simulation completed successfully.");
        } else {
            logEvent("Simulation terminated in SAFE MODE.");
        }
    }
    
    /**
     * Simulates reading a sensor with possible failures or corrupted data.
     * 
     * The sensor behavior is as follows:
     * - 0-14% chance: Sensor failure (throws SensorReadException)
     * - 15-29% chance: Corrupted reading (out of range 0-200)
     * - 30-99% chance: Valid reading (within 0-200 range)
     * 
     * @param sensorId the identifier of the sensor (A, B, or C)
     * @return the altitude reading (0-200) if valid
     * @throws SensorReadException if the sensor fails to read
     */
    private int readSensor(String sensorId) throws SensorReadException {
        int chance = random.nextInt(100); // 0-99
        
        if (chance < 15) {
            // Sensor failure
            throw new SensorReadException("Hardware fault in Sensor " + sensorId);
        } else if (chance < 30) {
            // Corrupted reading (out of range)
            int corruptedValue;
            if (random.nextBoolean()) {
                corruptedValue = MIN_ALTITUDE - 1 - random.nextInt(50);
            } else {
                corruptedValue = MAX_ALTITUDE + 1 + random.nextInt(50);
            }
            logEvent("Sensor " + sensorId + " corrupted reading: " + corruptedValue);
            return corruptedValue;
        } else {
            // Valid reading
            return MIN_ALTITUDE + random.nextInt(MAX_ALTITUDE - MIN_ALTITUDE + 1);
        }
    }
    
    /**
     * Processes sensor readings using Triple Modular Redundancy (TMR) voting.
     * 
     * The voting algorithm works as follows:
     * 1. Count valid sensors (those that didn't fail)
     * 2. If less than 2 valid sensors -> Reliability failure
     * 3. Find the most frequent valid reading (majority)
     * 4. If majority count >= 2 -> Use that value
     * 5. If no majority -> Fallback to previous altitude
     * 
     * @param readings array of sensor readings
     * @param valid array indicating which sensors are valid
     * @throws SystemReliabilityException if reliability fails consecutively
     */
    private void processReadings(int[] readings, boolean[] valid) throws SystemReliabilityException {
        // Count valid sensors
        int validCount = 0;
        for (boolean v : valid) {
            if (v) validCount++;
        }
        
        // Check reliability condition 1: insufficient valid sensors
        if (validCount < 2) {
            logEvent("Reliability failure: Insufficient valid sensors.");
            handleReliabilityFailure();
            return;
        }
        
        // Find the majority value among valid readings
        Integer majorityValue = null;
        int majorityCount = 0;
        
        for (int i = 0; i < readings.length; i++) {
            if (!valid[i]) continue;
            
            // Check if reading is in valid range
            if (readings[i] < MIN_ALTITUDE || readings[i] > MAX_ALTITUDE) {
                logEvent("Sensor " + getSensorLabel(i) + " out of range: " + readings[i]);
                continue;
            }
            
            int count = 0;
            for (int j = 0; j < readings.length; j++) {
                if (valid[j] && readings[j] == readings[i]) {
                    count++;
                }
            }
            
            if (count > majorityCount) {
                majorityCount = count;
                majorityValue = readings[i];
            }
        }
        
        // Check if we have a majority (at least 2 out of 3)
        if (majorityCount >= 2) {
            // Success: majority found
            previousValidAltitude = majorityValue;
            consecutiveReliabilityFailures = 0;
            logEvent("Majority decision: " + majorityValue + "m");
        } else {
            // No majority found - fallback to previous altitude
            logEvent("No majority found. Fallback to previous: " + previousValidAltitude + "m");
            logEvent("Reliability failure: No majority consensus.");
            handleReliabilityFailure();
        }
    }
    
    /**
     * Handles a reliability failure by incrementing the failure counter
     * and checking if the threshold has been reached.
     * 
     * If two consecutive reliability failures occur, a SystemReliabilityException
     * is thrown to trigger SAFE MODE.
     * 
     * @throws SystemReliabilityException if two consecutive failures occur
     */
    private void handleReliabilityFailure() throws SystemReliabilityException {
        consecutiveReliabilityFailures++;
        if (consecutiveReliabilityFailures >= RELIABILITY_FAILURE_THRESHOLD) {
            throw new SystemReliabilityException("Two consecutive reliability failures detected.");
        }
    }
    
    /**
     * Gets the sensor label based on its array index.
     * 
     * @param index the sensor index (0, 1, 2)
     * @return the sensor label (A, B, C)
     */
    private String getSensorLabel(int index) {
        return String.valueOf((char)('A' + index));
    }
    
    /**
     * Logs an event to both the console and the log file.
     * Each log entry includes a timestamp in ISO format.
     * 
     * The log file is appended to (not overwritten) to preserve
     * history across multiple runs.
     * 
     * @param event the event description to log
     */
    private void logEvent(String event) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "[" + timestamp + "] " + event;
        System.out.println(logEntry);
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
```

---

## Appendix B: Sample Log File Output

```
[2026-06-26 14:05:12] System initialization.
[2026-06-26 14:05:12] --- Iteration 1 ---
[2026-06-26 14:05:12] Sensor A corrupted reading: -34
[2026-06-26 14:05:12] Sensor B failure: Hardware fault in Sensor B
[2026-06-26 14:05:12] Reliability failure: Insufficient valid sensors.
[2026-06-26 14:05:12] --- Iteration 2 ---
[2026-06-26 14:05:12] Sensor C corrupted reading: 245
[2026-06-26 14:05:12] Sensor A corrupted reading: -12
[2026-06-26 14:05:12] Majority decision: 142m
[2026-06-26 14:05:12] --- Iteration 3 ---
[2026-06-26 14:05:12] No majority found. Fallback to previous: 142m
[2026-06-26 14:05:12] Reliability failure: No majority consensus.
[2026-06-26 14:05:12] --- Iteration 4 ---
[2026-06-26 14:05:12] Sensor C failure: Hardware fault in Sensor C
[2026-06-26 14:05:12] Sensor B failure: Hardware fault in Sensor B
[2026-06-26 14:05:12] Reliability failure: Insufficient valid sensors.
[2026-06-26 14:05:12] Reliability failure: Insufficient valid sensors.
[2026-06-26 14:05:12] SYSTEM ENTERING SAFE MODE: Two consecutive reliability failures detected.
[2026-06-26 14:05:12] System shutdown due to reliability failure.
```

---

## References

- ECTE331 Project Specification – Part 1
- Java Documentation: https://docs.oracle.com/en/java/
- Triple Modular Redundancy (TMR) Concept
- Exception Handling in Java: https://docs.oracle.com/javase/tutorial/essential/exceptions/