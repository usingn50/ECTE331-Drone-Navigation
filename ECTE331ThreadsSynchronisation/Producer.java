package ECTE331ThreadsSynchronisation;

/**
 * Producer thread for the Producer-Consumer problem.
 */
public class Producer extends Thread {
    private SharedBuffer buffer;
    private int itemsToProduce;

    public Producer(SharedBuffer buffer, int itemsToProduce) {
        this.buffer = buffer;
        this.itemsToProduce = itemsToProduce;
        setName("Producer");
    }

    @Override
    public void run() {
        for (int i = 0; i < itemsToProduce; i++) {
            try {
                buffer.produce(i);
                Thread.sleep((long) (Math.random() * 100)); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(getName() + ": Interrupted.");
            }
        }
        System.out.println(getName() + ": Finished producing.");
    }
}
