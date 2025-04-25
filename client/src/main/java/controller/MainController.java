package controller;

import java.util.Timer;
import java.util.TimerTask;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class MainController extends Controller {
  private static GameController gameCTL;
  private static ChatController chatCTL;
  private static HBox thisGame;

  @FXML
  HBox game;

  public void initialize() {
    // there will only ever be one of these active at once,
    // so this is more or less acceptable
    thisGame = game;
  }

  static void attachGame(GameController ctl) {
    // ensure thisGame is initialized first
    if (thisGame == null) {
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          attachGame(ctl);
        }
      }, 100);
      return;
    }

    gameCTL = ctl;

    EventHandler<? super KeyEvent> curEvent = thisGame.getOnKeyReleased();
    thisGame.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE && !chatCTL.oppProfilePane.isVisible())
        gameCTL.settingsButton.getOnMouseClicked().handle(null);
      else if (curEvent != null)
        curEvent.handle(e);
    });
  }

  static void attachChat(ChatController ctl) {
    // ensure thisGame is initialized first
    if (thisGame == null) {
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          attachChat(ctl);
        }
      }, 100);
      return;
    }

    chatCTL = ctl;

    EventHandler<? super KeyEvent> curEvent = thisGame.getOnKeyReleased();
    thisGame.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE && chatCTL.oppProfilePane.isVisible())
        chatCTL.oppProfileBackButton.getOnAction().handle(null);
      else if (curEvent != null)
        curEvent.handle(e);
    });
  }
}
