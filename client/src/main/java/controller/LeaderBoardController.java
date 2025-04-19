package controller;

import java.io.IOException;
import java.util.Collection;
import controller.utils.LeaderBoardRow;
import network.LeaderBoardData;
import network.Message.LeaderBoardView;
import network.NetworkClient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class LeaderBoardController {
  @FXML
  VBox leaderBoardList;

  @FXML
  Button backButton;
  @FXML
  Pane root;

  public void initialize() {
    NetworkClient.bindLeaderBoardController(this);
    NetworkClient.fetchLeaderBoard(LeaderBoardView.TOP_TEN);
  }

  public void fill(Collection<LeaderBoardData> c) {
    this.leaderBoardList.getChildren().setAll();
    for (LeaderBoardData entry : c) {
      System.out.println("got entry for user:" + entry.getUsername());
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
}
