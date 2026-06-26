# ECTE331 Project – Part 1 Report
## Fault-Tolerant Autonomous Drone Navigation System

---

## 1. Introduction

This report documents the design and implementation of a fault-tolerant autonomous drone
navigation system in Java. The system uses **Triple Modular Redundancy (TMR)** with three
redundant altitude sensors (A, B, C) to determine a reliable altitude estimate each cycle.
All important events are logged with timestamps; the primary log used throughout this
report is **`log.txt`** (produced during Run 3, `range=2`).

---

## 2. Class Overview

| Class | Role |
|---|---|
| `DroneNavigationSystem` | Main controller: reads sensors, votes, manages reliability |
| `SensorReadException` | Thrown on sensor hardware fault (extends `IOException`) |
| `SystemReliabilityException` | Thrown on 2 consecutive failures → SAFE MODE (extends `Exception`) |

### Key constants (adjustable for testing)

| Constant | Value | Purpose |
|---|---|---|
| `BASELINE` | 100 m | Base offset for valid readings |
| `DEFAULT_RANGE` | 5 | Offset range; smaller → more sensor agreement |
| `CYCLES` | 15 | Navigation cycles per run |
| `FAIL_THRESHOLD` | 2 | Consecutive failures before SAFE MODE |

### Fault probabilities per sensor per cycle

| `chance` range | Probability | Outcome |
|---|---|---|
| 0 – 14 | 15 % | Hardware failure → `SensorReadException` |
| 15 – 29 | 15 % | Corrupted reading (value outside [0, 200]) |
| 30 – 99 | 70 % | Valid reading: `BASELINE + random.nextInt(range)` |

---

## 3. System Flow Diagram

```
Each Cycle:
  ┌──────────────────────────────────┐
  │  readSensor(A) readSensor(B) readSensor(C)
  │      ↓               ↓               ↓
  │  [fail/corrupt/ok] [fail/corrupt/ok] [fail/corrupt/ok]
  └──────────────────────────────────┘
              │
         validCount = # sensors with reading in [0,200]
              │
     ┌────────┴────────┐
     │ validCount < 2  │ validCount ≥ 2
     │                 │
  RELIABILITY       vote(validIds, validVals)
   FAILURE            │
     │           ┌────┴────┐
     │        majority   no majority
     │           │           │
     │      prevAlt=maj   RELIABILITY
     │      consec=0       FAILURE
     │                       │
     └──────────┬────────────┘
            cycleFailure?
                │
           consecFails++
                │
          consecFails ≥ 2?
                │
           YES → throw SystemReliabilityException → SAFE MODE
```

---

## 4. Use Cases Demonstrated

The following runs, with different `range` parameters, demonstrate every possible scenario.
Each use case includes the relevant **console output** and the matching **log.txt entry**.

---

### UC-1 · All Three Sensors Agree (Normal Majority)

**Run 3 – Cycle 7** (`range=2`, log: `log.txt`)

All three sensors return the same valid altitude.

**Console:**
```
--- Cycle  7 / 15 -------------------------------------------
  Sensor A:  101 m   [OK]
  Sensor B:  101 m   [OK]
  Sensor C:  101 m   [OK]
  Valid sensors: 3/3   Unavailable: none
  [VOTING]  Sensor A=101  Sensor B=101  Sensor C=101
  [MAJORITY] All sensors agree: 101 m
  [ALTITUDE] Confirmed altitude = 101 m
```

**Log entry:**
```
[2026-06-25 23:03:28.068] MAJORITY DECISION [Cycle 7]: All sensors [A,B,C] agree on 101 m
```

---

### UC-2 · Two Sensors Agree + Outlier Detected

**Run 3 – Cycle 2** (`range=2`, log: `log.txt`)

Sensor C reads 101 m while A and B both read 100 m → A,B form the majority; C is an outlier.

**Console:**
```
--- Cycle  2 / 15 -------------------------------------------
  Sensor A:  100 m   [OK]
  Sensor B:  100 m   [OK]
  Sensor C:  101 m   [OK]
  Valid sensors: 3/3   Unavailable: none
  [VOTING]  Sensor A=100  Sensor B=100  Sensor C=101
  [MAJORITY] Sensors [A] and [B] agree: 100 m
  [OUTLIER]  Sensor [C] = 101 m differs from majority (100 m) – discarded
  [ALTITUDE] Confirmed altitude = 100 m
```

**Log entries:**
```
[2026-06-25 23:03:28.024] MAJORITY DECISION [Cycle 2]: Sensors [A,B] agree on 100 m
[2026-06-25 23:03:28.035] OUTLIER DETECTED [Cycle 2]: Sensor [C] reported 101 m | majority=100 m
```

> The same scenario occurs in Cycles 4, 5, 8, 10, 11 with different outlier sensors (A, B, or C).

---

### UC-3 · Two Valid Sensors Agree (One Sensor Failed)

**Run 2 – Cycle 5** (`range=1`, log: `log_20260625_230320.txt`)

Sensor B has a hardware failure; A and C both return 100 m → majority still found.

**Console:**
```
--- Cycle  5 / 15 -------------------------------------------
  Sensor A:  100 m   [OK]
  Sensor B: FAILURE   [Hardware fault on Sensor B (chance=11)]
  Sensor C:  100 m   [OK]
  Valid sensors: 2/3   Unavailable: [B]
  [VOTING]  Sensor A=100  Sensor C=100
  [MAJORITY] Sensors [A] and [C] agree: 100 m
  [ALTITUDE] Confirmed altitude = 100 m
```

**Log entries:**
```
[...] SENSOR FAILURE [Sensor B]: Hardware fault on Sensor B (chance=11)
[...] MAJORITY DECISION [Cycle 5]: Sensors [A,C] agree on 100 m
```

---

### UC-4 · Sensor Hardware Failure (`SensorReadException`)

**Run 3 – Cycle 1** (`range=2`, log: `log.txt`)

Sensor B throws `SensorReadException` (chance=4 < 15).

**Console:**
```
  Sensor B: FAILURE   [Hardware fault on Sensor B (chance=4)]
```

**Log entry:**
```
[2026-06-25 23:03:28.006] SENSOR FAILURE [Sensor B]: Hardware fault on Sensor B (chance=4)
```

---

### UC-5 · Corrupted Sensor Reading

**Run 3 – Cycle 1** (`range=2`, log: `log.txt`)

Sensor C returns −4 m (chance=15–29), which is outside [0, 200] → treated as unusable.

**Console:**
```
  Sensor C:   -4 m   [CORRUPTED - outside [0,200]]
```

**Log entry:**
```
[2026-06-25 23:03:28.007] CORRUPTED READING [Sensor C]: value=-4 m  (valid range=[0,200])
```

> Also observed: values > 200 (e.g., 276 m, 263 m, 290 m) in later cycles of the same run.

---

### UC-6 · No Majority (Two Valid Sensors Disagree) → Fallback

**Run 3 – Cycle 12** (`range=2`, log: `log.txt`)

Sensor C is corrupted; A=100 and B=101 disagree → no majority → fallback.

**Console:**
```
--- Cycle 12 / 15 -------------------------------------------
  Sensor A:  100 m   [OK]
  Sensor B:  101 m   [OK]
  Sensor C:  268 m   [CORRUPTED - outside [0,200]]
  Valid sensors: 2/3   Unavailable: [C]
  [VOTING]  Sensor A=100  Sensor B=101
  [NO MAJORITY] All valid sensors disagree: A=100  B=101
  [RELIABILITY FAIL] No majority found; all valid sensors disagree.
  [FALLBACK] Retaining previous altitude = 101 m
  [STATUS] Consecutive failures: 1 / 2
```

**Log entries:**
```
[2026-06-25 23:03:28.105] RELIABILITY FAILURE [Cycle 12]: No majority. Sensors=[A, B] Values=[100, 101]
[2026-06-25 23:03:28.106] FALLBACK DECISION [Cycle 12]: Retaining previous altitude = 101 m (no majority)
```

---

### UC-7 · Fewer Than 2 Valid Sensors → Reliability Failure + Fallback

**Run 3 – Cycle 1** (`range=2`, log: `log.txt`)

Only Sensor A is valid (B=FAILURE, C=CORRUPTED) → system cannot vote.

**Console:**
```
  Valid sensors: 1/3   Unavailable: [B, C]
  [RELIABILITY FAIL] Only 1 valid sensor(s) – cannot perform majority vote.
  [FALLBACK] Retaining previous altitude = 100 m
  [STATUS] Consecutive failures: 1 / 2
```

**Log entries:**
```
[2026-06-25 23:03:28.013] RELIABILITY FAILURE [Cycle 1]: Only 1 valid sensor(s). Unavailable: [B, C]
[2026-06-25 23:03:28.014] FALLBACK DECISION [Cycle 1]: Retaining previous altitude = 100 m (insufficient sensors)
```

> **UC-7b · Zero valid sensors** is shown in **Run 2 – Cycle 14**: A=corrupted, B=corrupted, C=FAILURE → 0/3 valid sensors. Log: `log_20260625_230320.txt`.

---

### UC-8 · Two Consecutive Failures → SAFE MODE

**Run 3 – Cycles 12 + 13** (`range=2`, log: `log.txt`)

Cycle 12 fails (no majority, §UC-6); Cycle 13 also fails (<2 valid). Two consecutive → SAFE MODE.

**Console:**
```
--- Cycle 13 / 15 -------------------------------------------
  Sensor A:  100 m   [OK]
  Sensor B:  -11 m   [CORRUPTED - outside [0,200]]
  Sensor C:  276 m   [CORRUPTED - outside [0,200]]
  Valid sensors: 1/3   Unavailable: [B, C]
  [RELIABILITY FAIL] Only 1 valid sensor(s) – cannot perform majority vote.
  [FALLBACK] Retaining previous altitude = 101 m
  [STATUS] Consecutive failures: 2 / 2

=================================
== *** SAFE MODE ACTIVATED *** ==
=================================
  Reason: Two consecutive reliability failures (cycles 12 and 13). System reliability compromised.
```

**Log entry:**
```
[2026-06-25 23:03:28.113] SAFE MODE ACTIVATED: Two consecutive reliability failures
                           (cycles 12 and 13). System reliability compromised.
```

---

### UC-9 · All 15 Cycles Complete Normally (No SAFE MODE)

**Run 5 – Full run** (`range=1`, log: `log_20260625_230336.txt`)

With range=1, all valid sensors return exactly 100 m, so majority is always found when
≥ 2 sensors are valid. Although some cycles triggered single (non-consecutive) failures,
no two consecutive failures occurred and all 15 cycles completed.

**Console (final lines):**
```
--- Cycle 15 / 15 -------------------------------------------
  ...
  [STATUS] Consecutive failures: 1 / 2        ← only 1, not 2 consecutive

[DONE] All 15 cycles completed normally.
```

---

## 5. Summary of Use Cases

| # | Use Case | Run | Range | Cycle(s) | Triggered by |
|---|---|---|---|---|---|
| UC-1 | All 3 sensors agree | Run 3 | 2 | 7 | All return 101 m |
| UC-2 | 2 agree + outlier | Run 3 | 2 | 2,4,5,8,10,11 | 2 match, 1 differs |
| UC-3 | 2 valid agree (1 failed) | Run 2 | 1 | 5 | B=FAILURE, A=C=100 |
| UC-4 | Sensor hardware failure | Run 3 | 2 | 1 | chance=4 < 15 |
| UC-5 | Corrupted reading | Run 3 | 2 | 1 | chance=15–29 |
| UC-6 | No majority → fallback | Run 3 | 2 | 12 | A=100, B=101 disagree |
| UC-7 | <2 valid → reliability fail | Run 3 | 2 | 1,3,6,9,13 | B+C fail/corrupt |
| UC-8 | SAFE MODE activation | Run 3 | 2 | 12+13 | 2 consecutive failures |
| UC-9 | All cycles normal | Run 5 | 1 | 1–15 | No consecutive failures |

---

## 6. Log File Reference

The primary log file submitted is **`log.txt`** (Run 3, range=2). It contains all six
required event types:

| Required Event | Present in log.txt | Example line |
|---|---|---|
| Sensor failure | ✓ | `SENSOR FAILURE [Sensor B]: Hardware fault...` |
| Corrupted reading | ✓ | `CORRUPTED READING [Sensor C]: value=-4 m...` |
| Outlier detection | ✓ | `OUTLIER DETECTED [Cycle 2]: Sensor [C] reported 101 m...` |
| Majority decision | ✓ | `MAJORITY DECISION [Cycle 2]: Sensors [A,B] agree on 100 m` |
| Fallback decision | ✓ | `FALLBACK DECISION [Cycle 1]: Retaining previous altitude = 100 m...` |
| SAFE MODE activation | ✓ | `SAFE MODE ACTIVATED: Two consecutive reliability failures...` |

Additional logs per run:

| Log File | Range | Key Events |
|---|---|---|
| `log_20260625_230317.txt` | 5 | Quick SAFE MODE (cycles 1+2) |
| `log_20260625_230320.txt` | 1 | 0-valid-sensor failure (UC-7b), SAFE MODE |
| `log_20260625_230327.txt` | 2 | All 6 event types, SAFE MODE (= `log.txt`) |
| `log_20260625_230331.txt` | 50 | No-majority fallback, SAFE MODE (cycles 1+2) |
| `log_20260625_230336.txt` | 1 | All 15 cycles completed normally |

---

## 7. Compilation and Execution

```bash
# Compile
javac *.java

# Run with default range (5)
java DroneNavigationSystem

# Run with custom range
java DroneNavigationSystem 1     # Always agree when valid
java DroneNavigationSystem 2     # Occasional outlier
java DroneNavigationSystem 50    # High disagreement

# Generate JavaDoc
javadoc -d javadoc -author -version -private *.java
```

---

## 8. Conclusion

The system correctly implements all required components:

- **Fault simulation**: 15% failure, 15% corruption, 70% valid per sensor per cycle.
- **TMR voting**: finds majority among 2 or 3 valid sensors; detects and logs outliers.
- **Reliability monitoring**: distinguishes two failure types (<2 valid, no majority);
  tracks consecutive failures and activates SAFE MODE after exactly 2.
- **Custom exceptions**: `SensorReadException` (hardware fault) and
  `SystemReliabilityException` (SAFE MODE trigger) used correctly.
- **Logging**: all six required event types written with timestamps to named log files.
- **JavaDoc**: all public and private members documented; HTML generated in `javadoc/`.
