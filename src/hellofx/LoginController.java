package hellofx;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class LoginController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private Label loginMessageLabel;

    // Maximum number of failed login attempts before lockout and duration of lockout period
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 5;

    public void loginButtonOnAction(ActionEvent e) throws IOException {
        if (usernameTextField.getText().isBlank() || passwordPasswordField.getText().isBlank()) {
            loginMessageLabel.setText("All fields must be filled in.");
        } else {
            validateLogin(e);
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

    public void validateLogin(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();
        String hashedPassword = hashPassword(password);
    
        if (hashedPassword == null) {
            loginMessageLabel.setText("An error occurred. Please try again.");
            return;
        }
    
        String query = "SELECT user_id, failed_attempts, lockout_end FROM users WHERE username = ?";
    
        try (Connection connectDB = new DBConnection().connectToDB();
            PreparedStatement pstmt = connectDB.prepareStatement(query)) {
    
            pstmt.setString(1, username);
    
            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    int failedAttempts = result.getInt("failed_attempts");
                    Timestamp lockoutEnd = result.getTimestamp("lockout_end");

                    // Reset failed attempts and lockout_end if lockout period has expired
                    if (lockoutEnd != null && Instant.now().isAfter(lockoutEnd.toInstant())) {  
                        failedAttempts = 0;
                        lockoutEnd = null;
                    }
    
                    if (lockoutEnd != null && Instant.now().isBefore(lockoutEnd.toInstant())) {
                        loginMessageLabel.setText("Too many failed login attempts. Please try again later.");
                        return;
                    }

                    if (authenticateUser(connectDB, username, hashedPassword)) {
                        try {
                            resetFailedAttempts(connectDB, username); // Reset the count and lockout end on successful login
                            int userId = result.getInt("user_id");
                            LoginState.saveLoginState(true);
                            LoginState.saveUserId(userId);
                            goToMainApp(event);
                        } catch (IOException e) {
                            e.printStackTrace();
                            loginMessageLabel.setText("An error occurred. Please try again.");
                        }
                    } else {
                        failedAttempts++;
                        Timestamp newLockoutEnd = failedAttempts >= MAX_ATTEMPTS ? 
                            Timestamp.from(Instant.now().plus(LOCK_TIME_MINUTES, ChronoUnit.MINUTES)) : null;
                        
                        updateFailedAttempts(connectDB, username, failedAttempts, newLockoutEnd);
                        
                        loginMessageLabel.setText(newLockoutEnd != null ? 
                            "Too many failed login attempts. Please try again later." : 
                            "Login failed. Please try again.");
                    }
                } else {
                    loginMessageLabel.setText("Login failed. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loginMessageLabel.setText("An error occurred. Please try again.");
        }
    }

    private boolean authenticateUser(Connection connectDB, String username, String hashedPassword) throws SQLException {
        String passwordQuery = "SELECT user_id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement passwordStmt = connectDB.prepareStatement(passwordQuery)) {
            passwordStmt.setString(1, username);
            passwordStmt.setString(2, hashedPassword);
            try (ResultSet passwordResult = passwordStmt.executeQuery()) {
                return passwordResult.next();
            }
        }
    }
    
    private void updateFailedAttempts(Connection connectDB, String username, int failedAttempts, Timestamp lockoutEnd) throws SQLException {
        String updateQuery = "UPDATE users SET failed_attempts = ?, lockout_end = ? WHERE username = ?";
        try (PreparedStatement updateStmt = connectDB.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, failedAttempts);
            updateStmt.setTimestamp(2, lockoutEnd);
            updateStmt.setString(3, username);
            updateStmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(Connection connectDB, String username) throws SQLException {
        String updateQuery = "UPDATE users SET failed_attempts = 0, lockout_end = NULL WHERE username = ?";
        try (PreparedStatement updateStmt = connectDB.prepareStatement(updateQuery)) {
            updateStmt.setString(1, username);
            updateStmt.executeUpdate();
        }
    }    

    public void goToSignup(ActionEvent e) throws IOException {
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

