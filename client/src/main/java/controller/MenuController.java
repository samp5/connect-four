package controller;

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
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
    menuPane.setBackground(
        new Background(new BackgroundImage(new Image("/assets/load-background.png"),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(1080, 720, false, false, false, false))));
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
  }
}
