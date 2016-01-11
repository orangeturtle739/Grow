package exceptions;

/**
 * Represents: an exception caused during a grow game.
 *
 * @author Jacob Glueck
 *
 */
public abstract class GrowException extends Exception {

	/**
	 * Default UID.
	 */
	private static final long serialVersionUID = 1L;

	public abstract String errorMessage();

	@Override
	public String getMessage() {
		return errorMessage();
	}

}
