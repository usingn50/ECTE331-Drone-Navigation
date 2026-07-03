package ECTE331ThreadsSynchronisation;

/**
 * Main class to demonstrate the Producer-Consumer problem using ReentrantLock and Condition.
 */
public class ProducerConsumerLock {

    public static void main(String[] args) {
        System.out.println("--- Producer-Consumer Simulation (ReentrantLock/Condition) Started ---");

        SharedBufferLock buffer = new SharedBufferLock(5);
        ProducerLock producer = new ProducerLock(buffer, 10);
        ConsumerLock consumer = new ConsumerLock(buffer, 10);

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted.");
        }

        System.out.println("--- Producer-Consumer Simulation (ReentrantLock/Condition) Finished ---");
    }
}
