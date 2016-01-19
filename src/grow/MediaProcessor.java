package grow;

import java.net.URI;

import javafx.scene.image.Image;

/**
 * A function which processes media from a Grow game.
 *
 * @author Jacob Glueck
 *
 */
public interface MediaProcessor {
	/**
	 * An empty media processor. The methods do nothing.
	 */
	public static final MediaProcessor EMPTY = new MediaProcessor() {

		@Override
		public void process(URI sound) {
		}

		@Override
		public void process(Image i) {
		}
	};

	/**
	 * Effect: displays the image
	 * 
	 * @param i
	 *            the image
	 */
	void process(Image i);

	/**
	 * Effect: plays the sound
	 * 
	 * @param sound
	 *            the sound
	 */
	void process(URI sound);
}
