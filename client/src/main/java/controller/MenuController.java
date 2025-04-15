package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import logic.GameLogic.GameMode;
import utils.SceneManager;
import utils.CursorManager;

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
    settingsButton.setOnAction(e -> {
      try {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/settings.fxml"));
        HBox settings = loader.load();
        SettingsController settingsCTL = loader.getController();
        settingsCTL.attach(menuPane, settings);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    });
  }
}
