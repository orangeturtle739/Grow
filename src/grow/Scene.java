package grow;

import java.io.PrintStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import grow.action.Action;
import javafx.scene.image.Image;

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
	 * The image file for this scene.
	 */
	private Image image;
	/**
	 * True if the image has been changed since the last call to
	 * {@link #clearImageChanged()}.
	 */
	private boolean imageChanged;

	/**
	 * The URI for the sound for this scene.
	 */
	private URI sound;
	/**
	 * True if the sound has been changed since the last call to
	 * {@link #clearSoundChanged()}.
	 */
	private boolean soundChanged;

	/**
	 * Creates: a new scene with no actions with the specified name and an empty
	 * action map.
	 *
	 * @param name
	 *            the name of this scene.
	 * @param description
	 *            the text to be displayed when the user first enters the room
	 * @param image
	 *            the image for this scene. If null, there is no image for this
	 *            scene.
	 */
	public Scene(String name, String description, Image image) {
		clearSoundChanged();
		clearImageChanged();
		this.name = name;
		rules = new LinkedList<>();
		this.description = description;
		setImage(image);
		setSound(null);
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
	 * @param image
	 *            the image file
	 */
	public void setImage(Image image) {
		if (image != null || this.image != null) {
			this.image = image;
			imageChanged = true;
		}
	}

	/**
	 * @return the image file
	 */
	public Image image() {
		return image;
	}

	/**
	 * @return true if the image has changed since the last call to
	 *         {@link #clearImageChanged()}.
	 */
	public boolean imageChanged() {
		return imageChanged;
	}

	/**
	 * Clears the sound changed status
	 */
	public void clearImageChanged() {
		imageChanged = false;
	}

	/**
	 * Effect: sets the sound URI to the specified URI.
	 *
	 * @param uri
	 *            the uri
	 */
	public void setSound(URI uri) {
		if (sound != null || uri != null) {
			sound = uri;
			soundChanged = true;
		}
	}

	/**
	 * @return the URI for the sound file, or null if there is not one.
	 */
	public URI sound() {
		return sound;
	}

	/**
	 * @return true if the sound has changed since the last call to
	 *         {@link #clearSoundChanged()}.
	 */
	public boolean soundChanged() {
		return soundChanged;
	}

	/**
	 * Clears the sound changed status
	 */
	public void clearSoundChanged() {
		soundChanged = false;
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
