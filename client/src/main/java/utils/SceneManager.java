package utils;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
  private static Stage stage;

  public static void initialize(Stage primaryStage) {
    stage = primaryStage;
  }

  /**
   * @param fxmlFile A valid filename from resouces/fxml/
   */
  public static void showScene(String fxmlFile) {
    try {
      FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + fxmlFile));
      Scene scene = new Scene(loader.load(), 1080, 720);
      CursorManager.setPointerCursor(scene);
      scene.getStylesheets().add("/css/chat.css");
      scene.getStylesheets().add("/css/game.css");
      scene.getStylesheets().add("/css/menu.css");

      stage.setScene(scene);
      stage.setResizable(false);
      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
