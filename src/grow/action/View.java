package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Rule;
import grow.Scene;

public class View extends Action {

	public static final char PREFIX = 'L';

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		int count = 1;
		output.println("Scene: " + current.name());
		for (Rule r : current.rules()) {
			output.printf("%-5s %s", Integer.toString(count++), r.toString());
			output.println();
		}
		return current;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

}
