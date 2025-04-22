package controller;

import controller.utils.GameSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import logic.GameLogic.GameMode;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;

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
  @FXML
  Button quitButton;

  public void initialize() {
    playOnlineButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.Multiplayer);
      SceneManager.showScene(SceneSelections.CONNECTIONS_MENU);
    });
    playLocalButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.LocalMultiplayer);
      SceneManager.showScene(SceneSelections.LOCAL_MULTIPLAYER);
    });
    playAIButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.LocalAI);
      SceneManager.showScene(SceneSelections.GAME);
    });
    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
    quitButton.setOnAction(e -> {
      SceneManager.performClose();
    });
  }
}
