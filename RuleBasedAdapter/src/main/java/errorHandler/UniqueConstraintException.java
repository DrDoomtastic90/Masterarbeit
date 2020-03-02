package errorHandler;

public class UniqueConstraintException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;


	public UniqueConstraintException(String message) {
		super(message);
	}

}
