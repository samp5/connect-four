package utils;

import java.io.IOException;

import controller.Controller;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.SettingsController;
import network.NetworkClient;

public class SceneManager {
  private static Stage stage;
  private static Controller ctl;
  private static SceneSelections currentScene = SceneSelections.MAIN_MENU;

  public static enum SceneSelections {
    GAME,
    LOCAL_MULTIPLAYER,
    MAIN_MENU,
    CONNECTIONS_MENU,
    LOADING,
    SERVER_MENU;

    public String toFileName() {
      switch (this) {
        case CONNECTIONS_MENU:
          return "connections.fxml";
        case GAME:
          return "main.fxml";
        case LOCAL_MULTIPLAYER:
          return "local_multiplayer.fxml";
        case LOADING:
          return "loading.fxml";
        case MAIN_MENU:
          return "menu.fxml";
        case SERVER_MENU:
          return "server_menu.fxml";
        default:
          return "menu.fxml";
      }
    }

  }

  public static void initialize(Stage primaryStage) {
    stage = primaryStage;
  }

  public static void showScene(SceneSelections selection) {
    try {
      FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + selection.toFileName()));
      Scene scene = new Scene(loader.load(), 1080, 720);
      ctl = loader.getController();
      currentScene = selection;
      CursorManager.setPointerCursor(scene);
      scene.getStylesheets().add("/css/chat.css");
      scene.getStylesheets().add("/css/game.css");
      scene.getStylesheets().add("/css/menu.css");

      stage.setScene(scene);
      stage.setResizable(false);

      stage.setOnCloseRequest(r -> {
        performClose();
        r.consume();
      });
      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static SceneSelections getCurrentScene() {
    return currentScene;
  }

  public static Controller getCurrentController() {
    return ctl;
  }

  // save close function
  public static void performClose() {
    try {
      SettingsController.save();
      NetworkClient.disconnect();
      Platform.exit();
    } catch (Exception e) {}
    System.exit(0);
  }
}
