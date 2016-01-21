package grow.action;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

import exceptions.CanceledException;
import grow.Game;
import grow.Scene;

/**
 * Represents: an action which allows the user to edit rules.
 *
 * @author Jacob Glueck
 */
public class Edit extends Action {

	/**
	 * Used to indicate no action
	 */
	private static final Action noAction = new Action() {
		@Override
		public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
			return null;
		}
	};

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		return Util.handleCancel(current, output, () -> {
			int num = Util.getRuleNumber("What rule would you like to edit?", output, input, world);
			output.println("What would you like to do?");
			List<String> choices = Arrays.asList("Edit patterns", "Edit actions", "Reorder actions");
			Util.printNumberedList("", ".", 0, 5, output, choices);
			// Subtract 1 because the numbering starts at 1.
			int option = Util.readInt(output, input, "", "Not a valid choice", 0, choices.size()) - 1;
			switch (option) {
			case 0:
				editPatterns(current, world, input, output, injector, num);
				break;
			case 1:
				editActions(current, world, input, output, injector, num);
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
	 * @param injector
	 *            the injector used to prompt the user
	 * @param index
	 *            the index of the rule to edit
	 * @throws CanceledException
	 *             if the user cancels the edit
	 */
	private static void editPatterns(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector, int index) throws CanceledException {
		// Make sure there are some patterns
		while (world.current().rules().get(index).patterns().size() != 0) {
			output.println("Which pattern would you like to edit? (Hit enter if you do not want to edit.)");
			Util.printNumberedList("", ".", 0, 5, output, world.current().rules().get(index).patterns());
			List<Integer> toEdit = Util.readInts(output, input, "Bad pattern number.", 1, world.current().rules().get(index).patterns().size(), 0, 1);
			if (!toEdit.isEmpty()) {
				// Subtract 1 because we have to edit
				String pattern = getAtIndex(world.current().rules().get(index).patterns(), toEdit.get(0) - 1);
				injector.accept(pattern);
				String newPattern = Util.read(output, input, "Editing: " + pattern, "Bad pattern.", (s) -> s);
				world.current().rules().get(index).patterns().remove(pattern);
				if (newPattern.length() != 0) {
					world.current().rules().get(index).patterns().add(newPattern);
					output.println("Changed \"" + pattern + "\" to \"" + newPattern + "\".");
				} else {
					output.println("Removed: " + pattern);
				}
			} else {
				break;
			}
		}
		world.current().rules().get(index).patterns().addAll(Util.readList(output, input, "Enter patterns to add: ", "Bad pattern!", (s) -> s, "List error.", (l) -> true));
	}

	/**
	 * Effect: gets the element at the specified index from the set. The set is
	 * ordered based on its iterator.
	 *
	 * @param <T>
	 *            the type of things in the set
	 * @param things
	 *            the things
	 * @param index
	 *            the index
	 * @return the element
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds
	 */
	private static <T> T getAtIndex(Set<T> things, int index) {
		int count = 0;
		for (T thing : things) {
			if (index == count) {
				return thing;
			} else {
				count++;
			}
		}
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + things.size());
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
	 * @param injector
	 *            the injector for prompting the user
	 * @param index
	 *            the index of the rule to edit
	 * @throws CanceledException
	 *             if the user cancels the edit
	 */
	private static void editActions(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector, int index) throws CanceledException {
		// Make sure there are some patterns
		while (world.current().rules().get(index).actions().size() != 0) {
			output.println("Which action would you like to edit? (Hit enter if you do not want to edit.)");
			Util.printNumberedList("", ".", 0, 5, output, world.current().rules().get(index).actions());
			List<Integer> toEdit = Util.readInts(output, input, "Bad action number.", 1, world.current().rules().get(index).actions().size(), 0, 1);
			if (!toEdit.isEmpty()) {
				// Subtract 1 because we have to edit
				int actionIndex = toEdit.get(0) - 1;
				Action action = world.current().rules().get(index).actions().get(actionIndex);
				injector.accept(action.toString());
				Action newAction = Util.read(output, input, "Editing: " + action, "Bad action.", (str) -> {
					if (str.length() == 0) {
						return noAction;
					} else {
						return Util.actionConverter.apply(str);
					}
				});
				world.current().rules().get(index).actions().remove(actionIndex);
				if (newAction != noAction) {
					world.current().rules().get(index).actions().add(newAction);
					output.println("Changed \"" + action + "\" to \"" + newAction + "\".");
				} else {
					output.println("Removed: " + action);
				}
			} else {
				break;
			}
		}
		world.current().rules().get(index).actions().addAll(Util.readList(output, input, "Actions to add: ", "Bad pattern!", Util.actionConverter, "List error.", (l) -> true));
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
}
