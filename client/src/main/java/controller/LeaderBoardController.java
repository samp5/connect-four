package controller;

import java.io.IOException;
import java.util.Collection;
import controller.utils.LeaderBoardRow;
import network.LeaderBoardData;
import network.Message.LeaderBoardView;
import network.NetworkClient;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class LeaderBoardController extends Controller {
  @FXML
  VBox leaderBoardList;

  @FXML
  Button backButton;
  @FXML
  Pane root;

  private static boolean isAttached = false;
  private Pane parent;

  public void initialize() {
    NetworkClient.bindLeaderBoardController(this);
    NetworkClient.fetchLeaderBoard(LeaderBoardView.TOP_TEN);
    setHandlers();
  }

  private void setHandlers() {
    backButton.setOnAction(e -> {
      detach();
    });
  }

  public void fill(Collection<LeaderBoardData> c) {
    this.leaderBoardList.getChildren().setAll();
    for (LeaderBoardData entry : c) {
      try {
        FXMLLoader loader = new FXMLLoader(ChatController.class.getResource("/fxml/leaderboard_entry.fxml"));
        HBox row = loader.load();
        LeaderBoardRow rowCTL = loader.getController();
        rowCTL.build(entry);
        this.leaderBoardList.getChildren().add(row);
      } catch (IOException ioE) {
        ioE.printStackTrace();
      }
    }
  }

  public void setView(LeaderBoardView view) {
  }

  public static void loadOnto(Pane onto) {
    if (SettingsController.isAttached()) {
      return;
    }

    try {
      FXMLLoader loader = new FXMLLoader(LeaderBoardController.class.getResource("/fxml/leaderboard.fxml"));
      Pane leaderboard = loader.load();
      LeaderBoardController ldboardCTL = loader.getController();
      ldboardCTL.attach(onto, leaderboard);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void attach(Pane parent, Pane root) {
    if (isAttached) {
      return;
    }
    parent.getChildren().add(root);
    root.relocate((parent.getWidth() - 800) / 2,
        (parent.getHeight() - 600) / 2);
    root.toFront();
    this.parent = parent;
    isAttached = true;

    EventHandler<? super KeyEvent> curHandler = parent.getOnKeyReleased();
    root.requestFocus();
    parent.setOnKeyReleased((e) -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        parent.setOnKeyReleased(curHandler);
        detach();
        e.consume();
      } else {
        curHandler.handle(e);
      }
    });
  }

  private void detach() {
    if (!isAttached) {
      return;
    }

    isAttached = false;
    parent.getChildren().remove(root);
  }
}
