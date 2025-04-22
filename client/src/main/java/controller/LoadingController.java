package controller;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import network.NetworkClient;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;

public class LoadingController extends Controller {
  @FXML
  ImageView loading;

  @FXML
  ImageView background;
  int grassState = 0;

  @FXML
  Button backButton;

  Timer timer;
  private static final int WIDTH = 660;
  private static final int HEIGHT = 180;
  private int loadState = 0;

  public void initialize() {
    setHandlers();
    animateLoading();
    animateGrass();
  }

  public void setHandlers() {
    backButton.setOnAction(e -> {
      NetworkClient.cancelJoinGame();
      SceneManager.showScene(SceneSelections.SERVER_MENU);
    });
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

  public void animateLoading() {
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        loadState = (loadState + 1) % 5;
        Platform.runLater(() -> {
          loading.setViewport(new Rectangle2D(0, (HEIGHT * loadState), WIDTH, HEIGHT));
        });
      }
    }, 1000, 1000);
  }
}
