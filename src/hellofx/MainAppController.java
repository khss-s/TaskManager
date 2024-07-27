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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private Label usernameLabel;
    @FXML
    private FlowPane tasksContainer;

    private List<VBox> allTasks = new ArrayList<>();

    private List<Stage> openWindows = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int userId = LoginState.getUserId();
        String username = fetchUsername(userId);
        usernameLabel.setText(username);

        loadTasksFromDatabase(userId);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTasks(newValue));

        tasksContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Stage primaryStage = (Stage) newScene.getWindow();
                primaryStage.setOnCloseRequest(event -> closeAllOpenWindows());
                bindTasksContainerWidth();
            }
        });

        // Periodically update task box styles
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> updateAllTaskBoxStyles()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateAllTaskBoxStyles() {
        Platform.runLater(() -> {
            for (VBox taskBox : allTasks) {
                // Extract the due date label and parse the due date
                Label dueDateLabel = (Label) ((HBox) taskBox.getChildren().get(2)).getChildren().get(0);
                String dueDateText = dueDateLabel.getText().replace("Due date: ", "").trim();
                LocalDate dueDate = dueDateText.equals("No due date") ? null : LocalDate.parse(dueDateText);
                
                // Extract the isDone status
                HBox titleBox = (HBox) taskBox.getChildren().get(0);
                CheckBox doneCheckBox = findCheckBox(titleBox);
                boolean isDone = doneCheckBox.isSelected();
                
                // Update the task box style based on isDone and due date
                updateTaskBoxStyle(taskBox, isDone, dueDate);
            }
        });
    }
    

    private void bindTasksContainerWidth() {
        // Traverse the parent hierarchy until we find a ScrollPane
        Parent parent = tasksContainer.getParent();
        while (parent != null && !(parent instanceof ScrollPane)) {
            parent = parent.getParent();
        }
    
        if (parent instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) parent;
            tasksContainer.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
        } else {
            System.err.println("ScrollPane not found in hierarchy!");
        }
    }

    public void addOpenWindow(Stage stage) {
        openWindows.add(stage);
    }
    
    private void closeAllOpenWindows() {
        for (Stage stage : openWindows) {
            if (stage.isShowing()) {
                stage.close();
            }
        }
        openWindows.clear();
    }

    private void loadTasksFromDatabase(int userId) {
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.connectToDB();

        try {
            Statement stmt = connectDB.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM tasks WHERE user_id = " + userId + " AND is_deleted = FALSE");

            while (result.next()) {
                int taskId = result.getInt("task_id");
                String title = result.getString("title");
                String content = result.getString("content");
                LocalDate dueDate = result.getObject("due_date", LocalDate.class);
                boolean isDone = result.getBoolean("is_done");

                addTaskToContainer(taskId, title, content, dueDate, isDone);
            }

            // Reorder tasks to move checked tasks to the end
            reorderTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterTasks(String keyword) {
        tasksContainer.getChildren().clear();
        if (keyword.isEmpty()) {
            tasksContainer.getChildren().addAll(allTasks);
        } else {
            for (VBox taskBox : allTasks) {
                HBox titleBox = (HBox) taskBox.getChildren().get(0);
                Label titleLabel = (Label) titleBox.getChildren().get(0);
                if (titleLabel.getText().toLowerCase().contains(keyword.toLowerCase())) {
                    tasksContainer.getChildren().add(taskBox);
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

    // Reorders tasks to move checked tasks to the end
    private void reorderTasks() {
        List<Node> tasks = new ArrayList<>(tasksContainer.getChildren());
        tasks.sort((node1, node2) -> {
            VBox taskBox1 = (VBox) node1;
            VBox taskBox2 = (VBox) node2;
    
            HBox titleBox1 = (HBox) taskBox1.getChildren().get(0);
            CheckBox checkBox1 = findCheckBox(titleBox1);
    
            HBox titleBox2 = (HBox) taskBox2.getChildren().get(0);
            CheckBox checkBox2 = findCheckBox(titleBox2);
    
            return Boolean.compare(checkBox1.isSelected(), checkBox2.isSelected());
        });
    
        tasksContainer.getChildren().setAll(tasks);
    }
    
    private CheckBox findCheckBox(HBox titleBox) {
        for (Node node : titleBox.getChildren()) {
            if (node instanceof CheckBox) {
                return (CheckBox) node;
            }
        }
        return null; // or handle appropriately if no CheckBox is found
    }

    public void updateTaskBoxStyle(VBox taskBox, boolean isDone, LocalDate dueDate) {
        if (isDone == true) {
            // Apply light green glow effect to done tasks
            DropShadow greenGlow = new DropShadow();
            greenGlow.setColor(Color.GREENYELLOW);
            greenGlow.setWidth(2);
            greenGlow.setHeight(2);
            greenGlow.setRadius(2);
            taskBox.setEffect(greenGlow);
            taskBox.setStyle("-fx-background-color: white; -fx-border-color: greenyellow; -fx-padding: 10;");
        } else {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setWidth(2);
            dropShadow.setHeight(2);
            dropShadow.setRadius(2);
    
            LocalDate today = LocalDate.now();
            
            if (dueDate == null) {
                // Default style if no due date
                taskBox.setEffect(null);
                taskBox.setStyle("-fx-background-color: white; -fx-border-color: #21005D; -fx-padding: 10;");
            } else if (dueDate.isBefore(today)) {
                // Past due date
                dropShadow.setColor(Color.RED);
                taskBox.setEffect(dropShadow);
                taskBox.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-padding: 10;");
            } else if (dueDate.isEqual(today) || dueDate.isBefore(today.plusDays(3))) {
                // Near due date (within 3 days)
                dropShadow.setColor(Color.ORANGE);
                taskBox.setEffect(dropShadow);
                taskBox.setStyle("-fx-background-color: white; -fx-border-color: orange; -fx-padding: 10;");
            } else {
                // Default style
                taskBox.setEffect(null);
                taskBox.setStyle("-fx-background-color: white; -fx-border-color: #21005D; -fx-padding: 10;");
            }
        }
    }    

    @FXML
    public void handleCreateButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskCreation.fxml"));
            Parent root = loader.load();

            TaskCreationController controller = loader.getController();
            controller.setMainAppController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Create Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            Image icon = new Image(Main.class.getResourceAsStream("TaskMaster_icon.jpeg"));
            stage.getIcons().add(icon);
            stage.show();

            addOpenWindow(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTaskToContainer(int taskId, String title, String content, LocalDate dueDate, boolean isDone) {
        VBox taskBox = new VBox();
        taskBox.setStyle("-fx-background-color: white; -fx-border-color: #21005D; -fx-padding: 10;");
        taskBox.setPrefSize(200, 200);

        // Set taskId as a property of taskBox
        taskBox.getProperties().put("taskId", taskId);

        Label titleLabel = new Label(title);
        TextArea contentArea = new TextArea(content);
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefHeight(100);

        CheckBox doneCheckBox = new CheckBox("Done");
        doneCheckBox.setMinWidth(51);
        doneCheckBox.setPrefWidth(51);
        doneCheckBox.setStyle("-fx-background-color: transparent;");
        doneCheckBox.setSelected(isDone);
        doneCheckBox.setOnAction(event -> handleDoneCheckboxAction(event, taskId, doneCheckBox, dueDate));

        // HBox for title and checkbox
        HBox titleBox = new HBox();
        Region titleBoxSpacer = new Region();
        HBox.setHgrow(titleBoxSpacer, Priority.ALWAYS);
        titleBox.getChildren().addAll(titleLabel, titleBoxSpacer, doneCheckBox);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("edit_icon.png")));
        editIcon.setFitWidth(15);
        editIcon.setFitHeight(15);
        Button editButton = new Button();
        editButton.setGraphic(editIcon);
        editButton.setPrefSize(10, 10);
        editButton.setStyle("-fx-background-color: transparent;");

        Label dueDateLabel = new Label("Due date: " + (dueDate != null ? dueDate.toString() : "No due date"));

        editButton.setOnAction(event -> handleEditButtonClick(taskBox, titleLabel, contentArea, dueDateLabel, isDone, taskId));
        
        // HBox for due date label and edit button
        HBox dueDateAndEditBox = new HBox();
        Region dueDateAndEditBoxSpacer = new Region();
        HBox.setHgrow(dueDateAndEditBoxSpacer, Priority.ALWAYS);
        dueDateAndEditBox.getChildren().addAll(dueDateLabel, dueDateAndEditBoxSpacer, editButton);
        dueDateAndEditBox.setAlignment(Pos.CENTER_LEFT);
       
        taskBox.getChildren().addAll(titleBox, contentArea, dueDateAndEditBox);
        taskBox.setSpacing(10);
        tasksContainer.getChildren().add(taskBox);
        allTasks.add(taskBox);

        reorderTasks();

        updateTaskBoxStyle(taskBox, isDone, dueDate);
    }

    private void handleDoneCheckboxAction(ActionEvent event, int taskId, CheckBox checkBox, LocalDate dueDate) {
        boolean isDone = checkBox.isSelected();

        // Update the task's is_done status in the database
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "UPDATE tasks SET is_done = ? WHERE task_id = ?";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setBoolean(1, isDone);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Node taskBox = checkBox.getParent().getParent();
        tasksContainer.getChildren().remove(taskBox);
        allTasks.remove(taskBox);
    
        if (isDone) {
            // Move the task to the end of the container if done
            tasksContainer.getChildren().add(taskBox);
            allTasks.add((VBox) taskBox);
        } else {
            // Move the task to the front of the container if not done
            tasksContainer.getChildren().add(0, taskBox);
            allTasks.add(0, (VBox) taskBox);
        }

        updateTaskBoxStyle((VBox) taskBox, isDone, dueDate);
    }

    @FXML
    public void handleEditButtonClick(VBox taskBox, Label titleLabel, TextArea contentArea, Label dueDateLabel, boolean isDone, int taskId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskEditing.fxml"));
            Parent root = loader.load();

            TaskEditingController controller = loader.getController();
            controller.setMainAppController(this);
            controller.setTaskBox(taskBox, titleLabel, contentArea, dueDateLabel, getTaskDueDate(taskId), isDone, taskId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            Image icon = new Image(Main.class.getResourceAsStream("TaskMaster_icon.jpeg"));
            stage.getIcons().add(icon);
            stage.show();

            addOpenWindow(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LocalDate getTaskDueDate(int taskId) {
        LocalDate dueDate = null;
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.connectToDB();
    
        try {
            String query = "SELECT due_date FROM tasks WHERE task_id = ?";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setInt(1, taskId);
            ResultSet result = pstmt.executeQuery();
    
            if (result.next()) {
                dueDate = result.getObject("due_date", LocalDate.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return dueDate;
    }
    

    public void deleteTaskFromContainer(VBox taskBox) {
        // Remove the task from the UI
        tasksContainer.getChildren().remove(taskBox);
        allTasks.remove(taskBox);

        // Extract taskId from taskBox
        int taskId = (int) taskBox.getProperties().get("taskId");

        // Soft deletes the task in the database
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "UPDATE tasks SET is_deleted = TRUE WHERE task_id = ?";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteAllTasksButtonAction(ActionEvent event) {
        // Delete all tasks from the UI
        tasksContainer.getChildren().clear();
        allTasks.clear();

        // Soft delete all tasks from the database for the logged-in user
        int userId = LoginState.getUserId();
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "UPDATE tasks SET is_deleted = TRUE WHERE user_id = ?";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteAllDoneTasksButtonAction(ActionEvent event) {
        softDeleteAllDoneTasksFromDatabase();
        deleteAllDoneTasksFromUI();
    }

    private void softDeleteAllDoneTasksFromDatabase() {
        // Soft delete all done tasks in the database for the logged-in user
        int userId = LoginState.getUserId();
        try {
            DBConnection connectNow = new DBConnection();
            Connection connectDB = connectNow.connectToDB();
            String query = "UPDATE tasks SET is_deleted = TRUE WHERE user_id = ? AND is_done = true";
            PreparedStatement pstmt = connectDB.prepareStatement(query);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAllDoneTasksFromUI() {
        // Create a list to hold tasks that need to be removed
        List<VBox> tasksToRemove = new ArrayList<>();
        
        // Iterate over all tasks and add the ones that are done to the list
        for (Node node : tasksContainer.getChildren()) {
            VBox taskBox = (VBox) node;
            HBox titleBox = (HBox) taskBox.getChildren().get(0);
            CheckBox doneCheckBox = (CheckBox) titleBox.getChildren().get(2);

            if (doneCheckBox.isSelected()) {
                tasksToRemove.add(taskBox);
            }
        }
        
        // Deletes the tasks from the UI and the allTasks list
        tasksContainer.getChildren().removeAll(tasksToRemove);
        allTasks.removeAll(tasksToRemove);
    }

    @FXML
    public void LogOut(ActionEvent e) throws IOException {
        closeAllOpenWindows();  // Close all open windows

        LoginState.saveLoginState(false);
        Parent loginPage = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene loginScene = new Scene(loginPage);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.show();
    }
}