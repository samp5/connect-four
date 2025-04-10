package game;

import network.Player;
import network.ServerClient;

public class GameManager {
  private static ServerClient waiting = null;

  public static void addToGameQueue(ServerClient client) {
    // if there is nobody waiting, add to wait queue
    if (waiting == null) {
      waiting = client;
      return;
    }

    // otherwise start game with the waiting player, then clear the wait status
    new Game(waiting, client).begin();
    waiting = null;
  }

  public static void removeFromQueue(Player p) {
    if (waiting != null && waiting.getPlayer().getUsername().equals(p.getUsername())) {
      waiting = null;
    }
  }
}
