package gui;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
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
		// Console c = new Console();
		String f = Font.loadFont(Console.class.getResource("UbuntuMono-R.ttf").toExternalForm(), 18).getFamily();
		System.out.println(f);
		WebView w = new WebView();
		w.getEngine().loadContent(
				"<html> <head> <style> p { font-family: \"Ubuntu Mono\"; font-size: 18pt; display: block; margin-top: 0em; margin-bottom: 0em; margin-left: 0; margin-right: 0; color: #00FF00; } </style> </head> <body>  <div id='content'> </div> </body> </html>");
		Platform.runLater(() -> w.getEngine()
				.executeScript("var para = document.createElement('p'); para.appendChild(document.createTextNode('World!')); document.getElementById('content').appendChild(para);"));
		Platform.runLater(() -> w.getEngine().executeScript(
				"var para = document.createElement('p'); para.appendChild(document.createTextNode('')); para.style.color = '#FF0000'; document.getElementById('content').appendChild(para);"));
		Platform.runLater(() -> w.getEngine().executeScript("document.getElementById('content').lastChild.appendChild(document.createTextNode('+Bob'));"));
		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			Platform.runLater(() -> {
				try {
					printDocument(w.getEngine().getDocument(), System.out);
				} catch (Exception e1) {
				}
			});
		}).start();
		primaryStage.setScene(new Scene(new BorderPane(w), 600, 600));
		primaryStage.show();
		// Thread t = new Thread(() -> {
		// while (true) {
		// c.output().println("Hi! " + System.currentTimeMillis());
		// try {
		// Thread.sleep(1000);
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }
		// }
		// });
		// t.setDaemon(true);
		// t.start();

		primaryStage.setOnCloseRequest(e -> Platform.exit());
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
