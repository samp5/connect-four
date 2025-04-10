package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import controller.ChatController;
import controller.GameController;
import javafx.application.Platform;
import network.Message.Type;
import utils.SceneManager;

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

  private static Socket socket;
  private static ObjectInputStream in;
  private static ObjectOutputStream out;
  private static NetworkThread listener;

  private static GameController gameCTL;
  private static ChatController chatCTL;


  // connect to a host
  public static boolean connect(String host, int port, String username, String password) throws IOException {
    socket = new Socket(host, port);
    listener = new NetworkThread(socket);
    listener.start();
    sendMessage(new Message(username, password));
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
      System.out.printf("error on getting stream: %s\n", e.getMessage());
    } catch (ClassNotFoundException e) {
      System.out.println("error parsing message");
    }
    return handled;
  }

  // send to proper controller
  private static void handleMessage(Message msg) {
    switch (msg.getType()) {
      case LOGIN:
        if (msg.isSuccess()) {
          SceneManager.showScene("loading.fxml");
        } else {
          System.out.println("Error logging in: " + msg.getChatMessage());
        }
        break;
		  case CHAT:
		  	break;
		  case MOVE:
		  	break;
		  case REG:
        SceneManager.showScene("loading.fxml");
		  	break;
      case START:
        SceneManager.showScene("main.fxml");
        break;
		  default:
		  	break;
    }
  }

  // send move to server
  public static void sendMove(int column) {
    Player localPlayer = gameCTL.getLocalPlayer();
    Message toSend = new Message(localPlayer.getUsername(), column, localPlayer.getID());
    sendMessage(toSend);
  }

  // send chat to server
  public static void sendChatMessage(String message) {
    Player localPlayer = gameCTL.getLocalPlayer();
    Message toSend = new Message(localPlayer.getUsername(), message, localPlayer.getID());
    sendMessage(toSend);
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
      socket.getOutputStream().write(new Message(Type.DISCONNECT).asBytes());
      socket.close();
    } catch (Exception e) {}
  }

  private static class NetworkThread extends Thread {
    private final Socket socket;

    public NetworkThread(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      System.out.println("NetworkThread began");
      while (!socket.isClosed()) {
        getMessages();
      }
      System.out.println("NetworkThread ended");
    }
  }
}
