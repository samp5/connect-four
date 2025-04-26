package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import controller.utils.LeaderBoardRow;
import network.LeaderBoardData;
import network.Message.LeaderBoardView;
import utils.AudioManager;
import utils.CursorManager;
import utils.ToolTipHelper;
import network.NetworkClient;
import network.PlayerData;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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

  @FXML
  BorderPane topTenButton;
  @FXML
  BorderPane aroundPlayerButton;
  @FXML
  BorderPane friendsButton;

  // cached data
  static HashMap<LeaderBoardView, ArrayList<LeaderBoardData>> leaderboardData = new HashMap<>();
  static LeaderBoardView lastView = LeaderBoardView.TOP_TEN;
  static Runnable onRecieve = null;

  private static boolean isAttached = false;
  private Pane parent;

  public void initialize() {
    NetworkClient.bindLeaderBoardController(this);
    setHandlers();
    CursorManager.setHandCursor(backButton, topTenButton, aroundPlayerButton, friendsButton);
    AudioManager.setAudioButton(backButton, topTenButton, aroundPlayerButton, friendsButton);

    setProfilePicture();
    setView(lastView);
  }

  private void setProfilePicture() {
    PlayerData.getProfile(() -> setProfilePicture()).ifPresent(up -> {
      ((ImageView) aroundPlayerButton.getCenter()).setImage(new Image(up.getProfilePicture().getAssetFileName()));
    });
  }

  public static void reset() {
    lastView = LeaderBoardView.TOP_TEN;
    leaderboardData.clear();
  }

  private void setHandlers() {
    backButton.setOnAction(e -> {
      detach();
    });
    topTenButton.setOnMouseClicked(e -> {
      setView(LeaderBoardView.TOP_TEN);
    });
    aroundPlayerButton.setOnMouseClicked(e -> {
      setView(LeaderBoardView.TEN_AROUND_PLAYER);
    });
    friendsButton.setOnMouseClicked(e -> {
      setView(LeaderBoardView.FRIENDS);
    });
    Tooltip.install(friendsButton, ToolTipHelper.make("Compare with Friends"));
    Tooltip.install(topTenButton, ToolTipHelper.make("Global Top 10"));
    Tooltip.install(aroundPlayerButton, ToolTipHelper.make("Your local rank"));
  }

  public static void recieveData(ArrayList<LeaderBoardData> data, LeaderBoardView type) {
    leaderboardData.put(type, data);
    if (onRecieve != null) {
      onRecieve.run();
      onRecieve = null;
    }
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
    lastView = view;

    Collection<LeaderBoardData> d = leaderboardData.get(view);

    if (d == null) {
      NetworkClient.fetchLeaderBoard(view);
      onRecieve = (() -> fill(leaderboardData.get(view)));
    } else {
      fill(d);
    }
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
      } else if (curHandler != null)
        curHandler.handle(e);
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
