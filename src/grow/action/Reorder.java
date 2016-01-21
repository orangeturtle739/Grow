package grow.action;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import grow.Game;
import grow.Rule;
import grow.Scene;

/**
 * Represents: an action that allows the user to edit the order of rules from a
 * single scene.
 *
 * @author Jacob Glueck
 *
 */
public class Reorder extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		return Util.handleCancel(current, output, () -> {
			output.println("Enter the new rule order as a list of space-seperated integers.");
			new View().act(current, world, input, output, injector);
			List<Integer> order = Util.readInts(output, input, "Bad list!", 1, world.current().rules().size(), world.current().rules().size());
			ArrayList<Rule> newRules = new ArrayList<>(world.current().rules().size());
			for (Integer i : order) {
				newRules.add(world.current().rules().get(i - 1));
			}
			world.current().rules().clear();
			world.current().rules().addAll(newRules);
			output.println("Reorder complete.");
			return new Go(current.name()).act(current, world, input, output, injector);
		});
	}
}
