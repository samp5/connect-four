package registry;

import java.io.Serializable;
import java.util.ArrayList;

import network.Player;

class RegistryPlayer implements Serializable {
  Long id;
  String username, password;
  PlayerStats stats;
  ArrayList<Player> friends;

  public RegistryPlayer(String username, String password, Long id) {
    this.username = username;
    this.password = password;
    this.id = id;
    this.stats = new PlayerStats();
  }

  public Player getClientPlayer() {
    return new Player(username, id);
  }

  public class PlayerStats implements Serializable {
    int gamesWon;
    int gamesPlayed;
    int globalRank;
  }

  public void completeGame(boolean win) {
    stats.gamesPlayed += 1;
    stats.gamesWon += win ? 1 : 0;

    // TODO: update rank
  }

  public void setRank(int rank) {
    stats.globalRank = rank;
  }

  public int getRank() {
    return stats.globalRank;
  }

  public Long getID() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
