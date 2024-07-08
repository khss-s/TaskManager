package hellofx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        boolean loggedIn = LoginState.getLoginState();
        Parent root;
        
        if (loggedIn) {
            root = FXMLLoader.load(getClass().getResource("MainApp.fxml"));
            primaryStage.setTitle("TaskMaster");
        } else {
            root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            primaryStage.setTitle("TaskMaster Log-in");
        }

        primaryStage.setScene(new Scene(root, 600, 400));
        Image icon = new Image(Main.class.getResourceAsStream("TaskMaster_icon.jpeg"));
        primaryStage.getIcons().add(icon);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}