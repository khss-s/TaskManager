package hellofx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
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
    private DatePicker dueDatePicker;
    @FXML
    private MainAppController mainAppController;
    @FXML
    private VBox taskBox;
    @FXML
    private Label titleLabel;
    @FXML
    private TextArea contentArea;
    @FXML
    private Label dueDateLabel;

    private int taskId;
    private LocalDate initialDueDate;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setTaskBox(VBox taskBox, Label titleLabel, TextArea contentArea, Label dueDateLabel, LocalDate dueDate, int taskId) {
        this.taskBox = taskBox;
        this.titleLabel = titleLabel;
        this.contentArea = contentArea;
        this.dueDateLabel = dueDateLabel;
        this.initialDueDate = dueDate;
        this.taskId = taskId;

        // Set the initial values
        titleField.setText(titleLabel.getText());
        contentField.setText(contentArea.getText());
        dueDatePicker.setValue(initialDueDate);
    }

    @FXML
    public void handleSaveButtonAction(ActionEvent event) {
        String title = titleField.getText();
        String content = contentField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        // Check if title is empty and set default value
        if (title.isEmpty()) {
            title = "Title";
        }

        // Update task in database
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "UPDATE tasks SET title = ?, content = ?, due_date = ? WHERE task_id = ?";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setObject(3, dueDate);
            pstmt.setInt(4, taskId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update the task box
        titleLabel.setText(title);
        contentArea.setText(content);
        dueDateLabel.setText("Due date: " + (dueDate != null ? dueDate.toString() : "No due date"));

        // Close the window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleDeleteButtonAction(ActionEvent event) {
        // Delete task from database
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "DELETE FROM tasks WHERE task_id = ?";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Remove the task box from the main application
        mainAppController.deleteTaskFromContainer(taskBox);

        // Close the window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
