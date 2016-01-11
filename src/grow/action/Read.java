package grow.action;

import java.io.File;
import java.io.FileNotFoundException;
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
	private final File stateFile;
	/**
	 * The file in which the adventure is saved
	 */
	private final File adventureFile;

	/**
	 * Creates: a read action that reads that state form the specified file, and
	 * the adventure form the specified adventure file.
	 *
	 * @param stateFile
	 *            the state file
	 * @param adventureFile
	 *            the adventure file
	 */
	public Read(File stateFile, File adventureFile) {
		this.stateFile = stateFile;
		this.adventureFile = adventureFile;
	}

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		try {
			Scanner state = new Scanner(stateFile);
			Scanner adventure = new Scanner(adventureFile);
			world.loadGame(state, adventure);
		} catch (FileNotFoundException | GrowException e) {
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