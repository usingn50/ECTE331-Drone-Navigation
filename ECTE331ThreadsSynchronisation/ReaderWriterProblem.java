package ECTE331ThreadsSynchronisation;

/**
 * Main class to demonstrate the Reader-Writer problem.
 */
public class ReaderWriterProblem {

    public static void main(String[] args) {
        System.out.println("--- Reader-Writer Problem Simulation Started ---");

        SharedData sharedData = new SharedData();

        // Create multiple readers and writers
        Reader reader1 = new Reader(sharedData, 5);
        Reader reader2 = new Reader(sharedData, 5);
        Writer writer1 = new Writer(sharedData, 3);
        Reader reader3 = new Reader(sharedData, 5);
        Writer writer2 = new Writer(sharedData, 3);

        // Start threads
        reader1.start();
        reader2.start();
        writer1.start();
        reader3.start();
        writer2.start();

        try {
            reader1.join();
            reader2.join();
            writer1.join();
            reader3.join();
            writer2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted.");
        }

        System.out.println("--- Reader-Writer Problem Simulation Finished ---");
    }
}
