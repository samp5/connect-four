package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import controller.ChatController;
import controller.GameController;
import javafx.application.Platform;
import utils.SceneManager;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import network.Message.Type;
import network.Message.WinType;

/**
 * Connect to server
 *
 * Send and recieve messgaes
 *
 * Run and listen on worker thread?
 *
 *
 * Route recieved messages to the correct handler (game updates, chat updates)
 *
 */
public class NetworkClient {

  private static Socket socket = null;
  private static ObjectInputStream in;
  private static ObjectOutputStream out;
  private static NetworkThread listener;

  private static GameController gameCTL;
  private static ChatController chatCTL;
  private static Player player;

  // connect to a host
  public static boolean connect(String host, int port, String username, String password)
      throws IOException {
    if (socket == null) {
      socket = new Socket(host, port);
      listener = new NetworkThread(socket);
      listener.start();
    }
    sendMessage(Message.forServerLoginAttempt(username, password));
    return socket.isConnected();
  }

  // get then handle all available messages
  public static int getMessages() {
    int handled = 0;
    try {
      InputStream inputStream = socket.getInputStream();
      int sz = inputStream.available();
      if (sz > 0) {
        in = new ObjectInputStream(inputStream);
        Message message = (Message) in.readObject();
        Platform.runLater(() -> handleMessage(message));
        ++handled;
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.printf("error on getting stream: %s\n", e.getMessage());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.out.println("error parsing message");
    }
    return handled;
  }

  // send to proper controller
  private static void handleMessage(Message msg) {
    switch (msg.getType()) {
      case LOGIN:
        if (msg.isSuccess()) {
          player = msg.getPlayer();
          SceneManager.showScene("loading.fxml");
        } else {
          System.out.println("Error logging in: " + msg.getChatMessage());
        }
        break;
      case CHAT:
        handleChat(msg.getChatMessage(), msg.getUsername(), false);
        break;
      case MOVE:
        gameCTL.recieveMove(msg.getColumn());
        break;
      case START:
        SceneManager.showScene("main.fxml");
        GameLogic.initialize(msg.getPlayer(), msg.getPlayer2(), msg.getRole());
        break;
      case RECONNECT:
        ArrayList<Integer> restoredMoves = msg.getRestoredMoves();
        if (restoredMoves == null) {
          // get moves to send to other player
          sendMessage(Message.forServerReconnect(gameCTL.getMoveHistory()));
        } else {
          // restore moves from other player
          gameCTL.restoreGameBoard(restoredMoves);
        }
        break;
      case FORFEIT:
        gameCTL.recieveForfeit();
        chatCTL.recieveForfeit();
        break;
      case DRAW_REQUEST:
        gameCTL.recieveDrawRequest();
        break;
      case OPPONENT_DISCONNECT:
        chatCTL.opponentDisconnect();
        break;
      case DRAW:
        if (msg.isSuccess()) {
          gameCTL.staleMate();
          chatCTL.draw();
        } else {
          chatCTL.drawDeclined();
        }
      case RESIGN_REQUEST:
        gameCTL.recieveResignRequest();
        break;
      case RESIGN_RESPONSE:
        if (msg.isSuccess()) {
          gameCTL.recieveForfeit();
          chatCTL.resignAccepted();
        } else {
          chatCTL.resignDeclined();
        }
        break;
      default:
        break;
    }
  }

  public static void handleChat(String msg, String username, boolean local) {
    chatCTL.recieveMessage(msg, username, local);
  }

  // send move to server
  public static void sendMove(int column) {
    Player localPlayer = gameCTL.getLocalPlayer();
    Message toSend = Message.forMove(localPlayer.getUsername(), column, localPlayer.getID());
    sendMessage(toSend);
  }

  // send chat to server
  public static void sendChatMessage(String message) {
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      Player localPlayer = gameCTL.getLocalPlayer();
      Message toSend = Message.forChat(localPlayer.getUsername(), message, localPlayer.getID());
      sendMessage(toSend);
    }
  }

  // alert server of game complete
  public static void gameComplete(WinType winType) {
    sendMessage(Message.forGameComplete(player, winType));
  }

  // forfeit a match
  public static void forfeit() {
    gameCTL.forfeit();
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      sendMessage(Message.forSimpleInstruction(Type.FORFEIT));
    }
  }

  // request a draw
  public static void drawRequest() {
    switch (GameLogic.getGameMode()) {
		case LocalAI:
      chatCTL.drawDeclined();
      handleChat("What? No.", "AI", false);
			break;
		case LocalMultiplayer:
      gameCTL.recieveDrawRequest();
			break;
		case Multiplayer:
      sendMessage(Message.forSimpleInstruction(Type.DRAW_REQUEST));
			break;
		case None:
		default:
			break;
    }
  }

  public static void replyDrawRequest(boolean accepted) {
    switch (GameLogic.getGameMode()) {
		case LocalMultiplayer:
      chatCTL.drawDeclined();
      break;
		case Multiplayer:
      sendMessage(Message.forGameResponse(Type.DRAW, accepted));
      break;
		case LocalAI:
		case None:
		default:
			break;
    }
  }

  public static void resignRequest() {
    switch (GameLogic.getGameMode()) {
		case LocalAI:
      handleChat("I'm impressed you even considered the option.", "AI", false);
      chatCTL.resignDeclined();
		case LocalMultiplayer:
      gameCTL.recieveResignRequest();
			break;
		case Multiplayer:
      sendMessage(Message.forSimpleInstruction(Type.RESIGN_REQUEST));
			break;
		case None:
		default:
			break;
    }
  }

  public static void replyResignRequest(boolean accepted) {
    switch (GameLogic.getGameMode()) {
		case LocalMultiplayer:
      chatCTL.resignDeclined();
      break;
		case Multiplayer:
      sendMessage(Message.forGameResponse(Type.RESIGN_RESPONSE, accepted));
      break;
		case LocalAI:
		case None:
		default:
			break;
    }
  }

  private static void sendMessage(Message m) {
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      out.writeObject(m);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  // set controllers so that the network client can "message" the ui
  //
  public static void bindGameController(GameController gc) {
    gameCTL = gc;
  }

  public static void bindChatController(ChatController cc) {
    chatCTL = cc;
  }

  public static void disconnect() {
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      out.writeObject(Message.forServerDisconnect(player));
      socket.close();
      socket = null;
    } catch (Exception e) {
    }
  }

  private static class NetworkThread extends Thread {
    private final Socket socket;

    public NetworkThread(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      while (!socket.isClosed()) {
        getMessages();
      }
    }
  }
}
