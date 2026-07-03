package ECTE331ThreadsSynchronisation;

/**
 * Main class to demonstrate the Producer-Consumer problem using synchronized methods and wait/notify.
 */
public class ProducerConsumerSync {

    public static void main(String[] args) {
        System.out.println("--- Producer-Consumer Simulation (synchronized) Started ---");

        SharedBuffer buffer = new SharedBuffer(5);
        Producer producer = new Producer(buffer, 10);
        Consumer consumer = new Consumer(buffer, 10);

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted.");
        }

        System.out.println("--- Producer-Consumer Simulation (synchronized) Finished ---");
    }
}
