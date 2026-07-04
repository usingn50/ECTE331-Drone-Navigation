# ECTE331 Project — Part 1 Report
## Fault-Tolerant Autonomous Drone Navigation System

**Student:** Ahmed Walid Alaaeldin
**Student ID:** 7684125

---

## 1. Introduction

This report documents the design, implementation, and verification of a fault-tolerant
autonomous drone navigation system in Java. The system uses **Triple Modular Redundancy
(TMR)** across three simulated altitude sensors (A, B, C) to produce a reliable altitude
estimate every cycle, with majority voting, fallback, reliability monitoring, custom
exception handling, and timestamped event logging to `log.txt`.

## 2. Class Overview

| Class | Role |
|---|---|
| `DroneNavigationSystem` | Main controller: reads sensors, votes, manages reliability, logs |
| `SensorReadException` | Thrown on sensor hardware fault (extends `IOException`) |
| `SystemReliabilityException` | Thrown on 2 consecutive reliability failures → SAFE MODE (extends `Exception`) |

### Fault probabilities per sensor per cycle (as specified)

| `chance` (0–99) | Probability | Outcome |
|---|---|---|
| 0 – 14 | 15% | Hardware failure → `SensorReadException` |
| 15 – 29 | 15% | Corrupted reading (value outside `[0, 200]`) |
| 30 – 99 | 70% | Valid reading: `baseline + random.nextInt(10)` |

## 3. Two correctness issues found during review and fixed

While verifying the system against the specification, two deviations were found and
corrected. Both were verified by re-running the program and inspecting `log.txt`.

### 3.1 SAFE MODE was never triggered (logic bug)

**Problem:** `runSimulation()` reset `consecutiveFailures = 0` after *every* iteration
that did not throw an exception — including iterations that were themselves reliability
failures but had not yet reached the threshold of 2. This meant the consecutive-failure
count could never accumulate past 1, so `SystemReliabilityException` (and therefore SAFE
MODE) could never actually be reached, no matter how many failures occurred in a row.

**Fix:** the reset was moved so it only happens inside `processIteration()`, and only when
a genuine majority decision succeeds:

```java
if (result != null) {
    consecutiveFailures = 0; // Reset only on a genuine successful majority decision
    ...
```

and the unconditional reset after the call in `runSimulation()` was removed. After the fix,
SAFE MODE is correctly triggered after two consecutive reliability failures (verified
below, §5.5).

### 3.2 Majority voting used a tolerance window instead of exact equality

**Problem:** the original code accepted two valid readings as a "majority" if they were
within 2 metres of each other (`VOTING_TOLERANCE = 2`). The specification states the rule
literally as *"if two valid sensors outputs are equal, then use that value."*

**Fix:** `VOTING_TOLERANCE` was changed to `0`, so two valid readings must now match
**exactly** to be accepted as a majority, matching the specification precisely.

## 4. Majority Voting and Reliability Logic (as implemented)

- Two valid readings equal → their (equal) value is used as the final altitude.
- All valid readings differ → fallback to the previous valid altitude.
- Fewer than 2 valid readings, or no majority among valid readings → counts as a
  **reliability failure**.
- Two **consecutive** reliability failures → `SystemReliabilityException` is thrown,
  "SAFE MODE ACTIVATED" is logged, and the simulation stops (emergency landing).

## 5. Verification: all required use cases, with real log evidence

The program was run repeatedly (per the assignment's own note that multiple runs may be
needed to observe every case). All excerpts below are taken verbatim from the actual
`log.txt` produced by these runs.

### 5.1 Sensor failure

```
[2026-07-04 13:25:07] Sensor A corrupted reading: -20
[2026-07-04 13:25:07] Sensor B failure: Hardware fault in Sensor B
```

### 5.2 Corrupted reading

```
[2026-07-04 13:25:07] Sensor A corrupted reading: -20
```

### 5.3 Majority decision (with outlier detection)

```
[2026-07-04 13:25:24] Majority decision: 106m
[2026-07-04 13:25:25] Majority decision: 108m [Outliers: Sensor B]
```
The second line shows the outlier-detection requirement in action: two sensors agreed on
108m while Sensor B's differing reading is explicitly named as the outlier.

### 5.4 Fallback decision / reliability failure

```
[2026-07-04 13:25:07] No majority found. Fallback to previous: 0m
[2026-07-04 13:25:07] Reliability failure: Insufficient valid sensors.
```

### 5.5 SAFE MODE activation (two consecutive reliability failures)

```
[2026-07-04 13:25:07] System initialization.
[2026-07-04 13:25:07] Sensor B failure: Hardware fault in Sensor B
[2026-07-04 13:25:07] No majority found. Fallback to previous: 0m
[2026-07-04 13:25:07] Sensor C failure: Hardware fault in Sensor C
[2026-07-04 13:25:07] No majority found. Fallback to previous: 0m
[2026-07-04 13:25:07] SAFE MODE ACTIVATED: Two consecutive reliability failures detected.
```
Immediately after this, the console shows the emergency landing sequence and the
simulation loop terminates (`break`), exactly as required.

## 6. Conclusion

After fixing the two issues above, the system satisfies every functional requirement in
the specification: fault injection with the correct probabilities, exception-based fault
handling, exact-match majority voting with fallback, reliability monitoring that correctly
accumulates consecutive failures, SAFE MODE activation and safe termination, and complete
timestamped logging of every required event type (sensor failure, corrupted reading,
outlier detection, majority decision, fallback decision, SAFE MODE activation) — all
demonstrated above with real log output, not hypothetical examples.
