package registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import network.Player;

public class PlayerRegistry {
  private static final HashMap<String, Player> registredPlayers = new HashMap<>();
  // private static final HashMap<String, Player> activePlayers = new HashMap<>();

  public static PlayerRegistrationInfo getRegisteredPlayer(String username, String password) {
    // // check if the player is already active
    // if (activePlayers.keySet().contains(username)) {
    //   return new PlayerRegistrationInfo(false, "Player already logged in.");
    // }

    // check if the player is currently registered
    if (registredPlayers.keySet().contains(username)) {
      Player p = registredPlayers.get(username);
      if (p.getPassword() == password) {
        return new PlayerRegistrationInfo(true, p);
      }
      return new PlayerRegistrationInfo(false, "Username and Password do not match.");
    }

    // register the player
    Player newReg = new Player(username, password);
    registredPlayers.put(username, newReg);
    return new PlayerRegistrationInfo(true, newReg);
  }

  public static class PlayerRegistrationInfo {
    private boolean success;
    private String reason;
    private Player player;

    private PlayerRegistrationInfo(boolean success, Player player) {
      this.success = success;
      this.player = player;
    }
    private PlayerRegistrationInfo(boolean success, String reason) {
      this.success = success;
      this.reason = reason;
    }

  	public boolean isSuccess() {
  		return success;
  	}

  	public String getReason() {
  		return reason;
  	}

    public Player getPlayer() {
      return player;
    }
  }
}
