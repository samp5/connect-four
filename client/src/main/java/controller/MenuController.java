package controller;

import java.util.Timer;
import java.util.TimerTask;

import controller.utils.GameSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import logic.GameLogic.GameMode;
import utils.AudioManager;
import utils.CursorManager;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;

/**
 * Bind to Menu.fxml
 *
 */
public class MenuController extends Controller {
  @FXML
  ImageView background;
  int grassState = 0;

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
    playOnlineButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        playOnlineButton.getOnAction().handle(null);
    });

    playLocalButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.LocalMultiplayer);
      SceneManager.showScene(SceneSelections.LOCAL_MULTIPLAYER);
    });
    playLocalButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        playLocalButton.getOnAction().handle(null);
    });

    playAIButton.setOnAction(e -> {
      GameController.setGameMode(GameMode.LocalAI);
      SceneManager.showScene(SceneSelections.GAME);
    });
    playAIButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        playAIButton.getOnAction().handle(null);
    });

    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
    settingsButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        settingsButton.getOnAction().handle(null);
    });

    quitButton.setOnAction(e -> {
      SceneManager.performClose();
    });
    quitButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        quitButton.getOnAction().handle(null);
    });

    // set cursors and sfx
    CursorManager.setHandCursor(playOnlineButton, playLocalButton, playAIButton,
        settingsButton, quitButton);
    AudioManager.setAudioButton(playOnlineButton, playLocalButton, playAIButton,
        settingsButton, quitButton);

    animateGrass();
  }

  private void animateGrass() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        grassState = (grassState + 1) % 2;
        try {
          Platform.runLater(() -> {
            background.setViewport(new Rectangle2D((3840 * grassState), 0, 3840, 2560));
          });
        } catch (Exception e) {}
      }
    }, 0, 1000);
  }
}
