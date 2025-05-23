import javafx.application.Application;
import javafx.stage.Stage;
import sun.misc.Signal;
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
    // sig-int handler
    Signal.handle(new Signal("INT"), sig -> {
      SceneManager.performClose();
    });

    // attempt to connect to localhost server
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    SceneManager.initialize(primaryStage);
    SceneManager.showScene(SceneSelections.MAIN_MENU);
    AudioManager.playMainTheme();

    // load settings
    SettingsController.load();
  }
}
