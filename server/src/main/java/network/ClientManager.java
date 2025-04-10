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
import network.Message.Type;
import registry.PlayerRegistry;
import registry.PlayerRegistry.PlayerRegistrationInfo;

public class ClientManager {
  private static final int PORT = 8000;
  private static Selector selector;
  private static HashSet<ServerClient> clients = new HashSet<>();

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
    while (true) {
      // get the loading animation's current character
      char aniChar = animationChars[animationProg];

      // clear console and print the animation
      // System.out.print("\033[H\033[2J");
      // System.out.flush();
      // System.out.printf("Waiting for Players to connect... %c\n", aniChar);

      // update animation
      animationProg = (animationProg + 1) % 4;

      recieveAllMessages();
      acceptClients();

      // sleep for animation to run
	  	Thread.sleep(150);
    }

    // alert connections made
    // System.out.print("\033[H\033[2J");
    // System.out.flush();
    // System.out.println("All players gathered. launching game...");
    // for (ServerClient connection : clients) {
    //   connection.sendMessage(new Message(Type.START));
    // }
  }

  /**
   * Checks that all clients are connected.
   * If a client is not connected, sets it to null.
   */
  private static void recieveAllMessages() {
    HashSet<ServerClient> toRemove = new HashSet<>();
    // get messages, if any, and handle
    for (ServerClient connection : clients) {
      ArrayList<Message> recievedMsgs = connection.getMessages();
      for (Message msg : recievedMsgs) {
        switch (msg.getType()) {
			    case CHAT:
			    	break;
			    case DISCONNECT:
            toRemove.add(connection);
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

    clients.removeAll(toRemove);
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
      clients.remove(client);
    }
  }
}
