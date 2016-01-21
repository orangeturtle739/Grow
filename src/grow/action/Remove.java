package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that allows the user to remove a single rule from a
 * scene.
 *
 * @author Jacob Glueck
 *
 */
public class Remove extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		return Util.handleCancel(current, output, () -> {
			current.rules().remove(Util.getRuleNumber("What rule would you like to remove?", output, input, world));
			output.println("Done.");
			return new Go(current.name()).act(current, world, input, output, injector);
		});
	}

	@Override
	public char commandPrefix() {
		return 'r';
	}

}
