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
public class MenuController {
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
      SceneManager.showScene("connections.fxml");
      GameController.setGameMode(GameMode.Multiplayer);
    });
    playLocalButton.setOnAction(e -> {
      SceneManager.showScene("main.fxml");
      GameController.setGameMode(GameMode.LocalMultiplayer);
    });
    playAIButton.setOnAction(e -> {
      SceneManager.showScene("main.fxml");
      GameController.setGameMode(GameMode.LocalAI);
    });
    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
  }
}
