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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private Label usernameLabel;
    @FXML
    private HBox notesContainer;

    private List<VBox> allNotes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int userId = LoginState.getUserId();
        String username = fetchUsername(userId);
        usernameLabel.setText(username);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterNotes(newValue));
    }

    private void filterNotes(String keyword) {
        notesContainer.getChildren().clear();
        if (keyword.isEmpty()) {
            notesContainer.getChildren().addAll(allNotes);
        } else {
            for (VBox noteBox : allNotes) {
                HBox titleBox = (HBox) noteBox.getChildren().get(0);
                Label titleLabel = (Label) titleBox.getChildren().get(0);
                if (titleLabel.getText().toLowerCase().contains(keyword.toLowerCase())) {
                    notesContainer.getChildren().add(noteBox);
                }
            }
        }
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
        contentArea.setPrefHeight(100);
    
        CheckBox doneCheckBox = new CheckBox("Done");
        doneCheckBox.setMinWidth(51);
        doneCheckBox.setPrefWidth(51);
        doneCheckBox.setStyle("-fx-background-color: transparent;");
        doneCheckBox.setOnAction(event -> {
            // Handle what happens when the checkbox is clicked (e.g., update database or UI)
            if (doneCheckBox.isSelected()) {
                // Mark the note as done
                // You can add logic here to update your database or do any other action
            } else {
                // Mark the note as not done
                // You can add logic here to update your database or do any other action
            }
        });
    
        // HBox for title and checkbox
        HBox titleBox = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBox.getChildren().addAll(titleLabel, spacer, doneCheckBox);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("edit_icon.png")));
        editIcon.setFitWidth(15);
        editIcon.setFitHeight(15);

        Button editButton = new Button();
        editButton.setGraphic(editIcon);
        editButton.setPrefSize(10, 10);
        editButton.setStyle("-fx-background-color: transparent;");
        editButton.setOnAction(event -> handleEditButtonClick(noteBox, titleLabel, contentArea));
    
        HBox buttonBox = new HBox();
        buttonBox.getChildren().add(editButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
    
        noteBox.getChildren().addAll(titleBox, contentArea, buttonBox);
        noteBox.setSpacing(10);
        notesContainer.getChildren().add(noteBox);
        allNotes.add(noteBox);
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
        allNotes.remove(noteBox);
    }

    @FXML
    public void handleClearAllButtonAction(ActionEvent event) {
        notesContainer.getChildren().clear();
        allNotes.clear();
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