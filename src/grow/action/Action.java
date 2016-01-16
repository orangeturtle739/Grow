package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that can be taken by a grow scene.
 */
public abstract class Action {

	/**
	 * Effect: executes the specified action on the current scene, and appends
	 * output to {@code output}. The output must end with a line break.
	 *
	 * @param current
	 *            the current scene
	 * @param world
	 *            the world in which the action is occurring
	 * @param input
	 *            the input from the user
	 * @param output
	 *            the output from performing the action
	 * @return the scene after the action. If the scene is null, the game is
	 *         over.
	 */
	public abstract Scene act(Scene current, Game world, Scanner input, PrintStream output);

	/**
	 * @return the prefix that identifies this command.
	 */
	public abstract char commandPrefix();

	/**
	 * @return the body of this command. Returns an empty string by default.
	 */
	public String commandBody() {
		return "";
	}

	/**
	 * @return the command string (the prefix and body).
	 */
	public String commandString() {
		return commandPrefix() + commandBody();
	}

	@Override
	public String toString() {
		return commandString();
	}

	/**
	 * Parses an action from an action string. Assumes a print action if no
	 * prefix.
	 *
	 * @param action
	 *            the action string to parse
	 * @return the action, or null if unable to parse
	 */
	public static Action parseAction(String action) {
		if (action.length() == 0) {
			return null;
		} else {
			switch (action.charAt(0)) {
			case Print.PREFIX:
				return new Print(action.substring(1));
			case Extend.PREFIX:
				return new Extend();
			case ScoreChange.POSITIVE:
				int up = tryParse(action.substring(1));
				if (up >= 0) {
					return new ScoreChange(up);
				} else {
					break;
				}
			case ScoreChange.NEGATIVE:
				int down = tryParse(action.substring(1));
				if (down >= 0) {
					return new ScoreChange(-down);
				} else {
					break;
				}
			case Go.PREFIX:
				return new Go(action.substring(1));
			case Quit.PREFIX:
				return new Quit();
			case Restart.PREFIX:
				return new Restart();
			case View.PREFIX:
				return new View();
			default:
				// If no prefix, assume print
				return new Print(action);
			}
		}
		return null;
	}

	/**
	 * Tries to parse a positive integer from the string. Returns a negative
	 * number if failed.
	 *
	 * @param str
	 *            the string
	 * @return if >= 0, the number. If < 0, the parse failed.
	 */
	private static int tryParse(String str) {
		try {
			int result = Integer.parseInt(str);
			if (result >= 0) {
				return result;
			}
		} catch (NumberFormatException e) {

		}
		return -1;
	}
}
