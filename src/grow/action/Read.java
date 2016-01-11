package grow.action;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import exceptions.GrowException;
import grow.Game;
import grow.Scene;

/**
 * Represents: an action that reads the specified adventure and state files and
 * configures the world to use them.
 *
 * @author Jacob Glueck
 *
 */
public class Read extends Action {

	/**
	 * The prefix
	 */
	public static final char PREFIX = 'R';

	/**
	 * The file in which the state is saved
	 */
	private final InputStream stateFile;
	/**
	 * The file in which the adventure is saved
	 */
	private final InputStream adventureFile;

	/**
	 * Creates: a read action that reads that state form the specified file, and
	 * the adventure form the specified adventure file.
	 *
	 * @param stateFile
	 *            the state file
	 * @param adventureFile
	 *            the adventure file
	 */
	public Read(InputStream stateFile, InputStream adventureFile) {
		this.stateFile = stateFile;
		this.adventureFile = adventureFile;
	}

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		try {
			Scanner state = new Scanner(stateFile);
			Scanner adventure = new Scanner(adventureFile);
			world.loadGame(state, adventure);
			state.close();
			adventure.close();
		} catch (GrowException e) {
			output.printf("Problem with reading state (%s) or adventure (%s): %s", stateFile.toString(), adventureFile.toString(), e.getMessage());
			output.println();
		}
		return world.current();
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}
}