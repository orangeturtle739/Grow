package grow;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
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
import utiil.ZipLocker;

/**
 * Represents: a way to save Grow data.
 *
 * @author Jacob Glueck
 *
 */
public class SaveManager {

	/**
	 * The name of the file in which the last adventure playing is stored.
	 */
	private static final String CURRENT_FILE = "current_adventure.txt";
	/**
	 * The name of the folder in the root directory which holds the program data
	 * information (the above file)
	 */
	private static final String PROGRAM_DATA = "program_data";
	/**
	 * The name of the folder in the root directory which stores all the
	 * adventure state data
	 */
	private static final String ADVENTURE_STATE = "state";
	/**
	 * The name of the folder in the root directory which stores the adventures
	 */
	private static final String ADVENTURES = "adventures";

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
	 *            the directory in which to store all the grow files
	 */
	public SaveManager(File growDir) {
		this.growDir = growDir;
		// Make all the parent directories
		this.growDir.mkdirs();
		new File(growDir, PROGRAM_DATA).mkdirs();
		new File(growDir, ADVENTURE_STATE).mkdirs();
		new File(growDir, ADVENTURES).mkdirs();
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

	// /**
	// * Opens a stream to read the current program state.
	// *
	// * @return the stream
	// * @throws FileNotFoundException
	// * if there is a problem
	// */
	// private InputStream readProgramState() throws FileNotFoundException {
	// return new FileInputStream(new File(new File(growDir, PROGRAM_DATA),
	// CURRENT_FILE));
	// }

	/**
	 * Opens a stream to read the current adventure state.
	 *
	 * @param adventureName
	 *            the name of the adventure
	 * @return the stream
	 * @throws FileNotFoundException
	 *             if there is a problem
	 */
	private InputStream readAdventureState(String adventureName) throws FileNotFoundException {
		return new FileInputStream(new File(new File(growDir, ADVENTURE_STATE), adventureName + "_state.txt"));
	}

	/**
	 * Opens a stream to read the adventure data
	 *
	 * @param adventureName
	 *            the adventure name
	 * @return the stream. This stream must be closed, otherwise the zip file
	 *         will become corrupted, and the program will crash.
	 * @throws IOException
	 *             if there is a problem.
	 */
	private InputStream readAdventure(String adventureName) throws IOException {
		return readImage(adventureName, adventureName + "_world.txt");
	}

	/**
	 * Opens a stream to read data from the zip file
	 *
	 * @param adventureName
	 *            the name of the adventure
	 * @param fileName
	 *            the name of the file to read from the zip
	 * @return the stream. This stream must be closed, otherwise the zip file
	 *         will become corrupted, and the program will crash.
	 * @throws IOException
	 *             if there is a problem
	 * @throws NoSuchFileException
	 *             exception if the image could not be found
	 */
	private InputStream readImage(String adventureName, String fileName) throws IOException {
		ZipLocker zip = new ZipLocker(new File(new File(growDir, ADVENTURES), adventureName + ".zip"));
		InputStream stream;
		try {
			stream = zip.read(fileName);
		} catch (Exception e) {
			zip.close();
			throw e;
		}
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return stream.read();
			}

			@Override
			public void close() throws IOException {
				stream.close();
				zip.close();
			}
		};
	}

	// /**
	// * Opens a stream to write the program state
	// *
	// * @return the stream
	// * @throws FileNotFoundException
	// * if there is a problem
	// */
	// private OutputStream writeProgramState() throws FileNotFoundException {
	// return new FileOutputStream(new File(new File(growDir, PROGRAM_DATA),
	// CURRENT_FILE));
	// }

	/**
	 * Opens a stream to write the adventure state
	 *
	 * @param adventureName
	 *            the name of the adventure
	 * @return the stream
	 * @throws FileNotFoundException
	 *             if there is a problem
	 */
	private OutputStream writeAdventureState(String adventureName) throws FileNotFoundException {
		return new FileOutputStream(new File(new File(growDir, ADVENTURE_STATE), adventureName + "_state.txt"));
	}

	/**
	 * Opens a stream to write the adventure.
	 *
	 * @param adventureName
	 *            the name of the adventure.
	 * @return the stream. This stream must be closed, otherwise the zip file
	 *         will become corrupted, and the program will crash.
	 * @throws IOException
	 *             if there is a problem
	 */
	private OutputStream writeAdventure(String adventureName) throws IOException {
		return writeImage(adventureName, adventureName + "_world.txt");
	}

	/**
	 * Opens a stream to write to a file in the adventure zip file
	 *
	 * @param adventureName
	 *            the name of the adventure
	 * @param fileName
	 *            the name of the file to right to
	 * @return the stream. This stream must be closed, otherwise the zip file
	 *         will become corrupted, and the program will crash.
	 * @throws IOException
	 *             if there is a problem
	 */
	private OutputStream writeImage(String adventureName, String fileName) throws IOException {
		ZipLocker zip = new ZipLocker(new File(new File(growDir, ADVENTURES), adventureName + ".zip"));
		OutputStream stream = zip.write(fileName);
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				stream.write(b);
			}

			@Override
			public void close() throws IOException {
				stream.close();
				zip.close();
			}
		};
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
				Scanner state = new Scanner(readAdventureState(gameFile.getName()));
				Scanner game = new Scanner(readAdventure(gameFile.getName()));
				Game r = Game.parseGame(state, game);
				state.close();
				game.close();
				return r;
			} catch (Exception e) {
				output.println("Error loading last game state: " + e.getMessage());
				output.println("Will create new game.");
			}
		}

		// If we get here, the file did not exist or there was a problem reading
		// it. So, make a new game.
		return newGame(input, output);
	}

	/**
	 * @return an action which saves the current grow game properly.
	 */
	public Action saveAction() {
		return new Action() {

			@Override
			public char commandPrefix() {
				return 0;
			}

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
				try {
					return new Save(writeAdventureState(world.name()), writeAdventure(world.name())).act(current, world, input, output);
				} catch (IOException e) {
					output.println("Error saving: " + e.getMessage());
					return current;
				}
			}
		};
	}

	/**
	 * @return an action which prompts the user to pick a new story, and then
	 *         opens that story.
	 */
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
				for (File f : new File(growDir, ADVENTURES).listFiles((f) -> f.getName().endsWith(".zip"))) {
					files.add(f);
					output.printf("%-5s %s", Integer.toString(count++), f.getName().substring(0, f.getName().lastIndexOf('.')));
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
				String adventureName = files.get(num - 1).getName();
				adventureName = adventureName.substring(0, adventureName.lastIndexOf('.'));
				try {
					current = new Read(readAdventureState(adventureName), readAdventure(adventureName)).act(current, world, input, output);
				} catch (IOException e) {
					output.println("Error read adventure: " + e.getMessage());
					return current;
				}

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
			try {
				// Throws a no such file exception if there is no image.
				InputStream imageStream = readImage(g.name(), s + ".jpeg");
				scenes.get(s).setImage(new Image(imageStream));
				imageStream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Effect: saves an image so that it is associated with the specified scene
	 * in the game.
	 *
	 * @param s
	 *            the scene
	 * @param g
	 *            the game
	 * @param i
	 *            the image
	 * @return true if the save succedded, false otherwise.
	 */
	public boolean saveImage(Scene s, Game g, Image i) {
		try {
			BufferedImage buff = SwingFXUtils.fromFXImage(i, null);
			if (buff == null) {
				return false;
			}
			OutputStream imageOut = writeImage(g.name(), s.name() + ".jpeg");
			boolean success = ImageIO.write(buff, "JPEG", imageOut);
			imageOut.close();
			if (!success) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}

		try {
			InputStream in = readImage(g.name(), s.name() + ".jpeg");
			s.setImage(new Image(in));
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @return an action which prompts the user to create a new story and saves
	 *         the new story.
	 */
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
		for (File f : new File(growDir, ADVENTURES).listFiles()) {
			fileNames.add(f.getName());
		}
		String fileName;
		do {
			fileName = "story_" + randomAlphNum() + randomAlphNum() + randomAlphNum() + randomAlphNum() + randomAlphNum() + randomAlphNum();
		} while (fileNames.contains(fileName + ".zip"));
		output.println("What would you like to name your story (hit enter for " + fileName + ")?");
		String line = "";
		while (true) {
			line = input.nextLine();
			if (fileNames.contains(line + ".zip")) {
				output.println("That name is already taken. Pick a different name or hit enter.");
			} else {
				break;
			}
		}
		fileName = line.length() == 0 ? fileName : line;
		return new Game(new Scene("start", "Welcome to grow! Your world is empty :(. But, you can fill it with stuff! To get started, type \":help\"!"), fileName);
	}

	/**
	 * @return a random character, chosen from the numerals, the upppercase
	 *         letters, and the lowercase letters.
	 */
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

	/**
	 * @return an action which quits and saves the current state.
	 */
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
		return new File(new File(growDir, PROGRAM_DATA), CURRENT_FILE);
	}
}
