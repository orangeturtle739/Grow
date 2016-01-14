package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import grow.GrowGame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * The main GUI application
 *
 * @author Jacob Glueck
 *
 */
public class Grow extends Application {

	/**
	 * The region which holds the image
	 */
	private Region image;
	/**
	 * The game
	 */
	private GrowGame g;
	/**
	 * The console which displays the game
	 */
	private Console c;

	@Override
	public void start(Stage primaryStage) {
		c = new Console();
		image = new HBox();
		SplitPane split = new SplitPane(image, c);
		split.setOrientation(Orientation.VERTICAL);
		split.setDividerPositions(.7, .3);
		BorderPane mainPane = new BorderPane(split);
		primaryStage.setScene(new Scene(mainPane, 1000, 1000));

		// Find the grow root directory
		if (getRoot() == null) {
			File growRoot = null;
			File defaultRoot = new File(System.getProperty("user.home"), "grow");
			if (!defaultRoot.exists() || defaultRoot.listFiles(f -> f.isDirectory() || !f.getName().startsWith(".")).length == 0) {
				growRoot = defaultRoot;
			} else {
				new Alert(AlertType.INFORMATION, "Welcome to grow! Since this is the first time you have played, could you please choose a directory for me to save your files?", ButtonType.OK)
						.showAndWait();
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setInitialDirectory(defaultRoot);
				chooser.setTitle("Choose grow home");
				while (growRoot == null) {
					growRoot = chooser.showDialog(primaryStage);
					if (growRoot != null && growRoot.exists()) {
						if (!growRoot.isDirectory()) {
							new Alert(AlertType.ERROR, "You must chose a folder!", ButtonType.OK);
							growRoot = null;
						} else if (growRoot.listFiles(f -> f.isDirectory() || !f.getName().startsWith(".")).length != 0) {
							Optional<ButtonType> result = new Alert(AlertType.CONFIRMATION, "That folder is not empty. Are you sure?", ButtonType.YES, ButtonType.NO).showAndWait();
							if (!result.isPresent() || !result.get().equals(ButtonType.YES)) {
								growRoot = null;
							}
						}
					}
				}
			}
			growRoot.mkdirs();
			try {
				setRoot(growRoot);
			} catch (BackingStoreException e1) {
				new Alert(AlertType.ERROR, "Problem saving the grow root: " + e1.getMessage() + "\n. The program will now exit.", ButtonType.OK).showAndWait();
				Platform.exit();
			}
		}

		g = new GrowGame(c.input(), c.output(), getRoot());
		// Handle drag and drop images
		image.setOnDragOver(event -> {
			if (event.getGestureSource() != image && event.getDragboard().hasFiles() || event.getDragboard().hasImage()) {
				event.acceptTransferModes(TransferMode.COPY);
			}
			event.consume();
		});
		image.setOnDragEntered(event -> {
			if (event.getGestureSource() != image && event.getDragboard().hasFiles() || event.getDragboard().hasImage()) {
				image.setEffect(new Glow());
			}
			event.consume();
		});
		image.setOnDragExited(event -> {
			if (event.getGestureSource() != image && event.getDragboard().hasFiles() || event.getDragboard().hasImage()) {
				image.setEffect(null);
			}
			event.consume();
		});
		image.setOnDragDropped(event -> {
			boolean success = false;
			if (event.getGestureSource() != image && event.getDragboard().hasFiles() || event.getDragboard().hasImage()) {
				Image i = null;
				if (event.getDragboard().hasFiles()) {
					List<File> files = event.getDragboard().getFiles();
					if (files.size() == 1) {
						try {
							i = new Image(new FileInputStream(files.get(0)));
						} catch (Exception e1) {
							success = false;
						}
					}
				} else if (event.getDragboard().hasImage()) {
					i = event.getDragboard().getImage();
				} else {
					// This should never happen
					throw new Error();
				}

				if (i == null) {
					success = false;
				} else {
					success = g.saveImage(i);
				}
				if (success) {
					setBackground(image, i);
				}
			}
			event.setDropCompleted(success);
			event.consume();
		});

		GameThread gameThread = new GameThread();
		gameThread.start();
		Platform.setImplicitExit(false);
		primaryStage.setOnCloseRequest(e -> {
			if (!gameThread.inject(":quit")) {
				// We cannot just quit because they might be editing something.
				new Alert(AlertType.ERROR, "You must finish editing before quitting.", ButtonType.OK).showAndWait();
			}
			e.consume();
		});

		// Handle drag and drop adventures
		c.setOnDragDetected((e) -> {
			if (gameThread.inject(":save")) {
				Dragboard d = c.startDragAndDrop(TransferMode.COPY);
				ClipboardContent content = new ClipboardContent();
				content.putFiles(Arrays.asList(g.adventureFile()));
				d.setContent(content);
				e.consume();
			}
		});
		c.setOnDragOver(event -> {
			if (event.getGestureSource() != c && event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY);
			}
			event.consume();
		});
		c.setOnDragEntered(event -> {
			if (event.getGestureSource() != c && event.getDragboard().hasFiles()) {
				c.setEffect(new Glow());
			}
			event.consume();
		});
		c.setOnDragExited(event -> {
			if (event.getGestureSource() != c && event.getDragboard().hasFiles()) {
				c.setEffect(null);
			}
			event.consume();
		});
		c.setOnDragDropped(event -> {
			File newAdventure = null;
			if (event.getGestureSource() != c && event.getDragboard().hasFiles()) {
				if (event.getDragboard().hasFiles()) {
					List<File> files = event.getDragboard().getFiles();
					if (files.size() == 1) {
						try {
							newAdventure = files.get(0);
							if (!newAdventure.getName().endsWith(".zip")) {
								newAdventure = null;
							}
						} catch (Exception e1) {
							newAdventure = null;
						}
					}
				}
			}

			if (newAdventure != null) {
				final File realName = newAdventure;
				if (gameThread.inject(":import\n" + realName.getAbsolutePath().toString())) {
					event.setDropCompleted(true);
				} else {
					event.setDropCompleted(false);
				}
			}

			event.consume();
		});

		primaryStage.setTitle("Grow");

		primaryStage.show();
	}

	/**
	 * Effect: sets the background image of a region to be white with a
	 * specified image on top of it.
	 *
	 * @param n
	 *            the node
	 * @param i
	 *            the image
	 */
	private void setBackground(Region n, Image i) {
		BackgroundImage back = new BackgroundImage(i, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, false));
		Platform.runLater(() -> n.setBackground(new Background(new BackgroundFill[] { new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY) }, new BackgroundImage[] { back })));
	}

	/**
	 * With no arguments, launches a GUI Grow game. With the single flag
	 * {@code -t}, launches a text only version.
	 *
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {
		try {
			Map<String, List<String>> a = processArgs(args, "-t", "--grow-root", "--reset-root", "--set-root", "--help");

			if (a.containsKey("--set-root")) {
				testSize(a, 1, "--set-root must be the only flag.");
				testSize(a.get("--set-root"), 1, "--set-root can only have one argument");
				File root = new File(a.get("--set-root").get(0));
				root.mkdirs();
				setRoot(root);
				return;
			}
			if (a.containsKey("--reset-root")) {
				testSize(a, 1, "--reset-root must be the only flag.");
				testSize(a.get("--reset-root"), 0, "--reset-root takes no arguments");
				resetRoot();
				return;
			}
			if (a.containsKey("--help")) {
				testSize(a, 1, "--help must be the only flag");
				testSize(a.get("--help"), 0, "--help takes no arguments");
				System.out.println("Usage:");
				System.out.println("Special options, must be used alone:");
				System.out.println("--reset-root");
				System.out.println("\tDeletes the location of the grow root. When you run grow again, it will pick a new root.");
				System.out.println("--set-root <path to root folder>");
				System.out.println("\tSets the grow root to the specified folder. The folder will be made if it does not exist.");
				System.out.println("--help");
				System.out.println("\tPrints the helpful messages that are currently being printed.");
				System.out.println("Normal flags (may be used in combination with each other, in any order):");
				System.out.println("-t");
				System.out.println("\tStarts grow in text mode.");
				System.out.println("--grow-root <path to root folder>");
				System.out.println("\tSets the grow root to the specified folder for the current session.");
				return;
			}

			File growRoot = null;
			if (a.containsKey("--grow-root")) {
				testSize(a.get("--grow-root"), 1, "--grow-root can only have one argument");
				growRoot = new File(a.get("--grow-root").get(0));
				growRoot.mkdirs();
			}
			if (a.containsKey("-t")) {
				growRoot = growRoot == null ? getRoot() : growRoot;
				if (growRoot == null) {
					growRoot = new File(System.getProperty("user.home"), "grow");
					setRoot(growRoot);
					growRoot.mkdirs();
					System.out.println("Using " + growRoot + " to store grow files..");
				}
				GrowGame g = new GrowGame(new Scanner(System.in), new PrintStream(System.out), growRoot);
				g.init();
				g.play();
			} else {
				File oldRoot = getRoot();
				if (growRoot != null) {
					setRoot(growRoot);
				}
				launch();
				if (oldRoot != null) {
					setRoot(oldRoot);
				}
			}
		} catch (ArgumentException e) {
			System.err.println("Bad arguments: " + e.getMessage());
		} catch (BackingStoreException e) {
			System.err.println("Problem with backing store.");
			e.printStackTrace();
		}
	}

	/**
	 * Effect: stores the file as the grow root file in persistent storage.
	 *
	 * @param f
	 *            the file
	 * @throws BackingStoreException
	 *             if there is a problem storing the file.
	 */
	private static void setRoot(File f) throws BackingStoreException {
		Preferences pref = Preferences.systemNodeForPackage(Grow.class);
		pref.put("grow.root", f.getAbsolutePath());
		pref.flush();
	}

	/**
	 * Effect: gets the root file from persistent storage.
	 *
	 * @return the file, or null if it was not there
	 */
	private static File getRoot() {
		Preferences pref = Preferences.systemNodeForPackage(Grow.class);
		String str = pref.get("grow.root", null);
		return str == null ? null : new File(str);
	}

	/**
	 * Effect: deletes the root file from persistent storage.
	 *
	 * @throws BackingStoreException
	 *             if there is a problem
	 */
	private static void resetRoot() throws BackingStoreException {
		Preferences pref = Preferences.systemNodeForPackage(Grow.class);
		pref.clear();
	}

	/**
	 * Effect: if the size of the map is not equal to {@code size}, throws an
	 * {@link ArgumentException} with the specified message
	 *
	 * @param m
	 *            the map
	 * @param size
	 *            the desired size
	 * @param error
	 *            the error message
	 * @throws ArgumentException
	 *             if the size is not right
	 */
	private static void testSize(Map<?, ?> m, int size, String error) throws ArgumentException {
		if (m.size() != size) {
			throw new ArgumentException(error);
		}
	}

	/**
	 * Effect: if the size of the collection is not equal to {@code size},
	 * throws an {@link ArgumentException} with the specified message
	 *
	 * @param m
	 *            the collection
	 * @param size
	 *            the desired size
	 * @param error
	 *            the error message
	 * @throws ArgumentException
	 *             if the size is not right
	 */
	private static void testSize(Collection<?> m, int size, String error) throws ArgumentException {
		if (m.size() != size) {
			throw new ArgumentException(error);
		}
	}

	/**
	 * Creates: a map of flags to their options.
	 *
	 * @param args
	 *            the arguments to process
	 * @param f
	 *            the flags
	 * @return a map of the flags to all the things that are not flags that
	 *         occurred after them and before the next flag in the list of
	 *         arguments
	 * @throws ArgumentException
	 *             if there is a problem
	 */
	private static Map<String, List<String>> processArgs(String[] args, String... f) throws ArgumentException {
		Iterator<String> iter = Arrays.asList(args).iterator();
		Set<String> flags = new HashSet<>(Arrays.asList(f));
		Map<String, List<String>> result = new HashMap<>();
		String last = null;
		while (iter.hasNext()) {
			String next = iter.next();
			if (flags.contains(next)) {
				if (result.containsKey(next)) {
					throw new ArgumentException("Duplicate flag: " + next);
				} else {
					result.put(next, new LinkedList<>());
					last = next;
				}
			} else if (last != null) {
				result.get(last).add(next);
			} else {
				throw new ArgumentException("Unrecognized flag: " + next);
			}
		}
		return result;
	}

	/**
	 * Represents: a problem parsing command line arguments.
	 *
	 * @author Jacob Glueck
	 */
	private static class ArgumentException extends Exception {
		/**
		 * Default UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates: a new exception with the specified message.
		 *
		 * @param str
		 *            the message
		 */
		public ArgumentException(String str) {
			super(str);
		}
	}

	/**
	 * Represents: a thread which runs the grow game.
	 *
	 * @author Jacob Glueck
	 */
	private class GameThread extends Thread {

		/**
		 * True if something can currently be injected
		 */
		private final AtomicBoolean canInject;
		/**
		 * The current injection
		 */
		private final AtomicReference<String> injection;

		/**
		 * Creates: a new daemon game thread
		 */
		public GameThread() {
			canInject = new AtomicBoolean(false);
			injection = new AtomicReference<String>(null);
			setDaemon(true);
		}

		@Override
		public void run() {
			Consumer<Image> displayer = (f) -> {
				if (f != null) {
					try {
						setBackground(image, f);
						return;
					} catch (Exception e1) {
						System.out.println("Image not found: " + f);
					}
				}
				setBackground(image, new Image(Grow.class.getResourceAsStream("default.jpeg")));
			};
			g.init(displayer);
			ExecutorService reader = Executors.newFixedThreadPool(1, r -> {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			});
			String line;
			do {
				synchronized (canInject) {
					canInject.set(true);
				}
				Future<?> waitForInjection = reader.submit(() -> {
					try {
						synchronized (injection) {
							String toInject = null;
							while (toInject == null) {
								injection.wait();
								toInject = injection.get();
								// Make sure the injection is marked as used.
								injection.set(null);
							}
							c.simulateInput(toInject);
						}
					} catch (InterruptedException e) {
						// It does not matter if we are interrupted. We can
						// just exit. If there is something in injection
						// after we terminate, it will be used instead of
						// the user input.
					}
				});
				line = c.input().nextLine();
				// This code is complicated, but correct. If an injection in
				// initiated, waking up the thread above, but stops before the
				// "toInject = injection.get()" executes, and execution returns
				// to this thread, in which next line returns, the code will
				// prefer any value currently stored in toInject, and prevent
				// any more values from being stored before reading the value.
				synchronized (canInject) {
					canInject.set(false);
				}
				waitForInjection.cancel(true);
				synchronized (injection) {
					line = injection.get() == null ? line : injection.get();
				}
			} while (g.doTurn(line, displayer));
			Platform.exit();
		}

		/**
		 * Attempts to inject text into the game's input stream.
		 *
		 * @param str
		 *            the string
		 * @return true if the text was injected, false otherwise (if the game
		 *         could not accept commands).
		 */
		public boolean inject(String str) {
			synchronized (canInject) {
				if (canInject.get()) {
					synchronized (injection) {
						injection.set(str);
						injection.notifyAll();
					}
					canInject.set(false);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}