package exceptions;

/**
 * Thrown when a scene already exists.
 * 
 * @author Jacob Glueck
 *
 */
public class SceneExists extends GrowException {

	/**
	 * Default UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the scene that already exists.
	 */
	private final String sceneName;

	/**
	 * Creates: a new exception that indicates that the scene with the specified
	 * name already exists.
	 *
	 * @param sceneName
	 *            the scene name
	 */
	public SceneExists(String sceneName) {
		this.sceneName = sceneName;
	}

	@Override
	public String errorMessage() {
		return sceneName + " already exists.";
	}
}