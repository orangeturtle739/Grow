package gui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A testing class.
 *
 * @author Jacob Glueck
 *
 */
public class Test extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setOnCloseRequest(e -> Platform.exit());
		SoundPlayer p = new SoundPlayer();
		p.load(new File("/Users/jacob/Music/iTunes/iTunes Music/Music/Alexandre Desplat/The Imitation Game (Original Motion Picture S/06 Mission.mp3").toURI(), true);
		p.play();
		p.setVolume(1);
		primaryStage.setScene(new Scene(p, 600, 600));
		primaryStage.show();
	}

	/**
	 * Fun!
	 *
	 * @param doc
	 *            fun
	 * @param out
	 *            fun
	 * @throws IOException
	 *             fun
	 * @throws TransformerException
	 *             fun
	 */
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	/**
	 * Runs the test
	 *
	 * @param args
	 *            super cool
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
