# ECTE331 — Operating Systems Projects

**Student:** Ahmed Walid Alaaeldin
**Student ID:** 7684125

This repository contains three projects developed for the ECTE331 (Operating
Systems / Real-time Embedded Systems) course, covering fault tolerance, shared
resource management, and thread synchronization in multi-threaded Java
applications.

## 1. Fault-Tolerant Autonomous Drone Navigation System

**Main file:** `DroneNavigationSystem.java`
**Custom exceptions:** `SensorReadException.java`, `SystemReliabilityException.java`

Simulates a drone altitude-navigation system using Triple Modular Redundancy
(TMR) across three sensors, including:

- Simulated sensor failure / corrupted reading / valid reading, at the
  specified probabilities
- Majority voting (exact-match) to determine the final altitude
- Fallback to the previous valid altitude when all readings disagree
- SAFE MODE activation after two consecutive reliability failures
- Timestamped logging of every required event to `log.txt`

Full report, with real log evidence for every required use case:
`Report_Part1.md`

## 2. Real-Time Robotic Arm Controller with Priority Management

**Folder:** `ECTE331RoboticArm`

Simulates a robotic arm controller with three real-time threads of different
priorities (High: `SafetyMonitor`, Medium: `MotionPlanner`, Low: `Logger`)
sharing one resource (`MotorController`). Covers all six required tasks:

| Task | Files | Description |
|---|---|---|
| 1–2 | `RoboticArmSystem.java` | Basic multi-threaded implementation with mutual exclusion on the shared resource |
| 3 | `PriorityInversionDemo.java`, `LowPriorityTask.java`, `MediumPriorityTask.java` | Controlled scenario demonstrating priority inversion, reproducibly |
| 4 | `MotorController.java` (`PriorityMode.INHERITANCE`) | Simulated priority inheritance |
| 5 | `MotorController.java` (`PriorityMode.CEILING`) | Priority ceiling protocol |
| 6 | `PerformanceEvaluator.java`, `performance_results.csv`, `performance_chart.png` | Performance evaluation of all three strategies over 20 trials, with tables and a chart |

Full report covering Tasks 1–6, with real log evidence:
`ECTE331RoboticArm/Report_RoboticArm.md`

**Methodology note:** Java thread priorities are only a scheduling *hint* to
the JVM/OS and are not a real-time guarantee on general-purpose operating
systems. `MotorController` therefore models the medium-priority interference
deterministically (see its Javadoc) so the experiment is reproducible on any
machine, rather than depending on OS-specific scheduler behaviour.

## 3. Threads Synchronisation and Communication

**Folder:** `ECTE331ThreadsSynchronisation`

Solves "Problem 2" from the course: two threads (A and B) with a specific
execution dependency across six functions (FuncA1-3, FuncB1-3) sharing six
variables, without using busy-wait or `Thread.sleep()` to enforce ordering.

- `SumUtil.java` — utility method computing the required summation with a loop
- `SyncEvent.java` — a one-shot synchronization gate built on
  `synchronized`/`wait()`/`notifyAll()`
- `SharedState.java` — holds the shared variables and the four synchronization events
- `ThreadA.java`, `ThreadB.java` — implement the two threads per the
  dependency order in Figure 2.1
- `ThreadSyncApp.java` — demonstration run + a 100,000-iteration correctness
  stress test
- `Report_Part2.md` — full report explaining the mathematical solution,
  synchronization design, and test results

## How to run

Requires a Java Development Kit (JDK 17 or later).

1. **Clone the repository:**
    ```bash
    git clone https://github.com/usingn50/ECTE331-Drone-Navigation.git
    cd ECTE331-Drone-Navigation
    ```

2. **Drone Navigation System:**
    ```bash
    javac DroneNavigationSystem.java SensorReadException.java SystemReliabilityException.java
    java DroneNavigationSystem
    ```

3. **Robotic Arm Controller:**
    ```bash
    javac ECTE331RoboticArm/*.java
    java -cp . ECTE331RoboticArm.RoboticArmSystem        # Tasks 1-2: basic demo
    java -cp . ECTE331RoboticArm.PriorityInversionDemo    # Tasks 3-5: inversion + both protocols
    java -cp . ECTE331RoboticArm.PerformanceEvaluator 20  # Task 6: performance evaluation
    ```

4. **Threads Synchronisation and Communication:**
    ```bash
    cd ECTE331ThreadsSynchronisation
    javac *.java
    java ThreadSyncApp
    ```

## Documentation

JavaDoc for the Drone Navigation project is in `docs/DroneNavigationSystem`.
Each part's report is a Markdown file inside that part's own folder (or the
repository root for Part 1).
