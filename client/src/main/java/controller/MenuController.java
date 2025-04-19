package controller;

import controller.utils.GameSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import logic.GameLogic.GameMode;
import utils.SceneManager;

/**
 * Bind to Menu.fxml
 *
 */
public class MenuController extends Controller {
  @FXML
  Pane menuPane;

  @FXML
  Button playOnlineButton;
  @FXML
  Button playLocalButton;
  @FXML
  Button playAIButton;
  @FXML
  Button settingsButton;

  public void initialize() {
    playOnlineButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.Multiplayer);
      SceneManager.showScene("connections.fxml");
    });
    playLocalButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.LocalMultiplayer);
      SceneManager.showScene("main.fxml");
    });
    playAIButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.LocalAI);
      SceneManager.showScene("main.fxml");
    });
    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
  }
}
