package hellofx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    private boolean isDone;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setTaskBox(VBox taskBox, Label titleLabel, TextArea contentArea, Label dueDateLabel, LocalDate dueDate, boolean isDone, int taskId) {
        this.taskBox = taskBox;
        this.titleLabel = titleLabel;
        this.contentArea = contentArea;
        this.dueDateLabel = dueDateLabel;
        this.initialDueDate = dueDate;
        this.isDone = isDone;
        this.taskId = taskId;

        // Set the initial values
        titleField.setText(titleLabel.getText());
        contentField.setText(contentArea.getText());
        dueDatePicker.setValue(initialDueDate);

        // Disables past dates
        disablePastDates(dueDatePicker);
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
        mainAppController.updateTaskBoxStyle(taskBox, isDone, dueDate);

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

    public static void disablePastDates(DatePicker datePicker) {
        Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: rgba(33, 0, 93, 0.25);");
                        }
                    }
                };
            }
        };
        datePicker.setDayCellFactory(dayCellFactory);
    }
}
