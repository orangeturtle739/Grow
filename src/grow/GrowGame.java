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
import grow.action.Description;
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

public class GrowGame {

	private static final List<String> unknownInput = readList();
	private static final Random rnd = new Random();

	private static List<String> readList() {
		Scanner s = new Scanner(GrowGame.class.getResourceAsStream("unknown.txt"));
		List<String> words = new ArrayList<>();
		while (s.hasNextLine()) {
			words.add(s.nextLine());
		}
		s.close();
		return Collections.unmodifiableList(words);
	}

	private static String randomResponse() {
		return unknownInput.get(rnd.nextInt(unknownInput.size()));
	}

	private final Scanner input;
	private final PrintStream output;
	private final SaveManager saveManager;
	private Game world;

	public GrowGame(Scanner input, PrintStream output) {
		this.input = input;
		this.output = output;
		saveManager = new SaveManager(new File(System.getProperty("user.home"), "grow"));
	}

	public void play() {
		play((f) -> {
		});
	}

	public void play(Consumer<File> imageDisplayer) {
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
			base.rules().add(new Rule(Arrays.asList(new Description()), "edit description"));
			base.rules().add(new Rule(Arrays.asList(new EditOrder()), "edit order"));
			base.rules().add(new Rule(Arrays.asList(new EditPattern()), "edit patterns"));
			base.rules().add(new Rule(Arrays.asList(new RemoveRule()), "remove rule"));
			base.rules().add(new Rule(Arrays.asList(new EditAction()), "edit actions"));
			base.rules().add(new Rule(Arrays.asList(new EditActionOrder()), "reorder actions"));
			world = saveManager.init(input, output);
			imageDisplayer.accept(world.current().imageFile());
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
							imageDisplayer.accept(next.imageFile());
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
	 * @param s
	 *            the scene
	 * @param g
	 *            the game
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
