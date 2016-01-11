package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that allows the user to remove a single rule from a
 * scene.
 * 
 * @author Jacob Glueck
 *
 */
public class RemoveRule extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		int num = Util.getRuleNumber(current, world, input, output, "What rule would you like to remove?");
		current.rules().remove(num);
		output.println("Done.");
		return new Go(current.name()).act(current, world, input, output);
	}

	@Override
	public char commandPrefix() {
		return 'r';
	}

}
