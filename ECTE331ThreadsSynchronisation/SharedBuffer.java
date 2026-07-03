package ECTE331ThreadsSynchronisation;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Shared buffer for the Producer-Consumer problem using synchronized methods and wait/notify.
 */
public class SharedBuffer {
    private final Queue<Integer> buffer;
    private final int capacity;

    public SharedBuffer(int capacity) {
        this.buffer = new LinkedList<>();
        this.capacity = capacity;
    }

    public synchronized void produce(int item) throws InterruptedException {
        while (buffer.size() == capacity) {
            System.out.println(Thread.currentThread().getName() + ": Buffer is full, waiting...");
            wait();
        }
        buffer.add(item);
        System.out.println(Thread.currentThread().getName() + ": Produced " + item + ". Buffer size: " + buffer.size());
        notifyAll(); // Notify consumers that an item is available
    }

    public synchronized int consume() throws InterruptedException {
        while (buffer.isEmpty()) {
            System.out.println(Thread.currentThread().getName() + ": Buffer is empty, waiting...");
            wait();
        }
        int item = buffer.remove();
        System.out.println(Thread.currentThread().getName() + ": Consumed " + item + ". Buffer size: " + buffer.size());
        notifyAll(); // Notify producers that space is available
        return item;
    }
}
