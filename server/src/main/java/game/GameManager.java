package game;

import network.Player;
import network.ServerClient;
import network.ClientManager;

/**
 * Handles active games, game queues, and beginning new games
 */
public class GameManager {
  static int gameCount = 0;
  private static ServerClient waiting = null;

  /**
   * Add a client to the game queue, and if available, starts a new game
   */
  public static void addToGameQueue(ServerClient client) {
    // if there is nobody waiting, add to wait queue
    if (waiting == null) {
      waiting = client;
      return;
    }

    // once the game is started, the client manager should not keep watching the
    // clients, so take listening away
    ClientManager.removeClientListener(waiting);
    ClientManager.removeClientListener(client);

    // otherwise start game with the waiting player, then clear the wait status
    new Game(waiting, client);
    ++gameCount;
    waiting = null;
  }

  /**
   * Remove a player from queue if they are in it.
   * If they arent in queue, nothing happens.
   */
  public static void removeFromQueue(Player p) {
    if (waiting != null && waiting.getPlayer().getUsername().equals(p.getUsername())) {
      waiting = null;
    }
  }

  public static int getActiveGameCount() {
    return gameCount;
  }
}
