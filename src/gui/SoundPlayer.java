package gui;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Represents: a view that plays sound, has a volume control, a progress bar,
 * and a play/pause button.
 *
 * @author Jacob Glueck
 */
public class SoundPlayer extends HBox {

	/**
	 * The string which in Font Awesome is a play button
	 */
	private static final String PLAY = "\uf04b";
	/**
	 * The string which in Font Awesome is a pause button
	 */
	private static final String PAUSE = "\uf04c";
	/**
	 * Font Awesome, which uses symbols for many different characters
	 */
	private static final Font fontAwesome = Font.loadFont(SoundPlayer.class.getResourceAsStream("fontawesome-webfont.ttf"), 20);
	/**
	 * The width of a big thing.
	 */
	private static final double BIG_WIDTH = 90;
	/**
	 * The width of a small thing.
	 */
	private static final double MEDIUM_WIDTH = 50;
	/**
	 * The width of a small thing
	 */
	private static final double SMALL_WIDTH = 30;

	/**
	 * The player which plays the media
	 */
	private MediaPlayer player;
	/**
	 * The play/pause button
	 */
	private final ToggleButton play;
	/**
	 * The progress bar
	 */
	private final ProgressBar progress;
	/**
	 * The volume slider
	 */
	private final Slider volume;
	/**
	 * The label which shows how much time has passed
	 */
	private final Label passed;
	/**
	 * The label which shows how much time is remaining
	 */
	private final Label remaining;

	/**
	 * Creates: a new sound player
	 */
	public SoundPlayer() {
		play = new ToggleButton(PLAY);
		play.selectedProperty().addListener((s, o, n) -> {
			// If we have a player
			if (player != null) {
				if (n.booleanValue()) {
					play.setText(PAUSE);
					player.play();
				} else {
					play.setText(PLAY);
					player.pause();
				}
			} else {
				play.setSelected(false);
			}
		});
		play.setFont(fontAwesome);
		play.setPrefWidth(MEDIUM_WIDTH);
		getChildren().add(play);
		HBox.setHgrow(play, Priority.NEVER);
		Label mute = new Label("\uf026");
		Label loud = new Label("\uf028");
		mute.setFont(fontAwesome);
		loud.setFont(fontAwesome);
		mute.setPrefWidth(SMALL_WIDTH);
		loud.setPrefWidth(SMALL_WIDTH);
		volume = new Slider(0, 1, 1);
		volume.setPrefWidth(BIG_WIDTH);
		mute.setAlignment(Pos.BASELINE_RIGHT);
		getChildren().add(mute);
		getChildren().add(volume);
		getChildren().add(loud);

		passed = new Label("00:00");
		passed.setAlignment(Pos.BASELINE_RIGHT);
		remaining = new Label("00:00");
		passed.setPrefWidth(MEDIUM_WIDTH);
		remaining.setPrefWidth(MEDIUM_WIDTH);
		progress = new ProgressBar(0);
		progress.setMaxWidth(Double.MAX_VALUE);
		getChildren().add(passed);
		HBox.setHgrow(passed, Priority.NEVER);
		getChildren().add(progress);
		HBox.setHgrow(progress, Priority.ALWAYS);
		getChildren().add(remaining);
		HBox.setHgrow(remaining, Priority.NEVER);
		setAlignment(Pos.CENTER_LEFT);
		setSpacing(10);
		setMaxHeight(USE_PREF_SIZE);
		setPadding(new Insets(10));
		clear();
	}

	/**
	 * Effect: loads the specified sound file
	 *
	 * @param uri
	 *            the sound file
	 * @param loop
	 *            true if the sound should loop
	 */
	public void load(URI uri, boolean loop) {
		clear();
		String decodedURIString;
		// For some mysterious reason, it gets encoded twice, so we have to
		// change it so it is only encoded once.
		// This may be related to:
		// http://stackoverflow.com/questions/9873845/java-7-zip-file-system-provider-doesnt-seem-to-accept-spaces-in-uri
		decodedURIString = uri.toString().replaceAll("%(\\d\\d)(?!\\d)", "%25$1");
		try {
			decodedURIString = URLDecoder.decode(decodedURIString, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}
		player = new MediaPlayer(new Media(decodedURIString));
		setLoop(loop);
		player.currentTimeProperty().addListener((s, o, n) -> {
			double p = n.toSeconds();
			double r = player.getCycleDuration().toSeconds() - p;
			Platform.runLater(() -> {
				passed.setText(String.format("%02d:%02d", (int) Math.floor(p / 60), (int) Math.round(p) % 60));
				remaining.setText(String.format("%02d:%02d", (int) Math.floor(r / 60), (int) Math.round(r) % 60));
				progress.setProgress(p / player.getCycleDuration().toSeconds());
			});
		});
		player.volumeProperty().bind(volume.valueProperty());
		play.setDisable(false);
	}

	/**
	 * Effect: clears the sound file
	 */
	public void clear() {
		if (player != null) {
			player.dispose();
			player = null;
		}
		play.setSelected(false);
		play.setText(PLAY);
		play.setDisable(true);
		progress.setProgress(0);
		passed.setText("00:00");
		remaining.setText("00:00");
	}

	/**
	 * Effect: sets whether the currently playing sound should loop.<br>
	 * Requires: sound must be loaded.
	 *
	 * @param loop
	 *            true if the sound should loop
	 */
	public void setLoop(boolean loop) {
		player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	}

	/**
	 * Effect: sets the volume of the sound.<br>
	 * Requires: sound must be loaded.
	 *
	 * @param v
	 *            the volume. In the range [0, 1].
	 */
	public void setVolume(double v) {
		volume.setValue(v);
	}

	/**
	 * Effect: pauses the sound.<br>
	 * Requires: sound must be loaded.
	 */
	public void pause() {
		Platform.runLater(() -> play.setSelected(false));
	}

	/**
	 * Effect: plays the sound<br>
	 * Requires: sound must be loaded.
	 */
	public void play() {
		Platform.runLater(() -> play.setSelected(true));
	}

	/**
	 * @return the length of the sound file
	 */
	public Duration length() {
		return player.getCycleDuration();
	}

	/**
	 * @return the current running time
	 */
	public Duration time() {
		return player.getCurrentTime();
	}

	/**
	 * Effect: makes the player jump to the specified time in the file.<br>
	 * Requires: sound must be loaded.
	 *
	 * @param d
	 *            the time
	 */
	public void seek(Duration d) {
		player.seek(d);
	}

}
