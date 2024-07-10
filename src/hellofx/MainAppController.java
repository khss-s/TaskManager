package hellofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {
    @FXML
    private Label usernameLabel;
    @FXML
    private HBox notesContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int userId = LoginState.getUserId();
        String username = fetchUsername(userId);
        usernameLabel.setText(username);
    }

    private String fetchUsername(int userId) {
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.connectToDB();
        String username = "";

        try {
            Statement stmt = connectDB.createStatement();
            ResultSet result = stmt.executeQuery("SELECT username FROM users WHERE user_id = " + userId);

            if (result.next()) {
                username = result.getString("username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return username;
    }

    @FXML
    public void handleCreateButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NoteCreation.fxml"));
            Parent root = loader.load();

            NoteCreationController controller = loader.getController();
            controller.setMainAppController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Create Note");
            Image icon = new Image(Main.class.getResourceAsStream("TaskMaster_icon.jpeg"));
            stage.getIcons().add(icon);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNoteToContainer(String title, String content) {
        VBox noteBox = new VBox();
        noteBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 10;");
        noteBox.setPrefSize(175, 175);

        Label titleLabel = new Label(title);
        TextArea contentArea = new TextArea(content);
        contentArea.setWrapText(true);
        contentArea.setEditable(false);

        ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("edit_icon.png")));
        editIcon.setFitWidth(15); // Adjust these values to fit your icon size
        editIcon.setFitHeight(15); // Adjust these values to fit your icon size

        Button editButton = new Button();
        editButton.setGraphic(editIcon);
        editButton.setPrefSize(10, 10); // Set preferred size for the button
        editButton.setStyle("-fx-background-color: transparent;"); // Transparent background
        editButton.setOnAction(event -> handleEditButtonClick(noteBox, titleLabel, contentArea));

        HBox buttonBox = new HBox();
        buttonBox.getChildren().add(editButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        noteBox.getChildren().addAll(titleLabel, contentArea, buttonBox);
        notesContainer.getChildren().add(noteBox);
    }

    private void handleEditButtonClick(VBox noteBox, Label titleLabel, TextArea contentArea) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NoteEditing.fxml"));
            Parent root = loader.load();

            NoteEditingController controller = loader.getController();
            controller.setMainAppController(this);
            controller.setNoteBox(noteBox, titleLabel, contentArea);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Note");
            Image icon = new Image(Main.class.getResourceAsStream("TaskMaster_icon.jpeg"));
            stage.getIcons().add(icon);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteNoteFromContainer(VBox noteBox) {
        notesContainer.getChildren().remove(noteBox);
    }

    @FXML
    public void handleClearAllButtonAction(ActionEvent event) {
        notesContainer.getChildren().clear();
    }

    @FXML
    public void LogOut(ActionEvent e) throws IOException {
        LoginState.saveLoginState(false);
        Parent loginPage = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene loginScene = new Scene(loginPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.show();
    }
}
