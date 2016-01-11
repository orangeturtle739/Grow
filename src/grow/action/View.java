package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Rule;
import grow.Scene;

/**
 * Represents: an action that allows the user to look at all the rules defined
 * for the current scene.
 *
 * @author Jacob Glueck
 *
 */
public class View extends Action {

	/**
	 * The prefix
	 */
	public static final char PREFIX = 'l';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		int count = 1;
		output.println("Scene: " + current.name());
		for (Rule r : current.rules()) {
			output.printf("%-5s %s", Integer.toString(count++), r.toString());
			output.println();
		}
		return current;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

}
