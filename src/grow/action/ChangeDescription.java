package grow.action;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

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
	public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
		return Util.handleCancel(current, output, () -> {
			injector.accept(current.description());
			String description = Util.read(output, input, "What would you like the new description for scene \"" + current.name() + "\" to be?", "Bad description", (s) -> s);
			current.setDescription(description);
			output.println("Description set.");
			// Re-enter the room with the new description
			return new Go(current.name()).act(current, world, input, output, injector);
		});
	}

	@Override
	public char commandPrefix() {
		return 'd';
	}

}
