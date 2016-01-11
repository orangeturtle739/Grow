package exceptions;

/**
 * Represents: a syntax error from parsing a file.
 *
 * @author Jacob Glueck
 *
 */
public class SyntaxError extends GrowException {

	/**
	 * Default UID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The line number
	 */
	private final int line;
	/**
	 * The error description.
	 */
	private final String error;

	/**
	 * Creates: a new syntax error with the specified information.
	 *
	 * @param line
	 *            the line number
	 * @param error
	 *            the error
	 */
	public SyntaxError(int line, String error) {
		this.line = line;
		this.error = error;
	}

	@Override
	public String errorMessage() {
		return "Syntax error. Line: " + line + ". Problem: " + error;
	}

}
