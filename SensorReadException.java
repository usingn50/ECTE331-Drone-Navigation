import java.io.IOException;

/**
 * Custom exception representing a sensor failure.
 */
public class SensorReadException extends IOException {
    /**
     * Constructs a new SensorReadException with the specified detail message.
     * @param message the detail message.
     */
    public SensorReadException(String message) {
        super(message);
    }
}
