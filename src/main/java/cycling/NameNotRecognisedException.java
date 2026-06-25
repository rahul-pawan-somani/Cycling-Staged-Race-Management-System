package cycling;

/**
 * Thrown when attempting to use a name that does not exist in the
 * system.
 * 
 * @author Diogo Pacheco
 * @version 1.0
 *
 */
public class NameNotRecognisedException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of the exception with no message
	 */
	public NameNotRecognisedException() {
		// do nothing
	}

	/**
	 * Constructs an instance of the exception containing the message argument
	 * 
	 * @param message message containing details regarding the exception cause
	 */
	public NameNotRecognisedException(String message) {
		super(message);
	}

}
