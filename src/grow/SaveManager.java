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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import grow.action.Action;
import grow.action.Go;
import grow.action.Quit;
import grow.action.Read;
import grow.action.Save;
import grow.action.Util;
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
	 * A regex for finding sound file extensions.
	 */
	private static final String soundExtRegex = String.join("|", SOUND_FILES);

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
	 * @param injector
	 *            the injector to use to prompt the user
	 * @return the initial game
	 */
	public Game init(Scanner input, PrintStream output, Consumer<String> injector) {
		clean(input, output);
		Game result = initGame(input, output);
		linkMedia(result);
		new Go(result.current().name()).act(result.current(), result, input, output, injector);
		return result;
	}

	/**
	 * Effect: looks through the grow game directory and identifies state files
	 * without a corresponding adventure file, and adventure files without a
	 * corresponding state file. Then, with the user's permission, deletes the
	 * files.
	 *
	 * @param input
	 *            the input stream
	 * @param output
	 *            the output stream
	 */
	public void clean(Scanner input, PrintStream output) {
		Set<String> states = new HashSet<>();
		Set<String> adventures = new HashSet<>();
		File stateDir = new File(growDir, ADVENTURE_STATE);
		states.addAll(Arrays.asList(stateDir.listFiles()).stream().map((f) -> f.getName()).collect(Collectors.toList()));
		File adventureDir = new File(growDir, ADVENTURES);
		adventures.addAll(Arrays.asList(adventureDir.listFiles()).stream().map((f) -> f.getName()).collect(Collectors.toList()));
		Set<String> badStateFiles = new HashSet<>();
		Set<String> badAdventureFiles = new HashSet<>();
		Set<String> goodStateFiles = new HashSet<>();
		Set<String> goodAdventureFiles = new HashSet<>();
		for (String str : states) {
			if (!str.startsWith(".")) {
				if (!str.endsWith("_state.txt")) {
					badStateFiles.add(str);
				} else {
					goodStateFiles.add(str);
				}
			}
		}
		for (String str : adventures) {
			if (!str.startsWith(".")) {
				if (!str.endsWith(".zip")) {
					badAdventureFiles.add(str);
				} else {
					goodAdventureFiles.add(str);
				}
			}
		}
		Set<String> adventureLessStates = new HashSet<>();
		for (String state : goodStateFiles) {
			String adventureName = state.substring(0, state.length() - "_state.txt".length());
			if (!goodAdventureFiles.contains(adventureName + ".zip")) {
				adventureLessStates.add(state);
			}
		}

		if (badStateFiles.size() != 0) {
			output.println("There are game states stored in your grow folder which are named improperly:");
			Util.printNumberedList("", ".", 0, 5, output, badStateFiles);
			output.println("Would you like to remove them? (y/n)");
			if (yesNo(input)) {
				for (String str : badStateFiles) {
					deleteFile(new File(stateDir, str));
				}
			}
		}
		if (badAdventureFiles.size() != 0) {
			output.println("There are adventures stored in your grow folder which are named improperly:");
			Util.printNumberedList("", ".", 0, 5, output, badAdventureFiles);
			output.println("Would you like to remove them? (y/n)");
			if (yesNo(input)) {
				for (String str : badAdventureFiles) {
					deleteFile(new File(adventureDir, str));
				}
			}
		}
		if (adventureLessStates.size() != 0) {
			output.println("There are state files stored in your grow folder which correspond to adventures which do not exist:");
			Util.printNumberedList("", ".", 0, 5, output, adventureLessStates);
			output.println("Would you like to remove them? (y/n)");
			if (yesNo(input)) {
				for (String str : adventureLessStates) {
					deleteFile(new File(stateDir, str));
				}
			}
		}
	}

	/**
	 * Effect: recursively deletes a file and all of the files in it
	 *
	 * @param f
	 *            the file
	 */
	private void deleteFile(File f) {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				deleteFile(c);
			}
		}
		f.delete();
	}

	/**
	 * @param input
	 *            the input
	 * @return true only if the input is {@code Y} or {@code y}.
	 */
	private static boolean yesNo(Scanner input) {
		return input.nextLine().equalsIgnoreCase("y");
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
	 * Gets the URI to a sound file. Finds the sound file based on the scene
	 * name.
	 *
	 * @param adventureName
	 *            the adventure name
	 * @param sceneName
	 *            the name of the scene
	 * @return the URI
	 * @throws IOException
	 *             if there is a problem
	 * @throws NoSuchFileException
	 *             if the sound file cannot be found
	 */
	private URI readSound(String adventureName, String sceneName) throws IOException {
		ZipLocker zip = new ZipLocker(adventureFile(adventureName));
		List<String> fileNames = zip.listFiles();
		zip.close();
		Pattern p = Pattern.compile("(" + Pattern.quote(sceneName + ".") + ")(" + soundExtRegex + ")");
		for (String str : fileNames) {
			if (p.matcher(str).matches()) {
				return readSoundFile(adventureName, str);
			}
		}
		throw new NoSuchFileException(sceneName);
	}

	/**
	 * Reads sound from a specified file name
	 *
	 * @param adventureName
	 *            the adventure name
	 * @param fileName
	 *            the file name
	 * @return the URI
	 * @throws IOException
	 *             if there is a problem
	 */
	private URI readSoundFile(String adventureName, String fileName) throws IOException {
		ZipLocker zip = new ZipLocker(adventureFile(adventureName));
		URI result = zip.getURI(fileName);
		zip.close();
		return result;
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
			Scanner state = null;
			Scanner game = null;
			try {
				Scanner s = new Scanner(currentFile);
				String last = s.nextLine();
				s.close();
				File gameFile = new File(new File(growDir, ADVENTURES), last + ".zip");
				if (!gameFile.exists()) {
					throw new IOException("Game file " + gameFile + " does not exist.");
				}
				state = new Scanner(readAdventureState(last));
				game = new Scanner(readAdventure(last));
				Game r = Game.parseGame(state, game);
				state.close();
				game.close();
				return r;
			} catch (Exception e) {
				output.println("Error loading last game state: " + e.getMessage());
				output.println("Will create new game.");
			} finally {
				if (state != null) {
					state.close();
				}
				if (game != null) {
					game.close();
				}
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
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				try {
					return new Save(writeAdventureState(world.name()), writeAdventure(world.name())).act(current, world, input, output, injector);
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
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				return Util.handleCancel(current, output, () -> {
					Scene modCurrent = current;
					// Save the current game fist
					modCurrent = saveAction().act(modCurrent, world, input, output, injector);

					List<File> files = new ArrayList<>();
					int count = 1;
					for (File f : new File(growDir, ADVENTURES).listFiles((f) -> f.getName().endsWith(".zip"))) {
						files.add(f);
						output.printf("%-5s %s", Integer.toString(count++), f.getName().substring(0, f.getName().lastIndexOf('.')));
						output.println();
					}
					output.println();
					int num = Util.readInt(output, input, "Adventure #:", "Bad story number!", 1, files.size()) - 1;
					String adventureName = files.get(num).getName();
					adventureName = adventureName.substring(0, adventureName.lastIndexOf('.'));
					try {
						modCurrent = new Read(readAdventureState(adventureName), readAdventure(adventureName)).act(modCurrent, world, input, output, injector);
					} catch (IOException e) {
						output.println("Error read adventure: " + e.getMessage());
						return modCurrent;
					}

					// Look for any associated images
					// world.
					linkMedia(world);
					return new Go(modCurrent.name()).act(modCurrent, world, input, output, injector);
				});
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
	 *            the image. A value of null will delete the image.
	 * @return true if the save succeeded, false otherwise.
	 */
	public boolean saveImage(Scene s, Game g, Image i) {

		if (i == null) {
			try {
				ZipLocker zip = new ZipLocker(adventureFile(g.name()));
				zip.delete(s.name() + ".jpeg");
				zip.close();
				s.setImage(null);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

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
				scenes.get(s).setSound(readSound(g.name(), s));
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

		if (i == null) {
			try {
				deleteSoundFile(g, s);
				s.setSound(null);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

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
			deleteSoundFile(g, s);
			String newFileName = s.name() + "." + extension;
			OutputStream out = writeImage(g.name(), newFileName);
			Files.copy(Paths.get(i), out);
			out.close();
			URI newURI = readSoundFile(g.name(), newFileName);
			s.setSound(newURI);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Effect: deletes the sound file for the specified scene from the ZIP file
	 *
	 * @param g
	 *            the game
	 *
	 * @param s
	 *            the scene
	 * @throws IOException
	 *             if there is a problem
	 */
	private void deleteSoundFile(Game g, Scene s) throws IOException {
		if (s.sound() != null) {
			ZipLocker zip = new ZipLocker(adventureFile(g.name()));
			zip.delete(getLastBitFromUrl(s.sound().toString()));
			zip.close();
		}
	}

	/**
	 * @param url
	 *            the url
	 * @return the name of the file at the end
	 */
	private static String getLastBitFromUrl(String url) {
		try {
			return URLDecoder.decode(url.replaceFirst(".*/([^/?]+).*", "$1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
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
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				// Save the current game fist
				current = saveAction().act(current, world, input, output, injector);

				world.loadGame(newGame(input, output));
				current = saveAction().act(current, world, input, output, injector);
				return new Go(world.start().name()).act(current, world, input, output, injector);
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
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				File adventureZip;
				try {
					adventureZip = new File(new URI(input.nextLine()));
				} catch (URISyntaxException e2) {
					output.println("Bad URI Syntax: " + e2.getMessage());
					return current;
				}

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
					current = new Read(readAdventureState(newAdventureName), readAdventure(newAdventureName)).act(current, world, input, output, injector);
					output.println("Imported adventure!");
					// Link the images
					linkMedia(world);
					// Change the name
					world.setName(newAdventureName);
					return new Go(current.name()).act(current, world, input, output, injector);
				} catch (IOException e) {
					output.println("Problem reading the new adventure: " + e.getMessage());
					return current;
				}
			}
		};
	}

	/**
	 * @return an action to import music
	 */
	public Action importMusic() {
		return new Action() {
			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				try {
					if (!saveSound(current, world, new URI(input.nextLine()))) {
						output.println("Failed to import music.");
					}
				} catch (URISyntaxException e) {
					output.println("Bad URI syntax: " + e.getMessage());
				}
				return current;
			}
		};
	}

	/**
	 * @return an action to import pictures
	 */
	public Action importPicture() {
		return new Action() {

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				try {
					Image i = new Image(new URI(input.nextLine()).toURL().toString());
					if (!saveImage(current, world, i)) {
						output.println("Failed to save image!");
					}
				} catch (MalformedURLException | URISyntaxException e) {
					output.println("Error importing image!");
				}
				return current;
			}
		};
	}

	/**
	 * @return an action which clears the music associated with this scene
	 */
	public Action clearMusic() {
		return new Action() {

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				saveSound(current, world, null);
				return current;
			}
		};
	}

	/**
	 * @return an action which clear the image associated with this scene
	 */
	public Action clearImage() {
		return new Action() {

			@Override
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				saveImage(current, world, null);
				return current;
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
			public Scene act(Scene current, Game world, Scanner input, PrintStream output, Consumer<String> injector) {
				// Save the current game fist
				current = saveAction().act(current, world, input, output, injector);
				try {
					PrintStream currentFile = new PrintStream(currentFile());
					currentFile.println(world.name());
					currentFile.close();
				} catch (FileNotFoundException e) {
					output.println("Something strange has happened!");
					e.printStackTrace(output);
					output.println("When you start up the program next time, it may not remember where you left off. Please send the above information to the developer.");
				}
				return new Quit().act(current, world, input, output, injector);
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
