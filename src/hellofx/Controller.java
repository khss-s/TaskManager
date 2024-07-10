package hellofx;

import javax.swing.Action;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class Controller {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private Label loginMessageLabel;    

    public void loginButtonOnAction(ActionEvent e) {
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
}

