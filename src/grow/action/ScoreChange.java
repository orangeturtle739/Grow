package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action which changes the player's score
 *
 * @author Jacob Glueck
 *
 */
public class ScoreChange extends Action {

	/**
	 * The positive sign used to prefix a positive score change.
	 */
	public static final char POSITIVE = '+';
	/**
	 * The negative sign used to prefix a negative score change.
	 */
	public static final char NEGATIVE = '-';

	/**
	 * The amount to change the score by
	 */
	private final int change;

	/**
	 * Creates: a new score change action which changes the score by the
	 * specified amount
	 *
	 * @param change
	 *            the amount to change the score by
	 */
	public ScoreChange(int change) {
		this.change = change;
	}

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		world.score().increment(change);
		output.println((change >= 0 ? POSITIVE : NEGATIVE) + " " + Math.abs(change));
		return current;
	}

	@Override
	public char commandPrefix() {
		if (change >= 0) {
			return POSITIVE;
		} else {
			return NEGATIVE;
		}
	}

	@Override
	public String commandBody() {
		return Integer.toString(Math.abs(change));
	}
}