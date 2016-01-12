package gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Represents: a node which has an input stream and an output stream, which
 * works like a terminal
 *
 * @author Jacob Glueck
 *
 */
public class Console extends VBox {

	/**
	 * The color of the text that the user types
	 */
	private static final Color inputColor = Color.LIMEGREEN;
	/**
	 * The color of all the other text
	 */
	private static final Paint normalColor = new Text().getFill();
	/**
	 * The font used
	 */
	private static final Font font = Font.loadFont(Console.class.getResourceAsStream("UbuntuMono-R.ttf"), 18);

	/**
	 * The output stream, which writes to the window in {@link #current}.
	 */
	private final OutputStream echo;
	/**
	 * The print stream which writes to {@link #echo}
	 */
	private final PrintStream echoPrint;
	/**
	 * The output stream that the client uses. Writes {@link InputStream}.
	 * {@link #normalColor}.
	 */
	private final OutputStream output;
	/**
	 * The print stream that the client uses.
	 */
	private final PrintStream outputPrint;

	/**
	 * The input stream which the client can read
	 */
	private final InputStream input;
	/**
	 * The input buffer. This stuff has to be written to the input stream.
	 */
	private final BlockingQueue<Byte> inputBuffer;
	/**
	 * The scanner, which the client uses to read the input stream.
	 */
	private final Scanner scanner;

	/**
	 * The current color of the text
	 */
	private Paint current;

	/**
	 * The text view
	 */
	private final TextFlow view;
	/**
	 * The field in which the client types input
	 */
	private final TextField inputField;

	/**
	 * An object used to ensure that only one thread writes to the view at the
	 * same time.
	 */
	private final Object viewLock;

	/**
	 * Creates: a new, empty, console.
	 */
	public Console() {
		view = new TextFlow();
		view.setPadding(new Insets(5));
		Background white = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
		view.setBackground(white);
		inputField = new TextField();
		inputField.setFont(font);
		inputField.setStyle("-fx-text-fill: " + toRGBCode(inputColor) + ";");

		ScrollPane scroll = new ScrollPane(view);
		scroll.setFitToHeight(true);
		scroll.setFitToWidth(true);
		// Always scroll to the bottom
		view.heightProperty().addListener((observable, oldValue, newValue) -> scroll.setVvalue(1));
		getChildren().add(scroll);
		VBox.setVgrow(scroll, Priority.ALWAYS);
		getChildren().add(inputField);
		VBox.setVgrow(inputField, Priority.NEVER);

		current = inputColor;

		viewLock = new Object();
		echo = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				synchronized (viewLock) {
					Text t = new Text(Character.toString((char) b));
					t.setFont(font);
					t.setFill(current);
					Platform.runLater(() -> view.getChildren().add(t));
				}
			}
		};
		echoPrint = new PrintStream(echo);
		output = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				synchronized (viewLock) {
					// Switch to the normal color before writing, and then
					// switch
					// back.
					current = normalColor;
					echo.write(b);
					current = inputColor;
				}
			}
		};
		outputPrint = new PrintStream(output);

		inputBuffer = new LinkedBlockingQueue<>();
		input = new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					return inputBuffer.take();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public int read(byte b[], int off, int len) throws IOException {
				// This is just like the default method, except that it uses
				// take (to wait) on the first byte and poll (to not wait) on
				// all the other bytes. If you do not do this, then a scanner
				// will try to fill a big buffer and wait forever.
				if (b == null) {
					throw new NullPointerException();
				} else if (off < 0 || len < 0 || len > b.length - off) {
					throw new IndexOutOfBoundsException();
				} else if (len == 0) {
					return 0;
				}

				int c = read();
				if (c == -1) {
					return -1;
				}
				b[off] = (byte) c;

				int i = 1;
				for (; i < len; i++) {
					Byte poll = inputBuffer.poll();
					c = poll == null ? -1 : (byte) poll;
					if (c == -1) {
						break;
					}
					b[off + i] = (byte) c;
				}
				return i;
			}
		};
		scanner = new Scanner(input);
		inputField.setOnAction((e) -> {
			String text = inputField.getText();
			// We must print the text before forwarding it to ensure that the
			// text appears before any response.
			echoPrint.println(text);
			for (int x = 0; x < text.length(); x++) {
				try {
					inputBuffer.put((byte) text.charAt(x));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			try {
				inputBuffer.put((byte) '\n');
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			inputField.clear();
		});
	}

	/**
	 * @return a scanner which reads user input
	 */
	public Scanner input() {
		return scanner;
	}

	/**
	 * @return a print stream which prints to the console
	 */
	public PrintStream output() {
		return outputPrint;
	}

	/**
	 * Effect: clears the text entry field, and writes the specified string
	 * followed by a new line, as if the user entered it.
	 *
	 * @param str
	 *            the string to write
	 */
	public void simulateInput(String str) {
		Platform.runLater(() -> {
			// Handle multi-line input properly
			String[] split = str.split("\\R");
			for (String line : split) {
				inputField.setText(line);
				inputField.getOnAction().handle(null);
			}
		});
	}

	/**
	 * Converts a color to a hex code. From:
	 *
	 * @param color
	 *            the color
	 * @return the hex code
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color">
	 *      http://stackoverflow.com/questions/17925318/how-to-get-hex-web-
	 *      string-from-javafx-colorpicker-color</a>
	 */
	private static String toRGBCode(Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
	}
}