package gui;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLConnection;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import grow.GrowGame;
import grow.MediaProcessor;
import grow.StatusUpdater;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
	 * The sound player
	 */
	private SoundPlayer player;
	/**
	 * The game
	 */
	private GrowGame g;
	/**
	 * The console which displays the game
	 */
	private Console c;

	/**
	 * The label for the current adventure name
	 */
	private Label adventureName;
	/**
	 * The label for the current scene
	 */
	private Label adventureScene;
	/**
	 * The label for the drag and drop
	 */
	private Label dragAndDrop;
	/**
	 * The game thread
	 */
	private GameThread gameThread;

	@Override
	public void start(Stage primaryStage) {
		c = new Console();
		image = new HBox();
		player = new SoundPlayer();
		SplitPane mediaPane = new SplitPane(player, c);
		mediaPane.setOrientation(Orientation.VERTICAL);
		SplitPane split = new SplitPane(image, mediaPane);
		split.setOrientation(Orientation.VERTICAL);
		split.setDividerPositions(.5, .5);
		BorderPane mainPane = new BorderPane(split);
		GridPane bottomGrid = new GridPane();
		adventureName = new Label();
		adventureScene = new Label();
		dragAndDrop = new Label();
		dragAndDrop.setEffect(new DropShadow());
		bottomGrid.add(new ToolBar(labeledLabel("Adventure", adventureName)), 0, 0);
		bottomGrid.add(new ToolBar(labeledLabel("Scene", adventureScene)), 1, 0);
		bottomGrid.add(new ToolBar(labeledLabel("Share", dragAndDrop)), 2, 0);
		ColumnConstraints cons = new ColumnConstraints(0, 100, Double.MAX_VALUE);
		cons.setHgrow(Priority.ALWAYS);
		bottomGrid.getColumnConstraints().addAll(cons, cons, cons);

		mainPane.setBottom(bottomGrid);
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

		gameThread = new GameThread();
		gameThread.start();
		Platform.setImplicitExit(false);
		primaryStage.setOnCloseRequest(e -> {
			if (!gameThread.inject(":quit")) {
				// We cannot just quit because they might be editing something.
				Alert a = new Alert(AlertType.WARNING,
						"You are currently editing your adventure. In order to save your progress and exit, you must finish your edit and then exit. If you exit now, you will loose your edits, and your adventure may become corrupted. Would you like to exit now?",
						ButtonType.YES, ButtonType.NO);

				// Deactivate Default behavior for yes-Button:
				Button yesButton = (Button) a.getDialogPane().lookupButton(ButtonType.YES);
				yesButton.setDefaultButton(false);
				// Activate Default behavior for no-Button:
				Button noButton = (Button) a.getDialogPane().lookupButton(ButtonType.NO);
				noButton.setDefaultButton(true);
				Optional<ButtonType> result = a.showAndWait();
				if (result.isPresent() && result.get().equals(ButtonType.YES)) {
					Platform.exit();
				}
			}
			e.consume();
		});

		// Handle drag and drop images
		configureDrop(image, "import image", () -> {
		});

		// Handle drag and drop adventures
		dragAndDrop.setOnDragDetected((e) -> {
			if (gameThread.inject(":save")) {
				Dragboard d = c.startDragAndDrop(TransferMode.COPY);
				ClipboardContent content = new ClipboardContent();
				content.putFiles(Arrays.asList(g.adventureFile()));
				d.setContent(content);
				e.consume();
			}
		});
		configureDrop(c, "import adventure", () -> {
		});

		// Handle drag and drop music
		configureDrop(player, "import music", () -> player.clear());

		primaryStage.setTitle("Grow");

		primaryStage.show();
	}

	/**
	 * Effect: configures the region to accept a file dropped onto it by
	 * executing the specified command in the grow game with the dropped file's
	 * URI as an argument. The program executes the action before executing the
	 * command.
	 *
	 * @param r
	 *            the region
	 * @param command
	 *            the command for the grow game. Should not start with
	 *            {@code ":"}; one will be added.
	 * @param action
	 *            the action to run just before the command
	 */
	private void configureDrop(Region r, String command, Runnable action) {
		r.setOnDragOver(event -> {
			if (event.getGestureSource() != r && event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY);
			}
			event.consume();
		});
		r.setOnDragEntered(event -> {
			if (event.getGestureSource() != r && event.getDragboard().hasFiles()) {
				r.setEffect(new Glow());
			}
			event.consume();
		});
		r.setOnDragExited(event -> {
			if (event.getGestureSource() != r && event.getDragboard().hasFiles()) {
				r.setEffect(null);
			}
			event.consume();
		});
		r.setOnDragDropped(event -> {
			File newSound = null;
			if (event.getGestureSource() != r && event.getDragboard().hasFiles()) {
				if (event.getDragboard().hasFiles()) {
					List<File> files = event.getDragboard().getFiles();
					if (files.size() == 1) {
						try {
							newSound = files.get(0);
						} catch (Exception e1) {
							newSound = null;
						}
					}
				}
			}

			boolean completed = false;
			if (newSound != null) {
				action.run();
				if (gameThread.inject(":" + command + "\n" + newSound.toURI())) {
					completed = true;
				} else {
					new Alert(AlertType.ERROR, "You must finish editing before trying to drag and drop.", ButtonType.OK).showAndWait();
				}
			}
			event.setDropCompleted(completed);
			event.consume();
		});
	}

	/**
	 * Creates: a label with a label to its left.
	 *
	 * @param text
	 *            the text in the label. A {@code ": "} will be appended.
	 * @param label
	 *            the label to label.
	 * @return the labeled label.
	 */
	private static Node labeledLabel(String text, Label label) {
		HBox h = new HBox();
		Label l = new Label(text + ": ");
		h.getChildren().add(l);
		HBox.setHgrow(l, Priority.NEVER);
		h.getChildren().add(label);
		HBox.setHgrow(label, Priority.ALWAYS);
		return h;
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
	private static void setBackground(Region n, Image i) {
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
		doMain(args);
		System.exit(0);
	}

	/**
	 * Effect: runs all the main stuff
	 *
	 * @param args
	 *            the arguments
	 */
	private static void doMain(String[] args) {

		// There is a bug in the JavaFX Media library, caused by a problem in
		// the URLConnection class. The URLConnection class by default will use
		// caches, so if I edit a jar file, and put a new sound file inside,
		// then the URLConnection fails to see the new sound file, and the Media
		// class plays the old one. To fix this, I change the default use caches
		// to always be false.
		try {
			Field f = URLConnection.class.getDeclaredField("defaultUseCaches");
			f.setAccessible(true);
			f.set(null, false);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}

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
		Preferences pref = Preferences.userNodeForPackage(Grow.class);
		pref.put("grow.root", f.getAbsolutePath());
		pref.flush();
	}

	/**
	 * Effect: gets the root file from persistent storage.
	 *
	 * @return the file, or null if it was not there
	 */
	private static File getRoot() {
		Preferences pref = Preferences.userNodeForPackage(Grow.class);
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
		Preferences pref = Preferences.userNodeForPackage(Grow.class);
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
			MediaProcessor processor = new MediaProcessor() {

				@Override
				public void process(URI sound) {
					Platform.runLater(() -> {
						if (sound != null) {
							player.load(sound, true);
							player.play();
						} else {
							player.clear();
						}
					});
				}

				@Override
				public void process(Image f) {
					Platform.runLater(() -> {
						if (f != null) {
							try {
								setBackground(image, f);
								return;
							} catch (Exception e1) {
								System.out.println("Image not found: " + f);
							}
						}
						setBackground(image, new Image(Grow.class.getResourceAsStream("default.jpeg")));
					});
				}
			};
			StatusUpdater u = (a, s) -> {
				Platform.runLater(() -> {
					adventureName.setText(a);
					adventureScene.setText(s);
					dragAndDrop.setText(a + ".zip");
				});
			};
			g.init(processor, u);
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
			} while (g.doTurn(line, processor, u));
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
