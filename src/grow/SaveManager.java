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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import grow.action.Action;
import grow.action.Go;
import grow.action.Quit;
import grow.action.Read;
import grow.action.Save;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import util.ZipLocker;

/**
 * Represents: a way to save Grow data.
 *
 * @author Jacob Glueck
 *
 */
public class SaveManager {

	/**
	 * The supported sound file extensions.
	 */
	public static final Set<String> SOUND_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("mp3", "wav", "aac")));

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
		linkMedia(result);
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
	 * @return the stream or null if there is no state file
	 * @throws FileNotFoundException
	 *             if there is a problem
	 */
	private InputStream readAdventureState(String adventureName) throws FileNotFoundException {
		File f = new File(new File(growDir, ADVENTURE_STATE), adventureName + "_state.txt");
		if (!f.exists()) {
			return null;
		} else {
			return new FileInputStream(f);
		}
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
		ZipLocker zip = new ZipLocker(adventureFile(adventureName));
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

	/**
	 * Gets the URI to a sound file.
	 *
	 * @param adventureName
	 *            the adventure name
	 * @param fileName
	 *            the file name
	 * @return the URI
	 * @throws IOException
	 *             if there is a problem
	 */
	private URI readSound(String adventureName, String fileName) throws IOException {
		ZipLocker zip = new ZipLocker(adventureFile(adventureName));
		URI result = zip.getURI(fileName);
		zip.close();
		return result;
	}

	/**
	 * Effect: searches in the adventure ZIP file for files with the specified
	 * prefix and one of the specified extensions. If a file with a prefix and
	 * one of the extensions is found, the extension is returned. Otherwise, a
	 * no such file exception is thrown.
	 *
	 * @param adventureName
	 *            the name of the adventure
	 * @param filePrefix
	 *            the prefix of the file
	 * @param extensions
	 *            the possible extensions
	 * @return the extension
	 * @throws NoSuchFileException
	 *             if no such file exists
	 */
	private String findAdventureFile(String adventureName, String filePrefix, Collection<String> extensions) throws NoSuchFileException {
		for (String ext : extensions) {
			try {
				String name = filePrefix + "." + ext;
				// Make sure to close the stream
				readImage(adventureName, name).close();
				// If we get here, it exists.
				return ext;
			} catch (IOException e) {
				// Ignore it, this file did not exist
			}
		}
		throw new NoSuchFileException(filePrefix + extensions.toString());
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
		ZipLocker zip = new ZipLocker(adventureFile(adventureName));
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
				File gameFile = new File(new File(growDir, ADVENTURES), last + ".zip");
				if (!gameFile.exists()) {
					throw new IOException("Game file " + gameFile + " does not exist.");
				}
				Scanner state = new Scanner(readAdventureState(last));
				Scanner game = new Scanner(readAdventure(last));
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
				linkMedia(world);
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
	 * @return true if the save succeeded, false otherwise.
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
	 * Effect: searches for sounds for all the scenes in the game and links them
	 * to the scenes in the game.
	 *
	 * @param g
	 *            the game
	 */
	private void linkSounds(Game g) {
		Map<String, Scene> scenes = g.scenes();
		for (String s : scenes.keySet()) {
			try {
				// Throws a no such file exception if there is no image.
				scenes.get(s).setSound(readSound(g.name(), s + "." + findAdventureFile(g.name(), s, SOUND_FILES)));
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
	 * @return true if the save succeeded, false otherwise.
	 */
	public boolean saveSound(Scene s, Game g, URI i) {
		File f = new File(i);
		int eIndex = f.getName().lastIndexOf(".");
		if (eIndex == -1) {
			return false;
		}
		String extension = f.getName().substring(eIndex + 1, f.getName().length());
		if (!SOUND_FILES.contains(extension)) {
			return false;
		}
		try {
			OutputStream out = writeImage(g.name(), s.name() + "." + extension);
			Files.copy(Paths.get(i), out);
			out.close();
		} catch (IOException e) {
			return false;
		}

		try {
			s.setSound(readSound(g.name(), s + "." + findAdventureFile(g.name(), s.name(), SOUND_FILES)));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Links the images and the sounds
	 *
	 * @param g
	 *            the game
	 */
	private void linkMedia(Game g) {
		linkImages(g);
		linkSounds(g);
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
	 * Creates: an action which imports a file. The path of the file to import
	 * is given on the input stream. It should be absolute.
	 *
	 * @return the import action
	 */
	public Action importAction() {
		return new Action() {
			@Override
			public char commandPrefix() {
				return 0;
			}

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output) {
				File adventureZip = new File(input.nextLine());
				Set<String> fileNames = adventureFileNames();
				// Remove the trailing .zip
				if (!adventureZip.getName().endsWith(".zip") || !adventureZip.exists()) {
					output.println("Bad file!");
					return current;
				}
				String baseName = adventureZip.getName().substring(0, adventureZip.getName().length() - ".zip".length());
				String genName = baseName;
				int v = 1;
				// Only worry about version numbers if the base name exists
				if (fileNames.contains(genName + ".zip")) {
					Matcher m = Pattern.compile(".*_v(\\d+)$").matcher(baseName);
					if (m.matches()) {
						v = Integer.parseInt(m.group(1));
						// Remove the version number
						baseName = baseName.substring(0, baseName.length() - m.group(1).length() - "_v".length());
					}
				}
				while (fileNames.contains(genName + ".zip")) {
					genName = baseName + "_v" + (++v);
				}

				String fileName = adventureZip.getName();
				while (fileNames.contains(fileName)) {
					output.println("You already have an adventure called " + fileName.substring(0, fileName.length() - ".zip".length()));
					output.println("What would you like to rename the adventure to? (Hit enter for " + genName + ")");
					fileName = input.nextLine();
					if (fileName.length() == 0) {
						fileName = genName + ".zip";
					} else {
						fileName += ".zip";
					}
				}

				String newAdventureName = fileName.substring(0, fileName.length() - ".zip".length());

				ZipLocker zip = null;
				ZipLocker original = null;

				try {
					zip = new ZipLocker(adventureFile(newAdventureName));
					original = new ZipLocker(adventureZip);
					zip.copy(original);
					original.close();
					try {
						String originalStoryName = adventureZip.getName().substring(0, adventureZip.getName().length() - ".zip".length()) + "_world.txt";
						if (!originalStoryName.equals(newAdventureName + "_world.txt")) {
							InputStream i = zip.read(originalStoryName);
							OutputStream o = zip.write(newAdventureName + "_world.txt");
							int r;
							// Copy the file
							while ((r = i.read()) != -1) {
								o.write(r);
							}
							i.close();
							o.close();
							zip.delete(originalStoryName);
						} else {
							// Just make sure the file exists
							zip.read(originalStoryName).close();
						}
					} catch (NoSuchFileException e1) {
						output.println("Invalid ZIP file!");
						return current;
					}
				} catch (IOException e) {
					output.println("Problem importing");
					return current;
				} finally {
					try {
						zip.close();
					} catch (IOException e) {
						output.println("Problem closing ZIP file: " + e.getMessage());
						return current;
					}
				}
				try {
					current = new Read(readAdventureState(newAdventureName), readAdventure(newAdventureName)).act(current, world, input, output);
					output.println("Imported adventure!");
					// Link the images
					linkMedia(world);
					// Change the name
					world.setName(newAdventureName);
					return new Go(current.name()).act(current, world, input, output);
				} catch (IOException e) {
					output.println("Problem reading the new adventure: " + e.getMessage());
					return current;
				}
			}
		};
	}

	/**
	 * Gets the ZIP file where an adventure is stored
	 *
	 * @param adventureName
	 *            the adventure name
	 * @return the file where it is stored
	 */
	public File adventureFile(String adventureName) {
		return new File(new File(growDir, ADVENTURES), adventureName + ".zip");
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
		Set<String> fileNames = adventureFileNames();
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
	 * @return the names of all the existing adventures.
	 */
	private Set<String> adventureFileNames() {
		Set<String> fileNames = new HashSet<>();
		for (File f : new File(growDir, ADVENTURES).listFiles()) {
			fileNames.add(f.getName());
		}
		return fileNames;
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
