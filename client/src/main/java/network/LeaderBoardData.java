package network;

import java.io.Serializable;

public class LeaderBoardData implements Serializable {
  int rank;
  String username;
  double elo;
  int games_won;
  int games_lost;
  double win_percentage;

  public LeaderBoardData() {
  }

  public LeaderBoardData withUserName(String un) {
    this.username = un;
    return this;
  }

  public LeaderBoardData withRank(int rank) {
    this.rank = rank;
    return this;
  }

  public LeaderBoardData withELO(double elo) {
    this.elo = elo;
    return this;
  }

  public LeaderBoardData withGamesWon(int won) {
    this.games_won = won;
    return this;
  }

  public LeaderBoardData withGamesLost(int lost) {
    this.games_lost = lost;
    return this;
  }

  public LeaderBoardData withWinPercent(double percent) {
    this.win_percentage = percent;
    return this;
  }

  public int getRank() {
    return rank;
  }

  public String getUsername() {
    return username;
  }

  public double getElo() {
    return elo;
  }

  public int getGames_won() {
    return games_won;
  }

  public int getGames_lost() {
    return games_lost;
  }

  public double getWin_percentage() {
    return win_percentage;
  }
}
