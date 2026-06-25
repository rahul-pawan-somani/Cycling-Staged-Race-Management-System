package cycling;

/**
 * Each rider can only have a single result in a stage. This exception is thrown
 * when attempting to create another record for the same rider in the same
 * stage.
 * 
 * @author Diogo Pacheco
 * @version 1.0
 *
 */
public class DuplicatedResultException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of the exception with no message
	 */
	public DuplicatedResultException() {
		// do nothing
	}

	/**
	 * Constructs an instance of the exception containing the message argument
	 * 
	 * @param message message containing details regarding the exception cause
	 */
	public DuplicatedResultException(String message) {
		super(message);
	}

}
