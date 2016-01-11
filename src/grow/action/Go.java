package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import exceptions.SceneExists;
import grow.Game;
import grow.Scene;

/**
 * Represents: an action that goes to another node in the world
 *
 * @author Jacob Glueck
 */
public class Go extends Action {

	/**
	 * The prefix
	 */
	public static final char PREFIX = 'g';

	/**
	 * The name of the node to go to.
	 */
	private final String next;

	/**
	 * Creates: a new action that goes to the specified node. If the node is
	 * empty, {@link #act(Scene, Game, Scanner, PrintStream)} will make an empty
	 * node.
	 *
	 * @param next
	 *            the next node.
	 */
	public Go(String next) {
		this.next = next;
	}

	@Override
	public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
		if (world.getScene(next) == null) {
			output.println("Creating new scene: " + next);
			output.println("Description: ");
			Scene empty = new Scene(next, input.nextLine());
			try {
				world.addScene(empty);
			} catch (SceneExists e) {
				// This should never happen.
				throw new Error();
			}
			// output.println("Your new scene is empty. Extend it!");
			// Give the user a chance to extend the scene if it is empty
			// new Extend().act(empty, world, input, output);
		}
		Scene toGo = world.getScene(next);
		output.println(toGo.description());
		return toGo;
	}

	@Override
	public char commandPrefix() {
		return PREFIX;
	}

	@Override
	public String commandBody() {
		return next;
	}
}
