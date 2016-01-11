package grow.action;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import grow.Game;
import grow.Rule;
import grow.Scene;

/**
 * Represents: an action that allows the user to add a rule to a scene.
 *
 * @author Jacob Glueck
 *
 */
public class Extend extends Action {

	/**
	 * The action prefix
	 */
	public static final char PREFIX = 'x';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		output.println("Patterns to match (case insensitive, regex supported): ");
		Set<String> toMatch = new HashSet<>();
		String line;
		while ((line = input.nextLine()).length() > 0) {
			toMatch.add(line);
		}

		List<Action> a = getAction(input, output);
		current.rules().add(new Rule(a, toMatch));
		output.println("Extension complete.");
		return new Go(current.name()).act(current, world, input, output);
	}

	/**
	 * Prompts the user for a list of actions and reads them using the specified
	 * streams. Stops when the user enters a blank line. If the user enters a
	 * malformed actions, it ignores it.
	 *
	 * @param input
	 *            the input stream
	 * @param output
	 *            the output stream
	 * @return a list of actions
	 */
	private static List<Action> getAction(Scanner input, PrintStream output) {
		output.println("Actions:");
		List<Action> result = new LinkedList<>();
		while (true) {
			String line = input.nextLine();
			if (line.length() == 0) {
				break;
			}
			Action action = parseAction(line);
			if (action == null) {
				output.println("Bad action!");
			} else {
				result.add(action);
			}
		}
		return result;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}
}
