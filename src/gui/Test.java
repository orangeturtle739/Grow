package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Test extends Application {

	@Override
	public void start(Stage primaryStage) {
		Console c = new Console();
		primaryStage.setScene(new Scene(new BorderPane(c), 600, 600));
		primaryStage.show();
		Thread t = new Thread(() -> {
			while (true) {
				c.output().println("Hi! " + System.currentTimeMillis());
				try {
					Thread.sleep(1000);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();

		primaryStage.setOnCloseRequest(e -> Platform.exit());
	}

	public static void main(String[] args) {
		launch(args);
	}

}
