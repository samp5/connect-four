package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import controller.ChatController;
import controller.GameController;

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
  private static Thread listener;

  private static GameController gameCTL;
  private static ChatController chatCTL;


  // connect to a host
  public static boolean connect(String host, int port) throws IOException {
    socket = new Socket(host, port);
    return socket.isConnected();
  }

  // listener thread
  private static void startListener() {}

  public static int getMessages() {
    int handled = 0;
    try {
      InputStream inputStream = socket.getInputStream();
      int sz = inputStream.available();
      if (sz > 0) {
        in = new ObjectInputStream(inputStream);
        Message message = (Message) in.readObject();
        handleMessage(message);
        ++handled;
      }
    } catch (IOException e) {
      System.out.println("error on getting stream");
    } catch (ClassNotFoundException e) {
      System.out.println("error parsing message");
    }
    return handled;
  }

  // send to proper controller
  private static void handleMessage(Message msg) {
    switch (msg.getType()) {
		case CHAT:
			break;
		case MOVE:
			break;
		case REG:
      System.out.printf("Registered as player %d\n", msg.getPlayerID());
			break;
		default:
			break;
    }
  }

  // send move to server
  public static void sendMove(int column) {}

  // send chat to server
  public static void sendChatMessage(String message) {}

  // set controllers so that the network client can "message" the ui
  //
  public static void bindGameController(GameController gc) {
    gameCTL = gc;
  }

  public static void bindChatController(ChatController cc) {
    chatCTL = cc;
  }

  public static void disconnect() {}
}
