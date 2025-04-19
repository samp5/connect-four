package registry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import game.GameManager;
import network.Player;
import network.UserProfile;
import network.UserProfile.ProfilePicture;
import network.Message.WinType;

/**
 * Tracks all players: logged in, playing or not.
 */
public class PlayerRegistry {
  private static Long nextid = 0l;
  private static HashMap<String, Long> usernameLookup = new HashMap<>();
  private static HashMap<Long, RegistryPlayer> registeredPlayers = new HashMap<>();
  private static HashSet<Long> activePlayers = new HashSet<>();

  /**
   * Get the player registered under this username and password.
   * If the username doesn't exist, registers automatically.
   * If the username does exist, and the password matches, returns the user.
   * If the username exists but the password doesn't match, returns an error to
   * handle in the client.
   * If the username exists, but is logged in already, returns an error to
   * handle in the client.
   */
  public static PlayerRegistrationInfo getRegisteredPlayer(String username, String password) {
    synchronized (registeredPlayers) {
      synchronized (activePlayers) {
        Long id = usernameLookup.get(username);

        // if the username doesn't match with an ID, register the new player
        if (id == null) {
          // register the player
          Long newId = getNextID();
          RegistryPlayer newReg = new RegistryPlayer(username, password, newId);
          registeredPlayers.put(newId, newReg);
          activePlayers.add(newId);
          usernameLookup.put(username, newId);
          return new PlayerRegistrationInfo(true, newReg.getClientPlayer());
        }

        // check if the player is already active
        if (activePlayers.contains(id)) {
          return new PlayerRegistrationInfo(false, "Player already logged in.");
        }

        // check if the player is currently registered
        RegistryPlayer p = registeredPlayers.get(id);
        if (p.getPassword().equals(password)) {
          activePlayers.add(id);
          return new PlayerRegistrationInfo(true, p.getClientPlayer());
        }
      }
    }
    return new PlayerRegistrationInfo(false, "Username and Password do not match.");
  }

  public static Optional<RegistryPlayer> getRegistryPlayerByID(Long id) {
    synchronized (registeredPlayers) {
      RegistryPlayer player = registeredPlayers.get(id);
      if (player == null) {
        return Optional.empty();
      } else {
        return Optional.of(player);
      }
    }
  }

  public static Optional<UserProfile> getUserProfileByID(Long id) {
    synchronized (registeredPlayers) {
      RegistryPlayer player = registeredPlayers.get(id);
      if (player == null) {
        return Optional.empty();
      } else {
        return Optional.of(player.asUserProfile());
      }
    }
  }

  public static void updateProfilePicture(Long id, ProfilePicture newPic) {
    synchronized (registeredPlayers) {
      RegistryPlayer player = registeredPlayers.get(id);
      if (player != null) {
        player.profilePicture = newPic;
      } else {
        System.err.println("PlayerRegistry::updateProfilePicture could not find player with id: " + id.toString());
      }
    }
  }

  /**
   * Remove a player from the active player list, and the queue if they are in
   * queue.
   */
  public static void logoutPlayer(Player player) {
    if (player == null)
      return;

    synchronized (activePlayers) {
      activePlayers.remove(player.getID());
    }
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
  public static void updatePlayerStats(Player p, WinType win) {
    synchronized (registeredPlayers) {
      registeredPlayers.get(p.getID()).completeGame(win);
    }
  }

  public synchronized static void addFriends(Long id1, Long id2) {
    synchronized (registeredPlayers) {
      getRegistryPlayerByID(id1).ifPresent(p1 -> {
        getRegistryPlayerByID(id2).ifPresent(p2 -> {
          p1.friends.add(id2);
          p2.friends.add(id1);
        });
      });
    }

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
      System.err.println("Player Registry had an exception on save. Registry not saved.");
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
      System.err.println("Error loading player registry. class not found:");
      e.printStackTrace();
    } catch (FileNotFoundException fnf) {
      System.err.println("No PlayerRegistry found, creating player.registry");
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Player Registry had an exception on Load. Registry not loaded.");
    }
  }
}
