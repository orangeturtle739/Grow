package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

import grow.Game;
import grow.Scene;

/**
 * Represents: a quit action
 *
 * @author Jacob Glueck
 */
public class Quit extends Action {

	/**
	 * The prefix
	 */
	public static final char PREFIX = 'q';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		output.println("Score: " + world.score());
		// Null means that the game is over!
		return null;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

}
