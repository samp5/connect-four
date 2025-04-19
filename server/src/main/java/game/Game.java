package game;

import java.io.IOException;
import java.util.ArrayList;

import network.ClientManager;
import network.Message;
import network.Player;
import network.ServerClient;
import network.Player.PlayerRole;
import registry.PlayerRegistry;
import registry.Leaderboard;

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

    // TO TEST LEADERBOARD
    private int completeRequestRecieved = 0;

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
        // if the game is ending, always ensure that we don't try and reconnect these
        // clients
        GameManager.dcGames.remove(disconnectID);
      } catch (IOException e) {
        e.printStackTrace();
      }
      GameManager.gameCount--;
    }

    private void handleMessages(ServerClient connection) throws IOException {
      // Ignore any client who is disconnected
      if (disconnectID == connection.getPlayer().getID()) {
        return;
      }
      ArrayList<Message> messages = connection.getMessages();
      for (Message msg : messages) {
        switch (msg.getType()) {
          case DISCONNECT:
            if (disconnectID == null) {
              // send our opponent a message that we are disconnecting
              sendToOpponent(connection, Message.forServerDisconnect(opponent(connection).getPlayer()));

              // log out
              PlayerRegistry.logoutPlayer(connection.getPlayer());

              // set the disconnectID
              disconnectID = connection.getPlayer().getID();
              GameManager.dcGames.put(disconnectID, game);
            } else {
              PlayerRegistry.logoutPlayer(connection.getPlayer());
              active = false;
            }
            break;
          case COMPLETE:
            Player p = msg.getPlayer();
            PlayerRegistry.updatePlayerStats(p, msg.getWinType());
            if (completeRequestRecieved % 2 == 0) {
              Player opponent = opponent(connection).getPlayer();
              Leaderboard.updateElo(p.getID(), opponent.getID(), msg.getWinType());
              completeRequestRecieved += 1;
            }
            break;
          case RETURN_TO_LOBBY:
            if (disconnectID == null) {
              sendToOpponent(connection, Message.forOpponentReturnToLobby(opponent(connection).getPlayer()));
              disconnectID = connection.getPlayer().getID();
              ClientManager.addClientListener(connection);
              return;
            } else {
              ClientManager.addClientListener(connection);
              active = false;
            }
            break;
          case START:
          case FETCH_PROFILE:
            ClientManager.sendProfile(connection, msg.getPlayerID());
            break;
          case LOGIN:
            break;
          case FETCH_LEADER_BOARD:
          case CHAT:
          case MOVE:
          case RECONNECT:
          case DRAW:
          case DRAW_REQUEST:
          case RESIGN:
          case RESIGN_REQUEST:
          case RESIGN_RESPONSE:
          case REMATCH_REQUEST:
          case FRIEND_REQUEST:
          case REMATCH:
          case FRIEND_REQUEST_RESPONSE:
            // we also want to redirect this
            PlayerRegistry.addFriends(msg.getBefrienderID(), msg.getBefriendedID());
            // NO BREAK
          default: // redirect by default
            // only redirect if both players are connected
            if (disconnectID == null) {
              sendToOpponent(connection, msg);
            }
            break;
        }
      }
    }

    private ServerClient opponent(ServerClient connection) {
      return connection.getPlayer().getID() == player1.getPlayer().getID() ? player2 : player1;
    }

    private void sendToOpponent(ServerClient connection, Message msg) throws IOException {
      // if this player is disconnected do nothing
      if (connection.getPlayer().getID() == disconnectID) {
        return;
      }
      ServerClient opponent = opponent(connection);

      // if the opponent is already disconnected
      if (opponent.getPlayer().getID() == disconnectID) {
        return;
      }

      opponent.sendMessage(msg);

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
