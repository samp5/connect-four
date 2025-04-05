package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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


  public static void connect(String host, int port) throws IOException {
    // TODO
  }

  // listener thread
  private static void startListener() {}

  // send to proper controller
  private static void handleMessage(Message msg) {}

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
