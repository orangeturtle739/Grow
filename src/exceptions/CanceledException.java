package exceptions;

/**
 * Represents: the user canceling an edit.
 *
 * @author Jacob Glueck
 *
 */
public class CanceledException extends GrowException {

	/**
	 * Default UID.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String errorMessage() {
		return "Please tell the developer about this. You should never see this message.";
	}
}
