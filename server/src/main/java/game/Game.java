package game;

import java.io.IOException;
import java.util.ArrayList;

import network.Message;
import network.ServerClient;
import network.Player.PlayerRole;
import registry.PlayerRegistry;

/**
 * An instance of a connect-4 game with two players
 */
public class Game {
  public Game(ServerClient p1, ServerClient p2) {
    new GameThread(p1, p2).start();
  }

  private class GameThread extends Thread {
    private ServerClient player1, player2;
    private boolean active = true;

    public GameThread(ServerClient player1, ServerClient player2) {
      this.player1 = player1;
      this.player2 = player2;
    }

    @Override
    public void run() {
      try {
        // Send start message to each player
        this.player1.sendMessage(new Message(player1.getPlayer(), player2.getPlayer(), PlayerRole.PlayerOne));
        this.player2.sendMessage(new Message(player2.getPlayer(), player1.getPlayer(), PlayerRole.PlayerTwo));

        // run the game by redirecting any messages recieved to where they need
        // to go
        while (active) {
          handleMessages(player1);
          handleMessages(player2);
        }
      } catch (IOException e) {
      }
      GameManager.gameCount--;
    }

    private void handleMessages(ServerClient connection) throws IOException {
      ArrayList<Message> messages = connection.getMessages();
      for (Message msg : messages) {
        switch (msg.getType()) {
          case CHAT:
            // redirect message to other player
            if (connection.getPlayer().getID() == player1.getPlayer().getID()) {
              player2.sendMessage(msg);
            } else {
              player1.sendMessage(msg);
            }
            break;
          case DISCONNECT:
            PlayerRegistry.logoutPlayer(player1.getPlayer());
            PlayerRegistry.logoutPlayer(player2.getPlayer());
            active = false;
            return;
          case LOGIN:
            break;
          case MOVE:
            // redirect message to other player
            if (connection.getPlayer().getID() == player1.getPlayer().getID()) {
              player2.sendMessage(msg);
            } else {
              player1.sendMessage(msg);
            }
            break;
          case START:
            break;
          default:
            break;
        }
      }
    }
  }
}
