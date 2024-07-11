package hellofx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TaskCreationController {
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

        int taskId = 0;
        int userId = LoginState.getUserId();

        // Insert task into database
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "INSERT INTO tasks (user_id, title, content, is_done) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connectDB.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                taskId = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mainAppController.addTaskToContainer(taskId, title, content, false);

        // Close the window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
