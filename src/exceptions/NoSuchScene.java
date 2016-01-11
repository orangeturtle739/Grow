package exceptions;

/**
 * Represents: an error in which the desired scene does not exist.
 *
 * @author Jacob Glueck
 *
 */
public class NoSuchScene extends GrowException {

	/**
	 * Default UID.
	 */
	private static final long serialVersionUID = 8990928125883512719L;

	/**
	 * The name of the scene.
	 */
	private final String sceneName;

	/**
	 * Creates: a new error when the specified scene could not be found.
	 * 
	 * @param name
	 *            the name of the scene.
	 */
	public NoSuchScene(String name) {
		sceneName = name;
	}

	@Override
	public String errorMessage() {
		return "No such scene: " + sceneName;
	}

}
