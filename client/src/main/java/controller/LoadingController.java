package controller;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import network.NetworkClient;
import utils.AudioManager;
import utils.CursorManager;
import utils.NotificationManager;
import utils.NotificationManager.NotificationType;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;

public class LoadingController extends Controller {
  @FXML
  ImageView loading;

  @FXML
  ImageView background;
  int grassState = 0;

  // NOTIFICATIONS
  @FXML
  Pane notificationPane;
  @FXML
  TextFlow notificationText;
  @FXML
  ImageView notificationIcon;
  NotificationManager notificationManager;

  @FXML
  Button backButton;

  Timer timer;
  private static final int WIDTH = 660;
  private static final int HEIGHT = 180;
  private int loadState = 0;

  public void initialize() {
    NetworkClient.bindLoadingController(this);
    setHandlers();
    animateLoading();
    animateGrass();

    notificationManager = new NotificationManager(notificationPane, notificationText, notificationIcon);

    CursorManager.setHandCursor(backButton);
    AudioManager.setAudioButton(backButton);
  }

  public void setHandlers() {
    backButton.setOnAction(e -> {
      NetworkClient.cancelJoinGame();
      SceneManager.showScene(SceneSelections.SERVER_MENU);
    });

    background.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ESCAPE)
        backButton.getOnAction().handle(null);
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
        } catch (Exception e) {
        }
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

  public void recieveNotification(String msg, NotificationType type) {
    notificationManager.recieve(msg, type);
  }

  public void recievePrompt(String msg, NotificationType type, EventHandler<ActionEvent> onAccept,
      EventHandler<ActionEvent> onDeny) {
    notificationManager.recievePrompt(msg, type, onAccept, onDeny);
  }
}
