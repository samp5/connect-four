package controller;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import network.NetworkClient;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;

public class LoadingController extends Controller {
  @FXML
  Text loadingText;

  @FXML
  Pane loadingPane;

  @FXML
  Pane backgroundPane;

  @FXML
  Button backButton;

  Timer timer;
  private int dotState = 0;
  private int loadState = 0;

  public void initialize() {
    setHandlers();
    animateLoading();
  }

  public void setHandlers() {
    backButton.setOnAction(e -> {
      NetworkClient.cancelJoinGame();
      SceneManager.showScene(SceneSelections.SERVER_MENU);
    });
  }

  public void animateLoading() {
    loadingPane.setBackground(
        new Background(new BackgroundImage(new Image("/assets/load-0.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(240, 60, false, false, false, false))));

    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        loadingPane.setBackground(
            new Background(new BackgroundImage(new Image("/assets/load-" + String.valueOf(loadState) + ".png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(240, 60, false, false, false, false))));
        loadingText.setText(String.format("Waiting for Players%s", ".".repeat(dotState)));
        dotState = (dotState + 1) % 4;
        loadState = (loadState + 1) % 5;
      }
    }, 1000, 1000);
  }
}
