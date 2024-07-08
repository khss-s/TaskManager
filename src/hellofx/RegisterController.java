package hellofx;

//import com.mysql.cj.xdevapi.Statement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class RegisterController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private PasswordField confirmPasswordPasswordField;
    @FXML
    private Label signinMessageLabel;
    
    public void signinButtonOnAction(ActionEvent e) throws IOException {
        if (usernameTextField.getText().isBlank() == false && passwordPasswordField.getText().isBlank() == false && confirmPasswordPasswordField.getText().isBlank() == false) {
            if (passwordPasswordField.getText().equals(confirmPasswordPasswordField.getText())) {
                ValidateSignin(e);
            } else {
                signinMessageLabel.setText("Password does not match.");
            }
        } else {
            signinMessageLabel.setText("All fields must be filled in.");
        }
    }

    public void ValidateSignin(ActionEvent event) {
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.connectToDB();

        try {
            PreparedStatement pstmt = connectDB.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)");
            pstmt.setString(1, usernameTextField.getText());
            pstmt.setString(2, passwordPasswordField.getText());
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                goToLogin(event);
            } else {
                signinMessageLabel.setText("Failed to register user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToLogin(ActionEvent e) throws IOException {
        Parent loginPage = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene loginScene = new Scene(loginPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.show();
    }
}
