package grow.action;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

public class EditActionOrder extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		int rule = Util.getRuleNumber(current, world, input, output, "What rule would you like to edit?");
		int count = 1;
		for (Action a : current.rules().get(rule).actions()) {
			output.printf("%-5s %s", Integer.toString(count++), a);
			output.println();
		}
		output.println("What would you like the new order to be? Enter a list of space-seperated numbers.");
		List<Integer> order = new ArrayList<>();
		while (order.size() != current.rules().get(rule).actions().size()) {
			String[] split = input.nextLine().split("\\s");
			for (String element : split) {
				int num;
				try {
					num = Integer.parseInt(element);
				} catch (NumberFormatException e) {
					num = -1;
				}
				if (num < 1 || num > current.rules().get(rule).actions().size()) {
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
		List<Action> newActions = new ArrayList<>(current.rules().get(rule).actions().size());
		for (int x = 0; x < current.rules().get(rule).actions().size(); x++) {
			newActions.add(current.rules().get(rule).actions().get(order.get(x)));
		}
		// TODO this could be more efficient.
		current.rules().get(rule).actions().clear();
		current.rules().get(rule).actions().addAll(newActions);
		output.print("Order changed: ");
		output.println(current.rules().get(rule));
		return new Go(current.name()).act(current, world, input, output);
	}

	@Override
	public char commandPrefix() {
		return 'o';
	}
}
