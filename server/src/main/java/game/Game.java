package game;

import java.io.IOException;
import java.util.ArrayList;

import network.ClientManager;
import network.Message;
import network.Player;
import network.ServerClient;
import network.Player.PlayerRole;
import registry.PlayerRegistry;

/**
 * An instance of a connect-4 game with two players
 */
public class Game {
  GameThread thread;

  public Game(ServerClient p1, ServerClient p2) {
    thread = new GameThread(p1, p2, this);
    thread.start();
  }

  void reconnectPlayer(ServerClient connection) {
    this.thread.reconnectPlayer(connection);
  }

  private class GameThread extends Thread {
    private Game game;
    private ServerClient player1, player2;
    private Long disconnectID = null;
    private boolean active = true;

    public GameThread(ServerClient player1, ServerClient player2, Game game) {
      this.game = game;
      this.player1 = player1;
      this.player2 = player2;
    }

    @Override
    public void run() {
      try {
        // Send start message to each player
        this.player1.sendMessage(
            Message.forGameStart(player1.getPlayer(), player2.getPlayer(), PlayerRole.PlayerOne));
        this.player2.sendMessage(
            Message.forGameStart(player2.getPlayer(), player1.getPlayer(), PlayerRole.PlayerTwo));

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
          case DISCONNECT:
            if (disconnectID == null) {
              if (connection.getPlayer().getID() == player1.getPlayer().getID()) {
                player2.sendMessage(Message.forOpponentDisconnect(player1.getPlayer()));
              } else {
                player1.sendMessage(Message.forOpponentDisconnect(player2.getPlayer()));
              }
              PlayerRegistry.logoutPlayer(connection.getPlayer());
              disconnectID = connection.getPlayer().getID();
              GameManager.dcGames.put(disconnectID, game);
              return;
            }
            PlayerRegistry.logoutPlayer(connection.getPlayer());
            GameManager.dcGames.remove(disconnectID);
            active = false;
            return;
          case COMPLETE:
            Player p = msg.getPlayer();
            PlayerRegistry.updatePlayerStats(p, msg.isSuccess());
            break;
          case START:
          case LOGIN:
            break;
          case CHAT:
          case MOVE:
          case RECONNECT:
          case FORFEIT:
          case DRAW_REQUEST:
          case DRAW:
          case RESIGN_REQUEST:
          case RESIGN_RESPONSE:
          default: // redirect by default
            // don't redirect if there was a DC
            if (this.disconnectID != null)
              return;

            // redirect message to other player
            if (connection.getPlayer().getID() == player1.getPlayer().getID()) {
              player2.sendMessage(msg);
            } else {
              player1.sendMessage(msg);
            }
            break;
        }
      }
    }

    private void reconnectPlayer(ServerClient connection) {
      try {
        if (disconnectID == player1.getPlayer().getID()) {
          player1 = connection;
          player1.sendMessage(
              Message.forGameStart(player1.getPlayer(), player2.getPlayer(), PlayerRole.PlayerOne));
          player2.sendMessage(Message.forServerReconnect((ArrayList<Integer>) null));
        } else {
          player2 = connection;
          player2.sendMessage(
              Message.forGameStart(player2.getPlayer(), player1.getPlayer(), PlayerRole.PlayerTwo));
          player1.sendMessage(Message.forServerReconnect((ArrayList<Integer>) null));
        }
        ClientManager.removeClientListener(connection);
        GameManager.dcGames.remove(disconnectID);
        disconnectID = null;
      } catch (IOException e) {
      }
    }
  }
}
