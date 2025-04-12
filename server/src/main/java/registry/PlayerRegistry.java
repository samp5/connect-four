package registry;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import game.GameManager;
import network.Player;

/**
 * Tracks all players: logged in, playing or not.
 */
public class PlayerRegistry {
  private static long nextid = 0;
  private static HashMap<String, Player> registredPlayers = new HashMap<>();
  private static HashMap<String, Player> activePlayers = new HashMap<>();

  /**
   * Get the player registered under this username and password.
   * If the username doesn't exist, registers automatically.
   * If the username does exist, and the password matches, returns the user.
   * If the username exists but the password doesn't match, returns an error to 
   *    handle in the client.
   * If the username exists, but is logged in already, returns an error to
   *    handle in the client.
   */
  public static PlayerRegistrationInfo getRegisteredPlayer(String username, String password) {
    // check if the player is already active
    if (activePlayers.keySet().contains(username)) {
      return new PlayerRegistrationInfo(false, "Player already logged in.");
    }

    // check if the player is currently registered
    if (registredPlayers.keySet().contains(username)) {
      Player p = registredPlayers.get(username);
      if (p.getPassword().equals(password)) {
        activePlayers.put(username, p);
        return new PlayerRegistrationInfo(true, p);
      }
      return new PlayerRegistrationInfo(false, "Username and Password do not match.");
    }

    // register the player
    Player newReg = new Player(username, password, getNextID());
    registredPlayers.put(username, newReg);
    activePlayers.put(username, newReg);
    return new PlayerRegistrationInfo(true, newReg);
  }

  /**
   * Remove a player from the active player list, and the queue if they are in
   * queue.
   */
  public static void logoutPlayer(Player player) {
    if (player == null) return;

    String name = player.getUsername();
    activePlayers.remove(name);
    GameManager.removeFromQueue(player);
  }

  /**
   * ID's must be unique, so returns an id, while incrementing.
   */
  public static long getNextID() {
    return nextid++;
  }

  /**
   * Info about the player registration status.
   */
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

  public static int loggedInCount() {
    return activePlayers.size();
  }

  public static int registeredCount() {
    return registredPlayers.size();
  }

  /**
   * Save the registry to a file
   */
  public static void save() {
    try {
    FileOutputStream fileout = new FileOutputStream("player.registry");
    ObjectOutputStream objectout = new ObjectOutputStream(fileout);
    objectout.writeObject(registredPlayers);
    objectout.close();
    } catch (IOException e) {}
  }

  /**
   * Load the registry from a file
   */
  public static void load() {
    try {
      FileInputStream filein = new FileInputStream("player.registry");
      ObjectInputStream objectin = new ObjectInputStream(filein);
      Object obj = objectin.readObject();
      if (obj instanceof HashMap) {
        registredPlayers = (HashMap<String, Player>) obj;
      }
      objectin.close();
    } catch (IOException e) {
    } catch (ClassNotFoundException e) {
      System.out.println("Error loading player registry. class not found:");
      e.printStackTrace();
    }
  }
}
