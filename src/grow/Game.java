package grow;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import exceptions.NoSuchScene;
import exceptions.SceneExists;
import exceptions.SyntaxError;

/**
 * Represents: a grow world, which contains named scenes.
 *
 * @author Jacob Glueck
 *
 */
public class Game {

	/**
	 * The line that separates scenes
	 */
	private static final String SCENE_SEPARATOR = "```";

	/**
	 * The map of names to scenes.
	 */
	private final Map<String, Scene> world;

	/**
	 * The score in this world
	 */
	private final Score score;

	/**
	 * The starting scene
	 */
	private Scene start;

	/**
	 * The current scene
	 */
	private Scene current;

	/**
	 * The name of the current adventure
	 */
	private String adventureName;

	/**
	 * Creates: a new empty world, with a score of 0.
	 *
	 * @param start
	 *            the starting scene. Added to the world.
	 * @param adventureName
	 *            the name of the adventure
	 */
	public Game(Scene start, String adventureName) {
		world = new HashMap<>();
		score = new Score();
		this.start = start;
		try {
			addScene(start);
		} catch (SceneExists e) {
			throw new Error();
		}
		restart();
		this.adventureName = adventureName;
	}

	/**
	 * Creates: a new game with the specified world and starting scene.
	 *
	 * @param start
	 *            the starting scene
	 * @param world
	 *            the world
	 * @param adventureName
	 *            the name of the adventure
	 * @throws SceneExists
	 *             if a scene with the same name as start exists in the world,
	 *             but it not equal to start.
	 */
	private Game(Scene start, Map<String, Scene> world, String adventureName) throws SceneExists {
		this.world = world;
		score = new Score();
		this.start = start;
		if (!world.containsKey(start.name())) {
			addScene(start);
		} else if (world.get(start.name()) != start) {
			throw new SceneExists(start.name());
		}
		restart();
		this.adventureName = adventureName;
	}

	/**
	 * @return an unmodifiable view of the scenes of this adventure
	 */
	public Map<String, Scene> scenes() {
		return Collections.unmodifiableMap(world);
	}

	/**
	 * Effect: sets the current scene to the start scene and sets the score to
	 * 0.
	 */
	public void restart() {
		score.set(0);
		current = start;
	}

	/**
	 * @return the adventure name
	 */
	public String name() {
		return adventureName;
	}

	/**
	 * Effect: adds the specified scene to the game, and throws a
	 * {@link SceneExists} exception if the scene already exists.
	 *
	 * @param s
	 *            the scene to add
	 * @throws SceneExists
	 *             if a scene with the same name already exists
	 */
	public void addScene(Scene s) throws SceneExists {
		if (world.containsKey(s.name())) {
			throw new SceneExists(s.name());
		} else {
			world.put(s.name(), s);
		}
	}

	/**
	 * @param name
	 *            the name of the scene to get
	 * @return the scene, or null if no such scene exists.
	 */
	public Scene getScene(String name) {
		return world.get(name);
	}

	/**
	 * @return the current score.
	 */
	public Score score() {
		return score;
	}

	/**
	 * @return the starting scene.
	 */
	public Scene start() {
		return start;
	}

	/**
	 * @return the current scene.
	 */
	public Scene current() {
		return current;
	}

	/**
	 * Effect: sets the current scene to the next scene.
	 *
	 * @param next
	 *            the next scene. Must be either null or contained in the world.
	 * @throws NoSuchScene
	 *             if the scene is not null and is not contained in the world.
	 */
	public void move(Scene next) throws NoSuchScene {
		if (next != null && !world.containsKey(next.name())) {
			throw new NoSuchScene(next.name());
		} else {
			current = next;
		}
	}

	/**
	 * Effect: writes the state of the game (the score and current scene) to the
	 * specified output stream.
	 *
	 * @param out
	 *            the stream to write the data to.
	 */
	public void saveState(PrintStream out) {
		out.println("Current: " + current.name());
		out.println("Score: " + score.score());
	}

	/**
	 * Effect: writes the adventure (the name of the first scene and all the
	 * scenes in the adventure) to the specified output stream.
	 *
	 * @param out
	 *            the output stream.
	 */
	public void saveWorld(PrintStream out) {
		out.println("Name: " + adventureName);
		out.println("Start: " + start.name());
		for (Entry<String, Scene> scene : world.entrySet()) {
			scene.getValue().save(out);
			out.println(SCENE_SEPARATOR);
		}
	}

	/**
	 * Effect: reads the input file and sets the current game state to the state
	 * of the file.
	 *
	 * @param in
	 *            the input file
	 * @throws NoSuchScene
	 *             if the current scene does not exist in this game
	 * @throws SyntaxError
	 *             if there is a syntax error in the file
	 */
	public void loadState(Scanner in) throws NoSuchScene, SyntaxError {
		int line = 1;
		try {
			String currentScene = extract("Current: ", in.nextLine(), line);
			line++;
			String scoreString = extract("Score: ", in.nextLine(), line);
			int score = Integer.parseInt(scoreString);
			if (!world.containsKey(currentScene)) {
				throw new NoSuchScene(currentScene);
			}
			current = world.get(currentScene);
			this.score.set(score);
		} catch (NoSuchElementException | NumberFormatException e) {

		}
	}

	/**
	 * Effect: reads an adventure from a file, and stores it in the current
	 * game. The score is set to 0 and the current scene is set to the starting
	 * scene. If an exception is thrown, the current state of this game is
	 * unchanged.
	 *
	 * @param in
	 *            the input stream
	 * @throws SyntaxError
	 *             if there is a problem reading from the file
	 */
	public void loadAdventure(Scanner in) throws SyntaxError {
		try {
			loadGame(null, in);
		} catch (NoSuchScene e) {
			// This should never happen
			throw new Error();
		}
	}

	/**
	 * Effect: loads an adventure and state into the current game. If
	 * {@code state} is null, the current scene is set to the starting scene and
	 * the score is set to 0. If an exception is thrown, the current state of
	 * this game is unchanged.
	 *
	 * @param state
	 *            the state to load
	 * @param adventure
	 *            the adventure to load
	 * @throws SyntaxError
	 *             if there is a problem with the files
	 * @throws NoSuchScene
	 *             if the starting scene does not exist
	 */
	public void loadGame(Scanner state, Scanner adventure) throws SyntaxError, NoSuchScene {
		Game game = parseGame(state, adventure);
		restart();
		world.clear();
		world.putAll(game.world);
		current = game.current;
		start = game.start;
		score.set(game.score.score());
		adventureName = game.adventureName;
	}

	/**
	 * Effect: loads all the data from another game into this game.
	 *
	 * @param game
	 *            the game to copy data from.
	 */
	public void loadGame(Game game) {
		world.clear();
		world.putAll(game.world);
		current = game.current;
		start = game.start;
		score.set(game.score.score());
		adventureName = game.adventureName;
	}

	/**
	 * Creates: a new game from a state and adventure file
	 *
	 * @param state
	 *            the state
	 * @param adventure
	 *            the adventure
	 * @return the game
	 * @throws SyntaxError
	 *             if there is a problem
	 * @throws NoSuchScene
	 *             if there is a problem
	 */
	public static Game parseGame(Scanner state, Scanner adventure) throws SyntaxError, NoSuchScene {
		Game game = parseWorld(adventure);
		if (state != null) {
			game.loadState(state);
		}
		return game;
	}

	/**
	 * Parses a game from the given input file. The current scene is set to the
	 * starting scene.
	 *
	 * @param input
	 *            the input
	 * @return the game
	 * @throws SyntaxError
	 *             if there is a problem
	 */
	public static Game parseWorld(Scanner input) throws SyntaxError {
		int line = 1;
		try {
			String adventureName = extract("Name: ", input.nextLine(), line);
			line++;
			String startName = extract("Start: ", input.nextLine(), line);
			List<Scene> scenes = new LinkedList<>();
			try {
				while (input.hasNextLine()) {
					line++;
					String name = extract("Name: ", input.nextLine(), line);
					line++;
					String description = extract("Description: ", input.nextLine(), line);
					String rule;
					List<Rule> rules = new LinkedList<>();
					while (!(rule = input.nextLine()).equals(SCENE_SEPARATOR)) {
						line++;
						rules.add(Rule.parseRule(rule, line));
					}
					Scene scene = new Scene(name, description);
					scene.rules().addAll(rules);
					scenes.add(scene);
				}
			} catch (Exception e) {
				throw new SyntaxError(line, e.getMessage());
			}

			Map<String, Scene> world = new HashMap<>();
			for (Scene scene : scenes) {
				if (world.containsKey(scene.name())) {
					throw new SyntaxError(-1, "Duplicate scene: " + scene.name());
				} else {
					world.put(scene.name(), scene);
				}
			}

			if (!world.containsKey(startName)) {
				throw new SyntaxError(1, "The start scene is not defined!");
			}

			try {
				return new Game(world.get(startName), world, adventureName);
			} catch (SceneExists e) {
				// This should never happen
				throw new Error();
			}
		} catch (NoSuchElementException e) {
			throw new SyntaxError(line, "Expected line, but none found.");
		}
	}

	/**
	 * Extracts the suffix from a string that must start with the specified
	 * prefix. Throws a syntax error if there is a problem.
	 *
	 * @param prefix
	 *            the prefix that {@code line} must start with.
	 * @param line
	 *            the line to extract the suffix from
	 * @param lineNumber
	 *            the line number to use when throwing a {@link SyntaxError}.
	 * @return the suffix
	 * @throws SyntaxError
	 *             if there is a problem with the format of {@code line}.
	 */
	public static String extract(String prefix, String line, int lineNumber) throws SyntaxError {
		if (!line.startsWith(prefix)) {
			throw new SyntaxError(lineNumber, "Line does not start with: " + prefix);
		}
		String startName = line.substring(prefix.length());
		if (startName.length() == 0) {
			throw new SyntaxError(lineNumber, "No data after: " + prefix);
		}
		return startName;
	}

}
