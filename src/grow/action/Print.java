package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * Represents: a print action, which prints text.
 *
 * @author Jacob Glueck
 */
public class Print extends Action {

	/**
	 * The prefix
	 */
	public static final char PREFIX = 'p';

	/**
	 * The text this print action prints
	 */
	private final String toPrint;

	/**
	 * Creates: a new print action that prints the specified text.
	 *
	 * @param toPrint
	 *            the text to print.
	 */
	public Print(String toPrint) {
		this.toPrint = toPrint;
	}

	/**
	 * @return the text this print action will print.
	 */
	public String toPrint() {
		return toPrint;
	}

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		output.println(toPrint);
		return current;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

	@Override
	public String commandBody() {
		return toPrint;
	}
}