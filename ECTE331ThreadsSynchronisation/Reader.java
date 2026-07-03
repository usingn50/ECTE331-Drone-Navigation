package ECTE331ThreadsSynchronisation;

/**
 * Reader thread for the Reader-Writer problem.
 */
public class Reader extends Thread {
    private SharedData sharedData;
    private int readsToPerform;

    public Reader(SharedData sharedData, int readsToPerform) {
        this.sharedData = sharedData;
        this.readsToPerform = readsToPerform;
        setName("Reader-" + getId());
    }

    @Override
    public void run() {
        for (int i = 0; i < readsToPerform; i++) {
            try {
                sharedData.read();
                Thread.sleep((long) (Math.random() * 70)); // Simulate work after reading
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(getName() + ": Interrupted.");
            }
        }
        System.out.println(getName() + ": Finished reading.");
    }
}
