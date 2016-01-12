package gui;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
		primaryStage.setScene(new Scene(new BorderPane(split), 1000, 1000));
		primaryStage.show();
		g = new GrowGame(c.input(), c.output());

		// Handle drag and drop images
		image.setOnDragOver(event -> {
			if (event.getGestureSource() != image && event.getDragboard().hasFiles() || event.getDragboard().hasImage()) {
				event.acceptTransferModes(TransferMode.COPY);
			} else if (event.getGestureSource() != image && event.getDragboard().hasString()) {
				System.out.println(event.getDragboard().getString());
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
		primaryStage.setTitle("Grow");
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
		if (Arrays.equals(args, new String[] { "-t" })) {
			new GrowGame(new Scanner(System.in), System.out).play();
		} else {
			launch(args);
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
