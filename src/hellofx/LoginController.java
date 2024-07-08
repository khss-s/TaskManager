package hellofx;

//import javax.swing.Action;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private Label loginMessageLabel;    

    public void loginButtonOnAction(ActionEvent e) throws IOException {
        if (usernameTextField.getText().isBlank() == false && passwordPasswordField.getText().isBlank() == false) {
            ValidateLogin(e);
        } else {
            loginMessageLabel.setText("Both fields must be filled in.");
        }
    }

    public void ValidateLogin(ActionEvent event) {
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.connectToDB();

        try {
            Statement stmt = connectDB.createStatement();
            ResultSet result = stmt.executeQuery("SELECT user_id FROM users WHERE username = '" + usernameTextField.getText() + "' AND password = '" + passwordPasswordField.getText() + "'");
    
            if (result.next()) {
                int userId = result.getInt("user_id");
                LoginState.saveLoginState(true);
                LoginState.saveUserId(userId);
                goToMainApp(event);
            } else {
                loginMessageLabel.setText("Invalid login. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToSignin(ActionEvent e) throws IOException {
        Parent registerPage = FXMLLoader.load(getClass().getResource("Register.fxml"));
        Scene registerScene = new Scene(registerPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(registerScene);
        window.setTitle("TaskMaster Sign-in");
        window.show();
    }

    public void goToMainApp(ActionEvent e) throws IOException {
        Parent mainAppPage = FXMLLoader.load(getClass().getResource("MainApp.fxml"));
        Scene mainAppScene = new Scene(mainAppPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(mainAppScene);
        window.setTitle("TaskMaster");
        window.show();
    }
}

