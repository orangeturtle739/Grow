package grow.action;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that allows the user to edit the actions of a single
 * rule from a single scene.
 * 
 * @author Jacob Glueck
 *
 */
public class EditAction extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		int num = Util.getRuleNumber(current, world, input, output, "What rule would you like to edit?");
		String choice = "";
		while (!choice.equalsIgnoreCase("A") && !choice.equalsIgnoreCase("R")) {
			output.println("Would you like to (A)dd or (R)emove actions?");
			choice = input.nextLine();
		}
		if (choice.equalsIgnoreCase("A")) {
			output.println("Enter actions, followed by a blank line:");
			while (true) {
				String newAction = input.nextLine();
				if (newAction.length() == 0) {
					break;
				} else {
					Action a = Action.parseAction(newAction);
					if (a == null) {
						output.println("Bad action");
					} else {
						current.rules().get(num).addAction(a);
					}
				}
			}
		} else {
			Iterator<Action> removeIter = current.rules().get(num).actions().iterator();
			while (removeIter.hasNext()) {
				output.println("Remove " + removeIter.next() + " (y/n)?");
				if (input.nextLine().toUpperCase().startsWith("Y")) {
					removeIter.remove();
				}
			}
		}
		output.println("Action edit complete: " + current.rules().get(num));
		return new Go(current.name()).act(current, world, input, output);
	}

	@Override
	public char commandPrefix() {
		return 'a';
	}

}
