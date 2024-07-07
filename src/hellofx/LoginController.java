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
            ValidateLogin();
        } else {
            loginMessageLabel.setText("Both fields must be filled in.");
        }
    }

    public void ValidateLogin() {
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.connectToDB();

        try {
            Statement stmt = connectDB.createStatement();
            ResultSet result = stmt.executeQuery("SELECT count(1) FROM users WHERE username = '" + usernameTextField.getText() + "' AND password = '" + passwordPasswordField.getText() + "'");
    
            while(result.next()) {
                if (result.getInt(1) == 1) {
                    loginMessageLabel.setText("Logged in. Welcome.");
                } else {
                    loginMessageLabel.setText("Invalid login. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSignInClick(ActionEvent e) throws IOException {
        Parent registerPage = FXMLLoader.load(getClass().getResource("Register.fxml"));
        Scene registerScene = new Scene(registerPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(registerScene);
        window.show();
    }
}

