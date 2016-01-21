package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import grow.Game;
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
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		output.println("Scene: " + current.name());
		output.println("Description: " + current.description());
		Util.printNumberedList("", ".", 0, 5, output, current.rules().stream().map(r -> Util.prettyRule(r)).collect(Collectors.toList()));
		return current;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}
}
