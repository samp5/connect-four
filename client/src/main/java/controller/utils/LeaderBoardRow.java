package controller.utils;

import java.time.LocalDateTime;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import network.LeaderBoardData;

public class LeaderBoardRow {
  String senderName;
  int senderID;
  LocalDateTime sendTime;
  String message;

  @FXML
  Text rank;
  @FXML
  Text username;
  @FXML
  Text elo;
  @FXML
  Text games_won;
  @FXML
  Text games_lost;
  @FXML
  Text win_percentage;

  public void build(LeaderBoardData entry) {
    this.build(entry.getRank(), entry.getUsername(), entry.getElo(), entry.getGames_won(), entry.getGames_lost(),
        entry.getWin_percentage());
  }

  public void build(int rank, String username, double elo, int games_won, int games_lost, double win_percentage) {
    this.rank.setText("# " + String.valueOf(rank));
    this.username.setText(username);
    this.elo.setText(String.format("%d", (int) elo));
    this.games_won.setText(String.valueOf(games_won));
    this.games_lost.setText(String.valueOf(games_lost));
    this.win_percentage.setText(String.format("%d", (int) win_percentage) + "%");
  }

  public void initialize() {
  }
}
