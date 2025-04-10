package controller;

import java.util.Timer;
import java.util.TimerTask;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class LoadingController {
  @FXML
  Text loadingText;

  Timer timer;
  private int loadingState = 0;

  public void initialize() {
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        loadingText.setText(String.format("Waiting for Players%s", ".".repeat(loadingState)));
        loadingState = (loadingState + 1) % 4;
      }
    }, 1000, 1000);
  }
}
