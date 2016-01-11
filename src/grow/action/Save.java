package grow.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

/**
 * Represents: an action that saves to a specified file
 *
 * @author Jacob Glueck
 *
 */
public class Save extends Action {

	public static final char PREFIX = 'S';

	/**
	 * The save file for the adventure
	 */
	private final File adventureFile;

	/**
	 * The save file for the game state
	 */
	private final File stateFile;

	/**
	 * Creates: a new save action which will save to the specified file.<br>
	 * Note: the save command will make any missing directories on the save
	 * path.
	 *
	 * @param adventureFile
	 *            the file in which to save the adventure
	 * @param stateFile
	 *            the file in which to save the state
	 */
	public Save(File stateFile, File adventureFile) {
		this.adventureFile = adventureFile;
		this.stateFile = stateFile;
	}

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		try {
			// Make any missing directories
			adventureFile.getParentFile().mkdirs();
			stateFile.getParentFile();

			PrintStream adventureOut = new PrintStream(adventureFile);
			world.saveWorld(adventureOut);
			adventureOut.close();
			PrintStream stateOut = new PrintStream(stateFile);
			world.saveState(stateOut);
			stateOut.close();
		} catch (FileNotFoundException e) {
			output.println("Problem with file: " + e.getMessage());
		}
		return current;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

}
