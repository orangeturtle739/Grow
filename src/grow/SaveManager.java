package grow;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;

import grow.action.Action;
import grow.action.Go;
import grow.action.Quit;
import grow.action.Read;
import grow.action.Save;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SaveManager {

	private static final String CURRENT_FILE = "current_adventure.txt";

	/**
	 * The directory in which all grow files are saved
	 */
	private final File growDir;

	/**
	 * Creates: a new save manager which saves all the files in the specified
	 * directory. Makes the grow directory, and any parent directories, if
	 * needed.
	 *
	 * @param growDir
	 */
	public SaveManager(File growDir) {
		this.growDir = growDir;
		// Make all the parent directories
		this.growDir.mkdirs();
	}

	/**
	 * Creates: the initial game and goes to the current location so that the
	 * initial description is printed out
	 *
	 * @param input
	 *            the input
	 * @param output
	 *            the output
	 * @return the initial game
	 */
	public Game init(Scanner input, PrintStream output) {
		Game result = initGame(input, output);
		linkImages(result);
		new Go(result.current().name()).act(result.current(), result, input, output);
		return result;

	}

	/**
	 * Creates: the initial game
	 *
	 * @param input
	 *            the input
	 * @param output
	 *            the output
	 * @return the initial game
	 */
	private Game initGame(Scanner input, PrintStream output) {
		File currentFile = currentFile();
		if (currentFile.exists()) {
			try {
				Scanner s = new Scanner(currentFile);
				String last = s.nextLine();
				s.close();

				File gameFile = new File(growDir, last);
				File stateFile = new File(gameFile, gameFile.getName() + "_state.txt");
				File adventureFile = new File(gameFile, gameFile.getName() + "_world.txt");
				return Game.parseGame(new Scanner(stateFile), new Scanner(adventureFile));
			} catch (Exception e) {
				output.println("Error loading last game state: " + e.getMessage());
				output.println("Will create new game.");
			}
		}

		// If we get here, the file did not exist or there was a problem reading
		// it. So, make a new game.
		return newGame(input, output);
	}

	public Action saveAction() {
		return new Action() {

			@Override
			public char commandPrefix() {
				return 0;
			}

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
				File gameFile = new File(growDir, world.name());
				gameFile.mkdirs();
				File stateFile = new File(gameFile, world.name() + "_state.txt");
				File adventureFile = new File(gameFile, world.name() + "_world.txt");
				return new Save(stateFile, adventureFile).act(current, world, input, output);
			}
		};
	}

	public Action readAction() {
		return new Action() {

			@Override
			public char commandPrefix() {
				return 0;
			}

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
				// Save the current game fist
				current = saveAction().act(current, world, input, output);

				List<File> files = new ArrayList<>();
				int count = 1;
				for (File f : growDir.listFiles((f) -> f.isDirectory())) {
					files.add(f);
					output.printf("%-5s %s", Integer.toString(count++), f.getName());
					output.println();
				}
				output.println();
				int num = -1;
				while (num <= 0) {
					output.println("Adventure #:");
					try {
						num = Integer.parseInt(input.nextLine());
					} catch (NumberFormatException e) {
						num = -1;
					}
					if (num - 1 >= files.size()) {
						num = -1;
					}
				}
				File gameFile = files.get(num - 1);
				File stateFile = new File(gameFile, gameFile.getName() + "_state.txt");
				File adventureFile = new File(gameFile, gameFile.getName() + "_world.txt");
				current = new Read(stateFile, adventureFile).act(current, world, input, output);

				// Look for any associated images
				// world.
				linkImages(world);
				return new Go(current.name()).act(current, world, input, output);
			}
		};
	}

	/**
	 * Effect: searches for images for all the scenes in the game and links them
	 * to the scenes in the game.
	 *
	 * @param g
	 *            the game
	 */
	private void linkImages(Game g) {
		Map<String, Scene> scenes = g.scenes();
		for (String s : scenes.keySet()) {
			File imageFile = new File(new File(growDir, g.name()), s + ".jpeg");
			if (imageFile.exists()) {
				scenes.get(s).setImageFile(imageFile);
			}
		}
	}

	public boolean saveImage(Scene s, Game g, Image i) {
		File imageFile = new File(new File(growDir, g.name()), s.name() + ".jpeg");
		try {
			BufferedImage buff = SwingFXUtils.fromFXImage(i, null);
			if (buff == null) {
				return false;
			}
			if (!ImageIO.write(buff, "JPEG", imageFile)) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
		s.setImageFile(imageFile);
		return true;
	}

	public Action newAction() {
		return new Action() {

			@Override
			public char commandPrefix() {
				return 0;
			}

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
				// Save the current game fist
				current = saveAction().act(current, world, input, output);

				world.loadGame(newGame(input, output));
				current = saveAction().act(current, world, input, output);
				return new Go(world.start().name()).act(current, world, input, output);
			}
		};
	}

	/**
	 * Creates: a new game
	 *
	 * @param input
	 *            the input stream for user input
	 * @param output
	 *            the output stream for user input
	 * @return the new game
	 */
	private Game newGame(Scanner input, PrintStream output) {
		Set<String> fileNames = new HashSet<>();
		for (File f : growDir.listFiles()) {
			fileNames.add(f.getName());
		}
		String fileName;
		do {
			fileName = "story_" + randomAlphNum() + randomAlphNum() + randomAlphNum() + randomAlphNum() + randomAlphNum() + randomAlphNum();
		} while (fileNames.contains(fileName));
		output.println("What would you like to name your story (hit enter for " + fileName + ")?");
		String line = "";
		while (true) {
			line = input.nextLine();
			if (fileNames.contains(line)) {
				output.println("That name is already taken. Pick a different name or hit enter.");
			} else {
				break;
			}
		}
		fileName = line.length() == 0 ? fileName : line;
		return new Game(new Scene("start", "Welcome to grow! Your world is empty :(. But, you can fill it with stuff! To get started, type \":help\"!"), fileName);
	}

	private static char randomAlphNum() {
		int x = (int) (Math.random() * 62);
		if (x < 26) {
			return (char) ('a' + x);
		} else if (x < 52) {
			return (char) ('A' + x - 26);
		} else {
			return (char) ('0' + x - 52);
		}
	}

	public Action quitAction() {
		return new Action() {

			@Override
			public char commandPrefix() {
				return 0;
			}

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
				// Save the current game fist
				current = saveAction().act(current, world, input, output);
				try {
					PrintStream currentFile = new PrintStream(currentFile());
					currentFile.println(world.name());
					currentFile.close();
				} catch (FileNotFoundException e) {
					output.println("Something strange has happened!");
					e.printStackTrace(output);
					output.println("When you start up the program next time, it may not remember where you left off. Please send the above information to the developer.");
				}
				return new Quit().act(current, world, input, output);
			}
		};
	}

	/**
	 * @return the file where the name of the last played adventure is stored
	 */
	private File currentFile() {
		return new File(growDir, CURRENT_FILE);
	}
}
