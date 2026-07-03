package ECTE331ThreadsSynchronisation;

/**
 * Consumer thread for the Producer-Consumer problem using ReentrantLock and Condition.
 */
public class ConsumerLock extends Thread {
    private SharedBufferLock buffer;
    private int itemsToConsume;

    public ConsumerLock(SharedBufferLock buffer, int itemsToConsume) {
        this.buffer = buffer;
        this.itemsToConsume = itemsToConsume;
        setName("ConsumerLock");
    }

    @Override
    public void run() {
        for (int i = 0; i < itemsToConsume; i++) {
            try {
                buffer.consume();
                Thread.sleep((long) (Math.random() * 150)); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(getName() + ": Interrupted.");
            }
        }
        System.out.println(getName() + ": Finished consuming.");
    }
}
