# ECTE331 Project — Part 2 Report
## Problem 2: Threads Synchronisation and Communication

**Student:** Ahmed Walid Alaaeldin
**Student ID:** 7684125

---

## a) Final values of the shared variables

Using `sum(n) = n(n+1)/2`:

| Variable | Formula | Value |
|---|---|---|
| A1 | sum(0..500) | **125,250** |
| B1 | sum(0..250) | **31,375** |
| B2 | A1 + sum(0..200) = 125,250 + 20,100 | **145,350** |
| A2 | B2 + sum(0..300) = 145,350 + 45,150 | **190,500** |
| B3 | A2 + sum(0..400) = 190,500 + 80,200 | **270,700** |
| A3 | B3 + sum(0..400) = 270,700 + 80,200 | **350,900** |

The dependency graph in Figure 2.1 imposes a strict partial order on the six
function calls:

```
FuncA1 ---> FuncB2 ---> FuncA2 ---> FuncB3 ---> FuncA3
FuncB1 (independent, no downstream consumer)
```

FuncA1 and FuncB1 have no dependencies and may execute in either order (even
concurrently). From then on, execution must strictly alternate:
FuncB2 needs A1, FuncA2 needs B2, FuncB3 needs A2, FuncA3 needs B3.

---

## b) Synchronization design (no busy-wait, no `Thread.sleep`)

The assignment forbids both active waiting (`while(!cond){}`) and
`Thread.sleep()`. The correct Java mechanism for "block until a condition
becomes true, then wake up efficiently" is the monitor pattern built on the
object's intrinsic lock: `synchronized` + `wait()` + `notifyAll()`.

We encapsulate this pattern in a small reusable class, `SyncEvent`, which
behaves like a one-shot gate:

- `await()` — blocks the calling thread inside a `synchronized` method using
  `wait()` in a `while` loop (guarding against spurious wakeups), releasing
  the monitor lock while blocked so it costs no CPU cycles.
- `signal()` — sets an internal flag and calls `notifyAll()` to wake any
  thread waiting in `await()`.

Four `SyncEvent` instances are created, one per dependency edge in Figure 2.1:

| Event | Signaled by | Awaited by | Represents |
|---|---|---|---|
| `a1Ready` | ThreadA (after FuncA1) | ThreadB (before FuncB2) | A1 is ready |
| `b2Ready` | ThreadB (after FuncB2) | ThreadA (before FuncA2) | B2 is ready |
| `a2Ready` | ThreadA (after FuncA2) | ThreadB (before FuncB3) | A2 is ready |
| `b3Ready` | ThreadB (after FuncB3) | ThreadA (before FuncA3) | B3 is ready |

Because each thread only blocks on the *specific* event it depends on (not on
a global lock), FuncA1 and FuncB1 are free to run concurrently, and the rest
of the sequence is forced into the correct order purely by the
signal/await pairs — regardless of how the JVM/OS scheduler interleaves the
two threads.

---

## c) Implementation

Files (all in this submission):

- **`SumUtil.java`** — dedicated utility class with the static method
  `sum(int n)`, computed with a `for` loop (as required).
- **`SyncEvent.java`** — the wait/notify-based one-shot gate described above.
- **`SharedState.java`** — holds `A1,A2,A3,B1,B2,B3` and the four `SyncEvent`
  instances; also provides `reset()` for repeated test runs.
- **`ThreadA.java`** — runs `FuncA1 → (await b2Ready) → FuncA2 → (await b3Ready) → FuncA3`.
- **`ThreadB.java`** — runs `FuncB1 → (await a1Ready) → FuncB2 → (await a2Ready) → FuncB3`.
- **`ThreadSyncApp.java`** — the `main()` entry point.

Key excerpt from `ThreadA.java`:

```java
state.A1 = SumUtil.sum(500);
state.a1Ready.signal();          // unblock ThreadB's FuncB2

state.b2Ready.await();           // wait for ThreadB's FuncB2
state.A2 = state.B2 + SumUtil.sum(300);
state.a2Ready.signal();          // unblock ThreadB's FuncB3

state.b3Ready.await();           // wait for ThreadB's FuncB3
state.A3 = state.B3 + SumUtil.sum(400);
```

`ThreadB.java` mirrors this with `B1, B2, B3` and the complementary events.

---

## d) Testing for correctness over many iterations

`ThreadSyncApp.main()`:

1. Runs a single demonstration iteration and prints all six values plus a
   PASS/FAIL check against the mathematically expected results from part (a).
2. Runs **100,000 iterations** in a loop, resetting `SharedState` and
   restarting fresh `ThreadA`/`ThreadB` instances each time, comparing the
   final values to the expected results every time.

### Actual test output

```
=== Single demonstration run ===
A1=125250  B1=31375
B2=145350  A2=190500
B3=270700  A3=350900
Result: CORRECT

=== Stress test: verifying correctness over many iterations ===
Completed 100000 iterations.
Failures: 0
All iterations produced the correct final values -> synchronization is correct.
```

Zero failures across 100,000 independent runs is strong evidence that the
synchronization correctly enforces the dependency order shown in Figure 2.1
regardless of thread scheduling — satisfying the requirement that the
implementation work "irrespective of the Operating System's thread
scheduling," without any active waiting or use of `Thread.sleep()`.

---

## How to compile and run

```bash
javac *.java
java ThreadSyncApp
```
