package downtowngurl.ui;

import downtowngurl.DowntownGurl;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private DowntownGurl downtownGurl;

    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private final Image downtownGurlImage =
            new Image(this.getClass().getResourceAsStream("/images/DaDowntownGurl.png"));

    /**
     * Initializes the controller after its FXML fields are loaded.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Injects the Downtown Gurl instance used to generate chatbot responses.
     *
     * @param downtownGurl chatbot instance used by this window.
     */
    public void setDowntownGurl(DowntownGurl downtownGurl) {
        this.downtownGurl = downtownGurl;
    }

    /**
     * Shows the user's message and Downtown Gurl's response, then clears the input box.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = downtownGurl.getResponse(input);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getDowntownGurlDialog(response, downtownGurlImage)
        );
        userInput.clear();
    }
}
