package grow.action;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import exceptions.CanceledException;
import grow.Game;
import grow.Scene;

/**
 * Represents: an action which allows the user to edit rules.
 *
 * @author Jacob Glueck
 */
public class Edit extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		return Util.handleCancel(current, output, () -> {
			int num = Util.getRuleNumber("What rule would you like to edit?", output, input, world);
			output.println("What would you like to do?");
			List<String> choices = Arrays.asList("Edit patterns", "Edit actions", "Reorder actions");
			Util.printNumberedList("", ".", 0, 5, output, choices);
			// Subtract 1 because the numbering starts at 1.
			int option = Util.readInt(output, input, "", "Not a valid choice", 0, choices.size()) - 1;
			switch (option) {
			case 0:
				editPatterns(current, world, input, output, num);
				break;
			case 1:
				editActions(current, world, input, output, num);
				break;
			case 2:
				reorderActions(current, world, input, output, num);
				break;
			}
			output.println("Edit complete.");
			return current;
		});
	}

	/**
	 * Allows the user to remove and add patterns.
	 *
	 * @param current
	 *            the current scene
	 * @param world
	 *            the world
	 * @param input
	 *            the input
	 * @param output
	 *            the output
	 * @param index
	 *            the index of the rule to edit
	 * @throws CanceledException
	 *             if the user cancels the edit
	 */
	private static void editPatterns(Scene current, Game world, Scanner input, PrintStream output, int index) throws CanceledException {
		output.println("Which patterns would you like to remove? Enter a list of space-separated integers.");
		Util.printNumberedList("", ".", 0, 5, output, world.current().rules().get(index).patterns());
		Set<Integer> toRemove = new HashSet<>(Util.readInts(output, input, "Bad list!", 1, world.current().rules().get(index).patterns().size()));
		int count = 1;
		Iterator<String> patterns = world.current().rules().get(index).patterns().iterator();
		while (patterns.hasNext()) {
			patterns.next();
			if (toRemove.contains(count)) {
				patterns.remove();
			}
			count++;
		}
		world.current().rules().get(index).patterns().addAll(Util.readList(output, input, "Patterns to add: ", "Bad pattern!", (s) -> s, "List error?", (l) -> true));
	}

	/**
	 * Allows the user to remove and add actions
	 *
	 * @param current
	 *            the current scene
	 * @param world
	 *            the world
	 * @param input
	 *            the input
	 * @param output
	 *            the output
	 * @param index
	 *            the index of the rule to edit
	 * @throws CanceledException
	 *             if the user cancels the edit
	 */
	private static void editActions(Scene current, Game world, Scanner input, PrintStream output, int index) throws CanceledException {
		output.println("Which actions would you like to remove? Enter a list of space-separated integers.");
		Util.printNumberedList("", ".", 0, 5, output, world.current().rules().get(index).actions());
		Set<Integer> toRemove = new HashSet<>(Util.readInts(output, input, "Bad list!", 1, world.current().rules().get(index).actions().size()));
		int count = 1;
		Iterator<Action> patterns = world.current().rules().get(index).actions().iterator();
		while (patterns.hasNext()) {
			patterns.next();
			if (toRemove.contains(count)) {
				patterns.remove();
			}
			count++;
		}
		world.current().rules().get(index).actions().addAll(Util.readList(output, input, "Actions to add: ", "Bad pattern!", Util.actionConverter, "List error?", (l) -> true));
	}

	/**
	 * Allows the user to change the order of the actions
	 *
	 * @param current
	 *            the current scene
	 * @param world
	 *            the world
	 * @param input
	 *            the input stream
	 * @param output
	 *            the output stream
	 * @param index
	 *            the index of the rule to edit
	 * @throws CanceledException
	 *             if the user cancels the edit
	 */
	private static void reorderActions(Scene current, Game world, Scanner input, PrintStream output, int index) throws CanceledException {
		output.println("Enter the new action order as a list of space-separated integers.");
		Util.printNumberedList("", ".", 0, 5, output, world.current().rules().get(index).actions());
		List<Integer> order = Util.readInts(output, input, "Bad list!", 1, world.current().rules().get(index).actions().size(), world.current().rules().get(index).actions().size());
		ArrayList<Action> newActions = new ArrayList<>(world.current().rules().get(index).actions().size());
		for (Integer i : order) {
			newActions.add(world.current().rules().get(index).actions().get(i - 1));
		}
		world.current().rules().get(index).actions().clear();
		world.current().rules().get(index).actions().addAll(newActions);
	}

	@Override
	public char commandPrefix() {
		return 'e';
	}

}
