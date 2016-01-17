package grow;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

import exceptions.NoSuchScene;
import grow.action.Action;
import grow.action.ChangeDescription;
import grow.action.Edit;
import grow.action.Extend;
import grow.action.Print;
import grow.action.Remove;
import grow.action.Reorder;
import grow.action.Restart;
import grow.action.View;
import javafx.scene.image.Image;

/**
 * Represents: a game of Grow
 *
 * @author Jacob Glueck
 *
 */
public class GrowGame {

	/**
	 * The list of responses to unknown input
	 */
	private static final List<String> unknownInput = readList();
	/**
	 * A random number generator used to randomly pick a response from
	 * {@link #unknownInput}.
	 */
	private static final Random rnd = new Random();

	/**
	 * Reads the {@code unknown.txt} file and returns the data to be stored
	 * {@link #unknownInput}.
	 *
	 * @return the data
	 */
	private static List<String> readList() {
		Scanner s = new Scanner(GrowGame.class.getResourceAsStream("unknown.txt"));
		List<String> words = new ArrayList<>();
		while (s.hasNextLine()) {
			words.add(s.nextLine());
		}
		s.close();
		return Collections.unmodifiableList(words);
	}

	/**
	 * @return a random response from {@link #unknownInput}.
	 */
	private static String randomResponse() {
		return unknownInput.get(rnd.nextInt(unknownInput.size()));
	}

	/**
	 * The scanner used for input to this game
	 */
	private final Scanner input;
	/**
	 * The print stream used for output from this game
	 */
	private final PrintStream output;
	/**
	 * The save manager for this game
	 */
	private final SaveManager saveManager;
	/**
	 * The game.
	 */
	private Game world;
	/**
	 * The base scene, with all the built-in commands
	 */
	private final Scene base;

	/**
	 * Creates: a new game of Grow which reads input from {@code input} and
	 * prints output to {@code output}.
	 *
	 * @param input
	 *            the input.
	 * @param output
	 *            the output.
	 * @param growRoot
	 *            the root directory for the storage for the game
	 */
	public GrowGame(Scanner input, PrintStream output, File growRoot) {
		this.input = input;
		this.output = output;
		saveManager = new SaveManager(growRoot);
		world = null;
		base = new Scene("default", "For help and instructions, type \"help\".");
		base.rules()
				.add(new Rule(
						Arrays.asList(new Print(
								"To quit, type \":quit\"\nTo start over again, type \":restart\"\nTo add a rule to this scene, type \":extend\"\nTo remove a rule from this scene, type \":remove\"\nTo change the order of the rules in this scene, type \":reorder\"\nTo change the description for the current scene, type \":description\"\nTo view all the rules for the current scene, type \":view\"\nTo open a different adventure, type \":change story\"\nTo create a new adventure, type \":new\"")),
				"help"));
		base.rules().add(new Rule(Arrays.asList(saveManager.quitAction()), "quit"));
		base.rules().add(new Rule(Arrays.asList(new Restart()), "restart"));
		base.rules().add(new Rule(Arrays.asList(saveManager.readAction()), "change story"));
		base.rules().add(new Rule(Arrays.asList(saveManager.newAction()), "new"));
		base.rules().add(new Rule(Arrays.asList(new Extend()), "extend"));
		base.rules().add(new Rule(Arrays.asList(new Remove()), "remove"));
		base.rules().add(new Rule(Arrays.asList(new Edit()), "edit"));
		base.rules().add(new Rule(Arrays.asList(new Reorder()), "reorder"));
		base.rules().add(new Rule(Arrays.asList(new ChangeDescription()), "description"));

		base.rules().add(new Rule(Arrays.asList(new View()), "view"));

		base.rules().add(new Rule(Arrays.asList(saveManager.importAction()), "import"));
		base.rules().add(new Rule(Arrays.asList(saveManager.saveAction()), "save"));
	}

	/**
	 * Initializes the game. Throws an {@link IllegalStateException} if the game
	 * has already been initialized.
	 *
	 * @param imageDisplayer
	 *            the image displayer used to display the initial image
	 * @param u
	 *            the status updater, used to signal scene or adventure changes
	 */
	public void init(Consumer<Image> imageDisplayer, StatusUpdater u) {
		if (world != null) {
			throw new IllegalStateException();
		}
		world = saveManager.init(input, output);
		imageDisplayer.accept(world.current().image());
		u.update(world.name(), world.current().name());
	}

	/**
	 * Does not display images.
	 *
	 * @see GrowGame#init(Consumer, StatusUpdater)
	 */
	public void init() {
		init((i) -> {
		} , (a, s) -> {
		});
	}

	/**
	 * Effect: executes a single turn using {@code line} as the initial input,
	 * and using the input stream to get the rest of the input. If the turn
	 * results in the termination of the game, this method returns false, and
	 * resets the game so that another call to GrowGame#init(Consumer,
	 * StatusUpdater) will restart it.
	 *
	 * @param line
	 *            the line to use as the initial input
	 * @param imageDisplayer
	 *            the image displayer
	 * @param u
	 *            the status updater, used to signal scene or adventure changes
	 * @return true if the game is still going, false if the game is over
	 */
	public boolean doTurn(String line, Consumer<Image> imageDisplayer, StatusUpdater u) {
		List<Action> actions = null;
		// Check to see if it is a command
		if (line.startsWith(":")) {
			actions = base.act(line.substring(1));
		}
		actions = actions == null ? world.current().act(line) : actions;
		if (actions == null) {
			output.println(randomResponse());
		} else {
			for (Action a : actions) {
				Scene next = a.act(world.current(), world, input, output);
				try {
					world.move(next);
				} catch (NoSuchScene e) {
					output.println("Something bad has occurred. Please tell the developer.");
					e.printStackTrace(output);
					next = null;
				}
				if (next == null) {
					// The game is over, so reset (allow another call to init)
					world = null;
					return false;
				} else {
					imageDisplayer.accept(next.image());
					u.update(world.name(), world.current().name());
				}
			}
		}
		return true;
	}

	/**
	 * Does not display images
	 *
	 * @param line
	 *            the initial input
	 * @return true if the game is still running
	 * @see GrowGame#doTurn(String, Consumer, StatusUpdater)
	 */
	public boolean doTurn(String line) {
		return doTurn(line, (i) -> {
		} , (a, s) -> {
		});
	}

	/**
	 * Starts a new game of grow that does not display images. Does not return
	 * until complete.
	 */
	public void play() {
		play((f) -> {
		} , (a, s) -> {
		});
	}

	/**
	 * Starts a new game of grow that does display images using the specified
	 * image consumer. Does not return until complete.
	 *
	 * @param imageDisplayer
	 *            the consumer which consumes files and displays the images.
	 * @param u
	 *            the status updater, used to signal scene or adventure changes
	 */
	public void play(Consumer<Image> imageDisplayer, StatusUpdater u) {
		// Keep doing turns until the game is over
		while (doTurn(input.nextLine(), imageDisplayer, u)) {
			;
		}
	}

	/**
	 * Effect: tries to save and load the image into the game.
	 *
	 * @param i
	 *            the image
	 * @return true if it worked, false otherwise.
	 */
	public boolean saveImage(Image i) {
		if (world == null) {
			return false;
		}
		return saveManager.saveImage(world.current(), world, i);
	}

	/**
	 * @return the ZIP file where the current adventure is stored
	 */
	public File adventureFile() {
		return saveManager.adventureFile(world.name());
	}

	// /**
	// * Effect: imports an adventure from a zip, and switches to it.
	// *
	// * @param adventureZip
	// * the adventure zip
	// */
	// public void importAdventure(File adventureZip) {
	// saveManager.importAction(adventureZip).act(world.current(), world, input,
	// output);
	// }
}
