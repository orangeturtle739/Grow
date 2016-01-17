package grow;

/**
 * Represents: a way for the grow game to share its status
 *
 * @author Jacob Glueck
 */
public interface StatusUpdater {
	/**
	 * Effect: updates the current status display using the provided scene name
	 * and adventure name.
	 * 
	 * @param adventureName
	 *            the adventure name
	 * @param sceneName
	 *            the scene name
	 */
	void update(String adventureName, String sceneName);
}
