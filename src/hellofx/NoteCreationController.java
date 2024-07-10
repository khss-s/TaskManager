package hellofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NoteCreationController {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;
    @FXML
    private MainAppController mainAppController;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    public void handleSaveButtonAction(ActionEvent event) {
        String title = titleField.getText();
        String content = contentField.getText();

        // Check if title is empty and set default value
        if (title.isEmpty()) {
            title = "Title";
        }
        
        mainAppController.addNoteToContainer(title, content);

        // Close the window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
