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
  private static Long nextid = 0l;
  private static HashMap<String, Long> usernameLookup = new HashMap<>();
  private static HashMap<Long, RegistryPlayer> registeredPlayers = new HashMap<>();
  private static HashMap<Long, RegistryPlayer> activePlayers = new HashMap<>();

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
    Long id = usernameLookup.get(username);

    // if the username doesn't match with an ID, register the new player
    if (id == null) {
      // register the player
      Long newId = getNextID();
      RegistryPlayer newReg = new RegistryPlayer(username, password, newId);
      registeredPlayers.put(newId, newReg);
      activePlayers.put(newId, newReg);
      usernameLookup.put(username, newId);
      return new PlayerRegistrationInfo(true, newReg.getClientPlayer());
    }

    // check if the player is already active
    if (activePlayers.keySet().contains(id)) {
      return new PlayerRegistrationInfo(false, "Player already logged in.");
    }

    // check if the player is currently registered
    RegistryPlayer p = registeredPlayers.get(id);
    if (p.getPassword().equals(password)) {
      activePlayers.put(id, p);
      return new PlayerRegistrationInfo(true, p.getClientPlayer());
    }
    return new PlayerRegistrationInfo(false, "Username and Password do not match.");
  }

  /**
   * Remove a player from the active player list, and the queue if they are in
   * queue.
   */
  public static void logoutPlayer(Player player) {
    if (player == null) return;

    activePlayers.remove(player.getID());
    GameManager.removeFromQueue(player);
  }

  /**
   * ID's must be unique, so returns an id, while incrementing.
   */
  public static Long getNextID() {
    Long ret = nextid;
    nextid += 1;
    return ret;
  }

  /**
   * Updates a player's stats
   */
  public static void updatePlayerStats(Player p, boolean win) {
    registeredPlayers.get(p.getID()).completeGame(win);

    // TODO: update rank
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
    return registeredPlayers.size();
  }

  /**
   * Save the registry to a file
   */
  public static void save() {
    try {
    FileOutputStream fileout = new FileOutputStream("player.registry");
    ObjectOutputStream objectout = new ObjectOutputStream(fileout);
    objectout.writeObject(registeredPlayers);
    objectout.writeObject(usernameLookup);
    objectout.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Player Registry had an exception on save. Registry not saved.");
    }
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
        registeredPlayers = (HashMap<Long, RegistryPlayer>) obj;
      }
      obj = objectin.readObject();
      if (obj instanceof HashMap) {
        usernameLookup = (HashMap<String, Long>) obj;
      }

      nextid = (long) registeredPlayers.size();

      objectin.close();
    } catch (ClassNotFoundException e) {
      System.out.println("Error loading player registry. class not found:");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Player Registry had an exception on Load. Registry not loaded.");
    }
  }
}
