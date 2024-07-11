package hellofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TaskEditingController {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;
    @FXML
    private MainAppController mainAppController;
    @FXML
    private VBox taskBox;
    @FXML
    private Label titleLabel;
    @FXML
    private TextArea contentArea;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setTaskBox(VBox taskBox, Label titleLabel, TextArea contentArea) {
        this.taskBox = taskBox;
        this.titleLabel = titleLabel;
        this.contentArea = contentArea;

        // Set the initial values
        titleField.setText(titleLabel.getText());
        contentField.setText(contentArea.getText());
    }

    @FXML
    public void handleSaveButtonAction(ActionEvent event) {
        String title = titleField.getText();
        String content = contentField.getText();

        // Check if title is empty and set default value
        if (title.isEmpty()) {
            title = "Title";
        }

        // Update the task box
        titleLabel.setText(title);
        contentArea.setText(content);

        // Close the window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleDeleteButtonAction(ActionEvent event) {
        // Remove the task box from the main application
        mainAppController.deleteTaskFromContainer(taskBox);

        // Close the window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
