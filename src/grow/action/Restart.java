package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that allows the user to go back to the first scene in
 * an adventure.
 *
 * @author Jacob Glueck
 *
 */
public class Restart extends Action {

	/**
	 * The prefix
	 */
	public static final char PREFIX = 'r';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		world.restart();
		// Go to the starting stage.
		return new Go(world.current().name()).act(null, world, input, output, injector);
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

}
