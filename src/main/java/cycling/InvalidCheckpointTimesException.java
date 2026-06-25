package cycling;

/**
 * Each stage result contains the times for every checkpoint, plus the start
 * and finish times. The times must be in chronological order, i.e.,
 * checkpoint_i {@literal <=} checkpoint_i+1.
 * 
 * @author Diogo Pacheco
 * @version 2.0
 *
 */
public class InvalidCheckpointTimesException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of the exception with no message
	 */
	public InvalidCheckpointTimesException() {
		// do nothing
	}

	/**
	 * Constructs an instance of the exception containing the message argument
	 * 
	 * @param message message containing details regarding the exception cause
	 */
	public InvalidCheckpointTimesException(String message) {
		super(message);
	}

}
