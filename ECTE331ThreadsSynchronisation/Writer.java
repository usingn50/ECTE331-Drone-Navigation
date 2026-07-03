package ECTE331ThreadsSynchronisation;

/**
 * Writer thread for the Reader-Writer problem.
 */
public class Writer extends Thread {
    private SharedData sharedData;
    private int writesToPerform;

    public Writer(SharedData sharedData, int writesToPerform) {
        this.sharedData = sharedData;
        this.writesToPerform = writesToPerform;
        setName("Writer-" + getId());
    }

    @Override
    public void run() {
        for (int i = 0; i < writesToPerform; i++) {
            try {
                sharedData.write(i + 1); // Write new data
                Thread.sleep((long) (Math.random() * 120)); // Simulate work after writing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(getName() + ": Interrupted.");
            }
        }
        System.out.println(getName() + ": Finished writing.");
    }
}
