package grow.action;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

public class EditPattern extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		int num = Util.getRuleNumber(current, world, input, output, "What rule would you like to edit?");
		String choice = "";
		while (!choice.equalsIgnoreCase("A") && !choice.equalsIgnoreCase("R")) {
			output.println("Would you like to (A)dd or (R)emove patterns?");
			choice = input.nextLine();
		}
		if (choice.equalsIgnoreCase("A")) {
			output.println("Enter patterns, followed by a blank line:");
			while (true) {
				String newPattern = input.nextLine();
				if (newPattern.length() == 0) {
					break;
				} else {
					current.rules().get(num).addPattern(newPattern);
				}
			}
		} else {
			Iterator<String> removeIter = current.rules().get(num).patterns().iterator();
			while (removeIter.hasNext()) {
				output.println("Remove " + removeIter.next() + " (y/n)?");
				if (input.nextLine().toUpperCase().startsWith("Y")) {
					removeIter.remove();
				}
			}
		}
		output.println("Pattern edit complete: " + current.rules().get(num));
		return new Go(current.name()).act(current, world, input, output);
	}

	@Override
	public char commandPrefix() {
		return 'e';
	}

}
