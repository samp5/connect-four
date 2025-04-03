import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main Application Class.
 * likely should not be referenced.
 */
public class JavaFX extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("ConnectFour");
    primaryStage.setScene(new Scene(new VBox()));
    primaryStage.show();
  }
}
