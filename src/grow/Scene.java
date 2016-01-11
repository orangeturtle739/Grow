package grow;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import grow.action.Action;

/**
 * Represents: a scene in grow
 *
 * @author Jacob Glueck
 *
 */
public class Scene {

	/**
	 * The name of this scene.
	 */
	private final String name;

	/**
	 * The text to be displayed when the user enters this room.
	 */
	private String description;

	/**
	 * The action map for this scene
	 */
	private final List<Rule> rules;

	/**
	 * The image file for this scene
	 */
	private File imageFile;

	/**
	 * Creates: a new scene with no actions with the specified name and an empty
	 * action map.
	 *
	 * @param name
	 *            the name of this scene.
	 * @param description
	 *            the text to be displayed when the user first enters the room
	 * @param imageFile
	 *            the file with an image for this scene. If null, there is no
	 *            image for this scene.
	 */
	public Scene(String name, String description, File imageFile) {
		this.name = name;
		rules = new LinkedList<>();
		this.description = description;
		this.imageFile = imageFile;
	}

	/**
	 * Creates: a new scene with no actions with the specified name and no
	 * image.
	 *
	 * @param name
	 *            the name of this scene.
	 * @param description
	 *            the text to be displayed when the user first enters the room
	 */
	public Scene(String name, String description) {
		this(name, description, null);
	}

	/**
	 * Effect: sets the image file to the specified image
	 *
	 * @param imageFile
	 *            the image file
	 */
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * @return the image file
	 */
	public File imageFile() {
		return imageFile;
	}

	/**
	 * @return the name of this scene.
	 */
	public String name() {
		return name;
	}

	/**
	 * @return the text to be displayed when the user first enters the room.
	 */
	public String description() {
		return description;
	}

	/**
	 * Effect: sets the description of this scene to {@code d}.
	 *
	 * @param d
	 *            the new description
	 */
	public void setDescription(String d) {
		description = d;
	}

	/**
	 * Determines the action that should occur based on the user input. Returns
	 * null if no actions match.
	 *
	 * @param input
	 *            the input.
	 * @return the action that should occur, or null if no action would occur.
	 */
	public List<Action> act(String input) {
		for (Rule r : rules) {
			if (r.matches(input)) {
				return r.toDo();
			}
		}
		return null;
	}

	/**
	 * @return an unmodifiable view of the action map.
	 */
	public List<Rule> rules() {
		return rules;
	}

	/**
	 * Effect: saves this to the specified output stream.
	 *
	 * @param ps
	 *            the output stream.
	 */
	public void save(PrintStream ps) {
		ps.print("Name: ");
		ps.println(name);
		ps.print("Description: ");
		ps.println(description);
		for (Rule r : rules) {
			ps.println(r);
		}
	}
}
