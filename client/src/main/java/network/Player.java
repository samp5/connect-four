package network;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {

  public enum PlayerRole {
    PlayerOne, PlayerTwo, None
  }

  public Player(String usrName, int id) {
    username = usrName;
    ID = id;
  }

  // a global unique id for all registered players with the server
  int ID;

  // this players username
  String username;

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

  public int getID() {
    return ID;
  }

  public String getUsername() {
    return username;
  }

  public void setRole(PlayerRole role) {
    playerInGame = role;
  }
}
