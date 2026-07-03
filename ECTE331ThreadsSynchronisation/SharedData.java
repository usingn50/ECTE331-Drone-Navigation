package ECTE331ThreadsSynchronisation;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * Shared data for the Reader-Writer problem using ReentrantReadWriteLock.
 */
public class SharedData {
    private int data = 0;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public int read() throws InterruptedException {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + ": Reading data: " + data);
            Thread.sleep(50); // Simulate reading time
            return data;
        } finally {
            readLock.unlock();
        }
    }

    public void write(int newData) throws InterruptedException {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + ": Writing data: " + newData);
            Thread.sleep(100); // Simulate writing time
            this.data = newData;
        } finally {
            writeLock.unlock();
        }
    }
}
