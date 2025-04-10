package network;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {

  public enum PlayerRole {
    PlayerOne, PlayerTwo, None
  }

  public Player(String username, String password, long id) {
    this.username = username;
    this.password = password;
    this.ID = id;
  }
  public Player(String userName, long id) {
    this.username = userName;
    this.ID = id;
  }

  // a global unique id for all registered players with the server
  long ID;

  // this players username and password
  String username, password;

  // whether this player is local to the current game
  boolean local;
  PlayerRole playerInGame;

  // stats about this player
  PlayerInfo stats;

  public class PlayerInfo {
    int gamesWon;
    int gamesPlayed;
    int globalRank;
    ArrayList<Player> friends;
  }

  public PlayerRole getRole() {
    return playerInGame;
  }

  public long getID() {
    return ID;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setRole(PlayerRole role) {
    playerInGame = role;
  }
}
