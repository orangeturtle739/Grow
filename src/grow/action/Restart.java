package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

public class Restart extends Action {

	public static final char PREFIX = 'r';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		world.restart();
		// Go to the starting stage.
		return new Go(world.current().name()).act(null, world, input, output);
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

}
