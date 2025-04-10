package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import network.Message.Type;
import registry.PlayerRegistry;
import registry.PlayerRegistry.PlayerRegistrationInfo;

public class ClientManager {
  private static final int PORT = 8000;
  private static final int GAME_PLAYER_COUNT = 2;
  private static Selector selector;
  private static ServerClient clients[] = new ServerClient[GAME_PLAYER_COUNT];
  private static int clientsConnected = 0;

  /**
   * Connect to clients, while displaying a loading animation.
   * Ensures that all clients are connected, assuming they terminate with a 
   *    disconnect message.
   */
  public static void connectToClients() {
    try {
      _connectToClients();
    } catch (IOException | InterruptedException e) {
      System.out.println("Error connecting to clients. Exiting.");
      System.exit(1);
    }
  }
  private static void _connectToClients() throws IOException, InterruptedException {
    // create a server socket channel
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.bind(new InetSocketAddress("127.0.0.1", PORT));
    serverChannel.configureBlocking(false);

    // open the selector for the connection socket
    selector = Selector.open();
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);

    // animation vars
    int animationProg = 0;
    char animationChars[] = {'\\', '|', '/', '-'};

    // wait until both clients are connected
    while (clientsConnected < GAME_PLAYER_COUNT) {
      // get the loading animation's current character
      char aniChar = animationChars[animationProg];

      // clear console and print the animation
      System.out.print("\033[H\033[2J");
      System.out.flush();
      System.out.printf("Waiting for Player %d to connect... %c\n", clientsConnected + 1, aniChar);

      // update animation
      animationProg = (animationProg + 1) % 4;

      checkClientsConnected();
      acceptClients();

      // sleep for animation to run
	  	Thread.sleep(150);
    }

    // alert connections made
    System.out.print("\033[H\033[2J");
    System.out.flush();
    System.out.println("All players gathered. launching game...");
    for (ServerClient connection : clients) {
      connection.sendMessage(new Message(Type.START));
    }
  }

  /**
   * Checks that all clients are connected.
   * If a client is not connected, sets it to null.
   */
  private static void checkClientsConnected() { // TODO: rewrite to work with ^C or errors (probably ping packets?)
    // get messages, if any, and handle
    for (int i = 0; i < clients.length; ++i) {
      ServerClient connection = clients[i];
      if (connection == null) continue;

      ArrayList<Message> recievedMsgs = connection.getMessages();
      for (Message msg : recievedMsgs) {
        if (msg.getType() == Type.DISCONNECT) {
          clients[i] = null;
          --clientsConnected;
        }
      }
    }
  }

  /**
   * Accept any waiting clients.
   * Will not accept clients if full.
   * Non-Blocking.
   */
  private static void acceptClients() throws IOException {
    if (clientsConnected == GAME_PLAYER_COUNT) return;

    int readyChannels = selector.selectNow();
    if (readyChannels > 0) {
      // iterate through available keys
      Set<SelectionKey> selectedKeys = selector.selectedKeys();
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        if (key.isAcceptable()) {
          // get the connection
          ServerSocketChannel readyChannel = (ServerSocketChannel) key.channel();
          ServerClient connection = new ServerClient(readyChannel.accept(), clientsConnected);

          // connect to first empty slot
          for (int i = 0; i < clients.length; ++i) {
            if (clients[i] == null) clients[clientsConnected] = connection;
          }
          clientsConnected++;

          // get client username and password
          Message login = null;
          ArrayList<Message> messages = connection.getMessages();
          while (messages.size() < 1) {
            messages = connection.getMessages();
          }
          for (Message msg : messages) {
            if (msg.getType() == Type.LOGIN) {
              login = msg;
            }
          }

          // get the player associated with the username/password
          PlayerRegistrationInfo loginInfo = PlayerRegistry.getRegisteredPlayer(login.getUsername(), login.getPassword());
          System.out.printf("login attempt: %s + %s", login.getUsername(), login.getPassword());
          if (loginInfo.isSuccess()) {
            connection.sendMessage(new Message(true, null, loginInfo.getPlayer()));
          } else {
            connection.sendMessage(new Message(true, loginInfo.getReason(), null));
          }
          while (true);
        }

        keyIterator.remove();
      }
    }
  }
}
