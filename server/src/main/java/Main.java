import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import network.Message;
import network.ServerClient;

/**
 * Main Class.
 * likely should not be referenced.
 */
public class Main {
  private static final int PORT = 8000;
  private static final int PLAYER_COUNT = 2;
  private static ServerClient clients[];

  public static void main(String[] args) {
    // create the array of clients based on player count
    clients = new ServerClient[PLAYER_COUNT];

    try {
      connectToClients();
    } catch (Exception e) {
      System.out.println("Errer connecting to clients. Exiting..");
      System.exit(1);
    }
  }

  private static void connectToClients() throws IOException, InterruptedException {
    // create a server socket channel
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.bind(new InetSocketAddress("127.0.0.1", PORT));
    serverChannel.configureBlocking(false);

    // and a selector for the socket
    Selector selector = Selector.open();
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);

    // animation vars
    int connections = 0;
    int animationProg = 0;
    char animationChars[] = {'\\', '|', '/', '-'};

    // wait until both clients are connected
    while (connections < PLAYER_COUNT) {
      // get the loading animation's current character
      char aniChar = animationChars[animationProg];

      // clear console and print the animation
      System.out.print("\033[H\033[2J");
      System.out.flush();
      System.out.printf("Waiting for Player %d to connect... %c\n", connections + 1, aniChar);

      // update animation
      animationProg = (animationProg + 1) % 4;

      // get available connections (non-blocking)
      int readyChannels = selector.selectNow();
      if (readyChannels > 0) {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
          SelectionKey key = keyIterator.next();
          if (key.isAcceptable()) {
            ServerSocketChannel readyChannel = (ServerSocketChannel) key.channel();
            ServerClient connection = new ServerClient(readyChannel.accept(), connections);
            clients[connections] = connection;
            connections++;
            connection.sendMessage(new Message(connection.getConnectionID() + 1));
          }

          keyIterator.remove();
        }
      }

      // sleep for animation to run
	  	Thread.sleep(150);
    }

    // alert connections made
    System.out.print("\033[H\033[2J");
    System.out.flush();
    System.out.println("All players gathered. launching game...");

    // wait for now
    while (true);
  }
}
