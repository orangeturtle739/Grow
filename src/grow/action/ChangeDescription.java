package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that changes the description of a scene
 *
 * @author Jacob Glueck
 *
 */
public class ChangeDescription extends Action {

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		return Util.handleCancel(current, output, () -> {
			output.printf("The current description for the scene \"%s\" is: %s", current.name(), current.description());
			output.println();
			String description = Util.read(output, input, "What would you like the new description to be?", "Bad description", (s) -> s);
			current.setDescription(description);
			output.println("Description set.");
			// Re-enter the room with the new description
			return new Go(current.name()).act(current, world, input, output);
		});
	}

	@Override
	public char commandPrefix() {
		return 'd';
	}

}
