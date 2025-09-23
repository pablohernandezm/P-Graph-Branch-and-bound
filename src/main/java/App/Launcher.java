package App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Launcher class to start the application.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start the application and load the main FXML file.
     *
     * @param stage Stage.
     * @throws IOException If the FXML file is not found.
     */
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("app.fxml")));

        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("PNS - Branch and Bound");
        setWindowIcon(stage);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void setWindowIcon(Stage stage) {
        //6
        stage.getIcons().add(new Image(Objects.requireNonNull(Launcher.class.getResource("icon.png")).toExternalForm()));
    }
}
