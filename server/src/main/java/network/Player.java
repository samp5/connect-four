package network;

import java.io.Serializable;

public class Player implements Serializable {
  public enum PlayerRole {
    PlayerOne, PlayerTwo, None;

    public PlayerRole other() {
      switch (this) {
        case None:
          return None;
        case PlayerOne:
          return PlayerRole.PlayerTwo;
        case PlayerTwo:
          return PlayerRole.PlayerOne;
        default:
          return None;
      }
    }
  }

  public Player(String username, Long id) {
    this.username = username;
    this.id = id;
  }

  // this players username and id
  String username;
  Long id;

  // whether this player is local to the current game
  boolean local;
  PlayerRole playerInGame;

  public PlayerRole getRole() {
    return playerInGame;
  }

  public String getUsername() {
    return username;
  }

  public Long getID() {
    return id;
  }

  public void setRole(PlayerRole role) {
    playerInGame = role;
  }
}
