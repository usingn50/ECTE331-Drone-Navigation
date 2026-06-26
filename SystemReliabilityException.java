/**
 * Custom exception thrown when the system enters SAFE MODE due to reliability failure.
 */
public class SystemReliabilityException extends Exception {
    /**
     * Constructs a new SystemReliabilityException with the specified detail message.
     * @param message the detail message.
     */
    public SystemReliabilityException(String message) {
        super(message);
    }
}
