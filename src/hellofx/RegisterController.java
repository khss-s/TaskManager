package hellofx;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private PasswordField confirmPasswordPasswordField;
    @FXML
    private Label signupMessageLabel;
    
    public void signupButtonOnAction(ActionEvent e) throws IOException {
        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();
        String confirmPassword = confirmPasswordPasswordField.getText();

        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            signupMessageLabel.setText("All fields must be filled in.");
        } else if (username.startsWith(" ") || username.endsWith(" ")) {
            signupMessageLabel.setText("Username cannot contain leading or trailing spaces.");
        } else if (username.length() < 5 || username.length() > 20) {
            signupMessageLabel.setText("Username must be 5-20 characters long.");
        } else if (!username.matches("^[a-zA-Z0-9_ ]+$")) {
            signupMessageLabel.setText("Username can only contain letters, numbers, and underscores.");
        } else if (password.contains(" ")) {
            signupMessageLabel.setText("Password cannot contain spaces.");
        } else if (password.length() < 8) {
            signupMessageLabel.setText("Password must have at least 8 characters.");
        } else if (!password.equals(confirmPassword)) {
            signupMessageLabel.setText("Password does not match.");
        } else if (username.equals(password)) {
            signupMessageLabel.setText("Username and password must not be the same.");
        } else {
            validateSignup(e);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isUsernameTaken(String username) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
    
        try (Connection connectDB = new DBConnection().connectToDB();
             PreparedStatement pstmt = connectDB.prepareStatement(checkQuery)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            signupMessageLabel.setText("An error occurred. Please try again.");
        }
        return false;
    }

    public void validateSignup(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();
        String hashedPassword = hashPassword(password);

        if (hashedPassword == null) {
            signupMessageLabel.setText("An error occurred. Please try again.");
            return;
        }

        if (isUsernameTaken(username)) {
            signupMessageLabel.setText("The username is already in use. Please try another.");
            return;
        }

        String insertQuery = "INSERT INTO users(username, password) VALUES (?, ?)";

        try (Connection connectDB = new DBConnection().connectToDB();
             PreparedStatement pstmt = connectDB.prepareStatement(insertQuery)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try {
                    goToLogin(event);
                } catch (IOException e) {
                    e.printStackTrace();
                    signupMessageLabel.setText("An error occurred. Please try again.");
                }
            } else {
                signupMessageLabel.setText("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            signupMessageLabel.setText("An error occurred. Please try again.");
        }
    }

    public void goToLogin(ActionEvent event) throws IOException {
        Parent loginPage = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene loginScene = new Scene(loginPage);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        window.setScene(loginScene);
        window.show();
    }
}
