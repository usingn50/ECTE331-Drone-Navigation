# ECTE331 Robotic Arm Project — Task 6: Performance Evaluation

## Methodology

Each trial runs the controlled scenario from `PriorityInversionDemo`:

1. `LowPriorityTask` acquires the shared `MotorController` and holds it for
   300 ms.
2. 50 ms later, `MediumPriorityTask` begins 400 ms of unrelated CPU-bound
   work that never touches the resource.
3. 50 ms after that, `SafetyMonitor` (high priority) requests the resource
   and blocks until it becomes available.

The metric recorded is the **waiting time of the high-priority thread**,
measured with `System.nanoTime()` from the moment it requests the lock to
the moment it acquires it. The scenario was run **20 times per strategy**
(`PerformanceEvaluator`, see `performance_results.csv` for raw data).

Because general-purpose JVMs/operating systems only treat `Thread` priority
as a scheduling *hint* rather than a real-time guarantee, the interference of
the medium-priority thread is modelled deterministically in `MotorController`
(see its Javadoc) so that the experiment is reproducible on any machine,
rather than depending on OS-specific scheduler behaviour.

## Results

| Strategy | Min (ms) | Avg (ms) | Max (ms) |
|---|---|---|---|
| NONE (baseline) | 607 | 609.6 | 635 |
| Priority Inheritance | 244 | 253.2 | 257 |
| Priority Ceiling | 202 | 203.3 | 204 |

![Performance comparison chart](performance_chart.png)

## Discussion

- **Baseline (NONE):** the high-priority thread waits ~610 ms on average —
  far longer than the low-priority task's own 300 ms critical section. The
  extra ~300 ms is the medium-priority task indirectly delaying the release
  of the resource: a textbook case of **priority inversion**.
- **Priority Inheritance:** once the high-priority thread blocks, it boosts
  the low-priority holder's priority so it is no longer starved by the
  medium-priority task. Waiting time drops to ~253 ms — close to the
  holder's true 300 ms critical section (the small gap is the 50 ms delay
  before the high-priority thread actually requests the resource in the
  scenario timeline).
- **Priority Ceiling:** the holder is boosted to the ceiling priority
  *immediately upon acquiring* the resource, before any other thread even
  requests it. This gives the **lowest and most consistent** waiting time
  (~203 ms, tightest spread of the three strategies) because protection is
  proactive rather than reactive.
- **Variance:** the ceiling strategy has the smallest min–max spread (2 ms),
  confirming it gives the most *predictable* execution behaviour — an
  important property for real-time systems, in addition to average-case
  speed.

## Conclusion

Both mitigation protocols eliminate the unbounded priority inversion seen in
the baseline. Priority ceiling outperforms priority inheritance in this
scenario because it protects the holder from the very start of its critical
section rather than only after a higher-priority thread is already blocked,
and it produces the most predictable (lowest-variance) response time —
exactly the behaviour real-time systems require.
