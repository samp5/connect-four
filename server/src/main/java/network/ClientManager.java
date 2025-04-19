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

import game.GameManager;
import registry.Leaderboard;
import registry.PlayerRegistry;
import registry.PlayerRegistry.PlayerRegistrationInfo;

/**
 * Handles connections between server and any number of clients
 */
public class ClientManager {
  private static ServerSocketChannel socketChannel;
  private static final int PORT = 8000;
  private static Selector selector;
  private static HashSet<ServerClient> clients = new HashSet<>();
  private static HashSet<ServerClient> toStopListening = new HashSet<>();

  /**
   * Remove a server client from this managed listener.
   */
  public static void removeClientListener(ServerClient connection) {
    synchronized (clients) {
      clients.remove(connection);
    }
  }

  /**
   * Add a server client to this managed listener.
   */
  public static void addClientListener(ServerClient connection) {
    synchronized (clients) {
      clients.add(connection);
    }
  }

  /**
   * Connect to clients, while displaying a loading animation.
   * Ensures that all clients are connected, assuming they terminate with a
   * disconnect message.
   */
  public static void connectToClients() {
    // some base status prints
    System.out.println("Player Registry tracking 0 players");
    System.out.println("0 players currently logged in");
    System.out.println("Currently listening to 0 clients");
    System.out.println("Currently running 0 games");
    System.out.println("Connecting to clients -");

    try {
      _connectToClients();
    } catch (IOException | InterruptedException e) {
      System.err.println("Error connecting to clients. Exiting.");
      System.exit(1);
    }
  }

  private static void _connectToClients() throws IOException, InterruptedException {
    // create a server socket channel
    socketChannel = ServerSocketChannel.open();
    socketChannel.bind(new InetSocketAddress(PORT));
    socketChannel.configureBlocking(false);

    // open the selector for the connection socket
    selector = Selector.open();
    socketChannel.register(selector, SelectionKey.OP_ACCEPT);

    // keep accepting clients and getting messages
    while (true) {
      animateStatus();
      recieveAllMessages();
      acceptClients();
    }
  }

  /**
   * Gathers all messages from all clients connected to the manager.
   */
  private static void recieveAllMessages() {

    ArrayList<ServerClient> curentClients;

    synchronized (clients) {
      curentClients = new ArrayList<>(clients);
    }

    // get messages, if any, and handle
    for (ServerClient connection : curentClients) {
      ArrayList<Message> recievedMsgs = connection.getMessages();
      for (Message msg : recievedMsgs) {
        switch (msg.getType()) {
          case DISCONNECT:
            toStopListening.add(connection);
            PlayerRegistry.logoutPlayer(msg.getPlayer());
            break;
          case LOGIN:
            attemptLogin(connection, msg);
            break;
          case JOIN_GAME:
            GameManager.addToGameQueue(connection);
            break;
          case CANCEL_JOIN:
            GameManager.removeFromQueue(connection.getPlayer());
            break;
          case FETCH_LEADER_BOARD:
            sendLeaderBoard(connection, msg);
            break;
          case GET_SERVER_STATUS:

            sendServerInfo(connection);
            break;
          default:
            break;
        }
      }
    }

    synchronized (clients) {
      // remove that which needs removed
      clients.removeAll(toStopListening);
      toStopListening.clear();
    }
  }

  /**
   * Accept any waiting clients.
   * Non-Blocking.
   */
  private static void acceptClients() throws IOException {
    synchronized (clients) {
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
            ServerClient connection = new ServerClient(readyChannel.accept());
            clients.add(connection);
          }
          keyIterator.remove();
        }
      }
    }
  }

  private static void sendLeaderBoard(ServerClient client, Message msg) {
    try {
      client.sendMessage(Message.forLeaderBoardData(msg.getLeaderBoardViewType(),
          new ArrayList<>(Leaderboard.getLeaderBoard(msg.getLeaderBoardViewType()))));
    } catch (IOException e) {

      e.printStackTrace();
    }
  }

  private static void sendServerInfo(ServerClient client) {
    try {
      client.sendMessage(Message.forServerInfo(PlayerRegistry.loggedInCount(), GameManager.getActiveGameCount()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Communicate with the player registry to handle logging in
   */
  private static void attemptLogin(ServerClient client, Message msg) {
    // get the player associated with the username/password
    PlayerRegistrationInfo loginInfo = PlayerRegistry.getRegisteredPlayer(msg.getUsername(), msg.getPassword());

    try {
      if (loginInfo.isSuccess()) {
        Player p = loginInfo.getPlayer();
        client.setPlayer(p);
        client.sendMessage(Message.forServerLoginResponse(true, null, p));
      } else {
        client.sendMessage(Message.forServerLoginResponse(false, loginInfo.getReason(), null));
      }
    } catch (IOException e) {
      // if an IOException occurs, the client is disconnected
      clients.remove(client);
    }
  }

  /**
   * TERMINAL ANIMATION
   * this uses a lot of escape sequences, a good reference is either the link
   * below or the wikipedia page
   * (ref)[https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797]
   */
  private static long lastFrame = System.currentTimeMillis();
  private static int frametime = 200; // ms
  private static int animationState = 0;
  private static char animationFrames[] = new char[] { '\\', '|', '/', '-' };

  private static void animateStatus() {
    if (System.currentTimeMillis() >= (lastFrame + frametime)) {
      // stats..
      // dont worry about it :)
      System.out.printf("\033[2;26f\033[32m%d\033[0m players\033[0K\033[E",
          PlayerRegistry.registeredCount());
      System.out.printf("\033[32m%d\033[0m players currently logged in\033[0K\033[E",
          PlayerRegistry.loggedInCount());
      System.out.printf("\033[23C\033[32m%d\033[0m clients\033[0K\033[E", clients.size());
      System.out.printf("\033[18C\033[32m%d\033[0m games\033[0K\033[E",
          GameManager.getActiveGameCount());

      // "loading" animation
      lastFrame = System.currentTimeMillis();
      animationState = (animationState + 1) % 4;
      System.out.printf("\033[6;24f\033[D\033[36m%c\033[0m\033[E", animationFrames[animationState]);
    }
  }
}
