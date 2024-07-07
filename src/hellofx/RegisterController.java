package hellofx;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    public void handleLogInClick(ActionEvent e) throws IOException {
        Parent registerPage = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene registerScene = new Scene(registerPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(registerScene);
        window.show();
    }
}
