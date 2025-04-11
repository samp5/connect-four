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
import registry.PlayerRegistry;
import registry.PlayerRegistry.PlayerRegistrationInfo;


/**
 * Handles connections between server and any number of clients
 */
public class ClientManager {
  private static final int PORT = 8000;
  private static Selector selector;
  private static HashSet<ServerClient> clients = new HashSet<>();
  private static HashSet<ServerClient> toStopListening = new HashSet<>();

  /**
   * Remove a server client from this managed listener.
   */
  public static void removeClientListener(ServerClient connection) {
    toStopListening.add(connection);
  }

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

    // keep accepting clients and getting messages
    while (true) {
      recieveAllMessages();
      acceptClients();
    }
  }

  /**
   * Gathers all messages from all clients connected to the manager.
   */
  private static void recieveAllMessages() {
    // get messages, if any, and handle
    for (ServerClient connection : clients) {
      ArrayList<Message> recievedMsgs = connection.getMessages();
      for (Message msg : recievedMsgs) {
        switch (msg.getType()) {
			    case CHAT:
			    	break;
			    case DISCONNECT:
            toStopListening.add(connection);
            PlayerRegistry.logoutPlayer(msg.getPlayer());
			    	break;
			    case LOGIN:
            attemptLogin(connection, msg);
			    	break;
			    case MOVE:
			    	break;
			    case START:
			    	break;
			    default:
			    	break;
        }
      }
    }

    // remove that which needs removed
    clients.removeAll(toStopListening);
    toStopListening.clear();
  }

  /**
   * Accept any waiting clients.
   * Non-Blocking.
   */
  private static void acceptClients() throws IOException {
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

  /**
   * Communicate with the player registry to handle logging in
   */
  private static void attemptLogin(ServerClient client, Message msg) {
    // get the player associated with the username/password
    PlayerRegistrationInfo loginInfo = PlayerRegistry.getRegisteredPlayer(msg.getUsername(), msg.getPassword());

    try {
      if (loginInfo.isSuccess()) {
        Player p = loginInfo.getPlayer();
        client.sendMessage(new Message(true, null, p));
        client.setPlayer(p);
        GameManager.addToGameQueue(client);
      } else {
        client.sendMessage(new Message(false, loginInfo.getReason(), null));
      }
    } catch (IOException e) {
      // if an IOException occurs, the client is disconnected
      clients.remove(client);
    }
  }
}
