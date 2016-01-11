package grow.action;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
public class EditOrder extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		current = new View().act(current, world, input, output);
		output.println("What would you like the new order to be? Enter a list of space-seperated numbers.");
		List<Integer> order = new ArrayList<>();
		while (order.size() != current.rules().size()) {
			String[] split = input.nextLine().split("\\s");
			for (String element : split) {
				int num;
				try {
					num = Integer.parseInt(element);
				} catch (NumberFormatException e) {
					num = -1;
				}
				if (num < 1 || num > current.rules().size()) {
					num = -1;
				}
				if (num != -1) {
					// The print out starts counting at 1
					order.add(num - 1);
				} else {
					order.clear();
					output.println("Bad order!");
					break;
				}
			}
		}
		List<Rule> newRules = new ArrayList<>(current.rules().size());
		for (int x = 0; x < current.rules().size(); x++) {
			newRules.add(current.rules().get(order.get(x)));
		}
		// TODO this could be more efficient.
		current.rules().clear();
		current.rules().addAll(newRules);
		output.println("Order changed: ");
		current = new View().act(current, world, input, output);
		return new Go(current.name()).act(current, world, input, output);
	}

	@Override
	public char commandPrefix() {
		return 'o';
	}

}
