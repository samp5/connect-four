import javafx.application.Application;
import javafx.stage.Stage;
import network.NetworkClient;
import utils.AudioManager;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;
import controller.SettingsController;

/**
 * Main Application Class.
 * likely should not be referenced.
 */
public class JavaFX extends Application {

  public static void main(String[] args) {
    // add shut down disconnect and settings protection
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // this happens often when not doing anything over network
      try {
        SettingsController.save();

        Class.forName("network.NetworkClient");
        NetworkClient.disconnect();
      } catch (ClassNotFoundException e) {
      }
    }));

    // attempt to connect to localhost server
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    SceneManager.initialize(primaryStage);
    SceneManager.showScene(SceneSelections.MAIN_MENU);
    AudioManager.playContinuous("main_theme.mp3");

    // load settings
    SettingsController.load();
  }
}
