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

import exceptions.GrowException;
import grow.action.Action;
import grow.action.ChangeDescription;
import grow.action.EditAction;
import grow.action.EditActionOrder;
import grow.action.EditOrder;
import grow.action.EditPattern;
import grow.action.Extend;
import grow.action.Print;
import grow.action.RemoveRule;
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
	 * Creates: a new game of Grow which reads input from {@code input} and
	 * prints output to {@code output}.
	 *
	 * @param input
	 *            the input.
	 * @param output
	 *            the output.
	 */
	public GrowGame(Scanner input, PrintStream output) {
		this.input = input;
		this.output = output;
		saveManager = new SaveManager(new File(System.getProperty("user.home"), "grow"));
	}

	/**
	 * Starts a new game of grow that does not display images. Does not return
	 * until complete.
	 */
	public void play() {
		play((f) -> {
		});
	}

	/**
	 * Starts a new game of grow that does display images using the specified
	 * image consumer. Does not return until complete.
	 *
	 * @param imageDisplayer
	 *            the consumer which consumes files and displays the images.
	 */
	public void play(Consumer<Image> imageDisplayer) {
		try {
			Scene base = new Scene("default", "For help and instructions, type \"help\".");
			base.rules()
					.add(new Rule(
							Arrays.asList(new Print(
									"To quit, type \":quit\"\nTo start over again, type \":restart\"\nTo add a rule to this scene, type \":extend\"\nTo remove a rule from this scene, type \":remove rule\"\nTo open a different adventure, type \":change story\"\nTo create a new adventure, type \":new\"\nTo view all the rules for the current scene, type \":view\"\nTo change the order of the rules for the current scene, type \":edit order\"\nTo change the description for the current scene, type \":edit description\"\nTo edit a pattern in one of the rules for the current scene, type \":edit patterns\"\nTo edit the actions in one of the rules for the current scene, type \":edit actions\"\nTo change the order of the actions in one of the rules for the current scene, type \":reorder actions\"")),
					"help"));
			base.rules().add(new Rule(Arrays.asList(new Extend()), "extend"));
			base.rules().add(new Rule(Arrays.asList(saveManager.quitAction()), "quit"));
			base.rules().add(new Rule(Arrays.asList(new Restart()), "restart"));
			base.rules().add(new Rule(Arrays.asList(saveManager.saveAction()), "save"));
			base.rules().add(new Rule(Arrays.asList(saveManager.readAction()), "change story"));
			base.rules().add(new Rule(Arrays.asList(saveManager.newAction()), "new"));
			base.rules().add(new Rule(Arrays.asList(new View()), "view"));
			base.rules().add(new Rule(Arrays.asList(new ChangeDescription()), "edit description"));
			base.rules().add(new Rule(Arrays.asList(new EditOrder()), "edit order"));
			base.rules().add(new Rule(Arrays.asList(new EditPattern()), "edit patterns"));
			base.rules().add(new Rule(Arrays.asList(new RemoveRule()), "remove rule"));
			base.rules().add(new Rule(Arrays.asList(new EditAction()), "edit actions"));
			base.rules().add(new Rule(Arrays.asList(new EditActionOrder()), "reorder actions"));
			world = saveManager.init(input, output);
			imageDisplayer.accept(world.current().image());
			while (world.current() != null) {
				String line = input.nextLine();

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
						world.move(next);
						if (next == null) {
							break;
						} else {
							imageDisplayer.accept(next.image());
						}
					}
				}
			}
		} catch (GrowException e) {
			output.println("Something unusual occurred: ");
			e.printStackTrace(output);
			output.println("Please send this information to the developer!");
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
}
