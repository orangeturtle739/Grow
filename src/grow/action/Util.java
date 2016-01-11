package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * A utility class, full of only static methods, that simplifies collecting user
 * input.
 *
 * @author Jacob Glueck
 *
 */
public class Util {
	/**
	 * Asks the user to pick a rule from the current scene.
	 *
	 * @param current
	 *            the current scene
	 * @param world
	 *            the world
	 * @param input
	 *            the input stream
	 * @param output
	 *            the output stream
	 * @param prompt
	 *            the prompt
	 * @return the index of the rule in the scenes rule list
	 */
	public static int getRuleNumber(Scene current, Game world, Scanner input, PrintStream output, String prompt) {
		current = new View().act(current, world, input, output);
		int num = -1;
		while (num < 1 || num > current.rules().size()) {
			output.println(prompt);
			String line = input.nextLine();
			try {
				num = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				num = -1;
			}
		}
		return num - 1;
	}
}
