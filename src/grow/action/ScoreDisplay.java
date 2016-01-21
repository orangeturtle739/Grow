package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action which prints out the score of the current user
 * 
 * @author Jacob Glueck
 */
public class ScoreDisplay extends Action {

	/**
	 * The prefix for a score action
	 */
	public static final char PREFIX = 's';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		output.println("Your score is: " + world.score().score());
		return current;
	}

	@Override
	public char commandPrefix() {
		return 's';
	}

}
