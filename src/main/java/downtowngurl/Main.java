package downtowngurl;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Starts the JavaFX graphical interface for Downtown Gurl.
 */
public class Main extends Application {

    /**
     * Sets up and shows the primary application window.
     *
     * @param stage primary window provided by JavaFX.
     */
    @Override
    public void start(Stage stage) {
        Label helloWorld = new Label("Hello World!");
        Scene scene = new Scene(helloWorld, 400, 200);

        stage.setTitle("Downtown Gurl");
        stage.setScene(scene);
        stage.show();
    }
}
