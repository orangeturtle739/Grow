package gui;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
	@Override
	public void start(Stage primaryStage) {
		Console c = new Console();
		HBox image = new HBox();
		SplitPane split = new SplitPane(image, c);
		split.setOrientation(Orientation.VERTICAL);
		split.setDividerPositions(.7, .3);
		primaryStage.setScene(new Scene(new BorderPane(split), 1000, 1000));
		primaryStage.show();
		GrowGame g = new GrowGame(c.input(), c.output());

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

		Thread gameThread = new Thread(() -> {
			g.play((f) -> {
				if (f != null) {
					try {
						setBackground(image, new Image(new FileInputStream(f)));
						return;
					} catch (Exception e1) {
						System.out.println("Image not found: " + f);
					}
				}
				setBackground(image, new Image(Grow.class.getResourceAsStream("default.jpeg")));
			});
			Platform.exit();
		});
		gameThread.setDaemon(true);
		gameThread.start();
		Platform.setImplicitExit(false);
		primaryStage.setOnCloseRequest(e -> {
			// We cannot just quit because they might be editing something.
			new Alert(AlertType.CONFIRMATION, "To quit, type \":quit\"", ButtonType.OK).showAndWait();
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
		n.setBackground(new Background(new BackgroundFill[] { new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY) }, new BackgroundImage[] { back }));
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
}
