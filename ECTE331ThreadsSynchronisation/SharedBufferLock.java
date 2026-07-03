package ECTE331ThreadsSynchronisation;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared buffer for the Producer-Consumer problem using ReentrantLock and Condition.
 */
public class SharedBufferLock {
    private final Queue<Integer> buffer;
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public SharedBufferLock(int capacity) {
        this.buffer = new LinkedList<>();
        this.capacity = capacity;
    }

    public void produce(int item) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.size() == capacity) {
                System.out.println(Thread.currentThread().getName() + ": Buffer is full, waiting...");
                notFull.await();
            }
            buffer.add(item);
            System.out.println(Thread.currentThread().getName() + ": Produced " + item + ". Buffer size: " + buffer.size());
            notEmpty.signalAll(); // Notify consumers that an item is available
        } finally {
            lock.unlock();
        }
    }

    public int consume() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + ": Buffer is empty, waiting...");
                notEmpty.await();
            }
            int item = buffer.remove();
            System.out.println(Thread.currentThread().getName() + ": Consumed " + item + ". Buffer size: " + buffer.size());
            notFull.signalAll(); // Notify producers that space is available
            return item;
        } finally {
            lock.unlock();
        }
    }
}
