package gui;

import java.net.URI;

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

public class SoundPlayer extends HBox {

	private static final String PLAY = "\uf04b";
	private static final String PAUSE = "\uf04c";
	private static final Font fontAwesome = Font.loadFont(SoundPlayer.class.getResourceAsStream("fontawesome-webfont.ttf"), 20);
	private static final double BIG_WIDTH = 90;
	private static final double MEDIUM_WIDTH = 50;
	private static final double SMALL_WIDTH = 30;

	private MediaPlayer player;
	private final ToggleButton play;
	private final ProgressBar progress;
	private final Slider volume;
	private final Label passed;
	private final Label remaining;

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
		setDisable(true);
	}

	public void load(URI uri, boolean loop) {
		clear();
		player = new MediaPlayer(new Media(uri.toString()));
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
		setDisable(false);
	}

	public void clear() {
		if (player != null) {
			pause();
			player.dispose();
			player = null;
			setDisable(true);
		}
	}

	public void setLoop(boolean loop) {
		player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	}

	public void setVolume(double v) {
		volume.setValue(v);
	}

	public void pause() {
		Platform.runLater(() -> play.setSelected(false));
	}

	public void play() {
		Platform.runLater(() -> play.setSelected(true));
	}

	public Duration length() {
		return player.getCycleDuration();
	}

	public Duration time() {
		return player.getCurrentTime();
	}

	public void seek(Duration d) {
		player.seek(d);
	}

}
