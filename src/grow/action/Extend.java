package grow.action;

import java.io.PrintStream;
import java.util.HashSet;
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
		return Util.handleCancel(current, output, () -> {
			Set<String> toMatch = new HashSet<>();
			toMatch.addAll(Util.readList(output, input, "Patterns to match (case insensitive, regex supported): ", "Bad pattern!", (s) -> s, "List error?", (l) -> true));
			List<Action> a = Util.readList(output, input, "Actions: ", "Bad action!", Util.actionConverter, "List error?", (l) -> true);
			current.rules().add(new Rule(a, toMatch));
			output.println("Extension complete.");
			return new Go(current.name()).act(current, world, input, output);
		});
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}
}
