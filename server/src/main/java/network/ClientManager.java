package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import game.GameManager;
import network.Message.Type;
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
  private static HashSet<ServerClient> clientsInGame = new HashSet<>();
  private static HashSet<ServerClient> toStopListening = new HashSet<>();

  /**
   * Remove a server client from this managed listener.
   */
  public static void removeClientListener(ServerClient connection) {
    synchronized (clients) {
      clients.remove(connection);
    }
    synchronized (clientsInGame) {
      clientsInGame.add(connection);
    }
  }

  /**
   * Add a server client to this managed listener.
   */
  public static void addClientListener(ServerClient connection) {
    synchronized (clients) {
      clients.add(connection);
    }
    synchronized (clientsInGame) {
      clientsInGame.remove(connection);
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
            notifyFriends(connection, Message.forFriendOnlineStatus(connection.getPlayer(), false));
            break;
          case LOGIN:
            if (attemptLogin(connection, msg)) {
              notifyFriends(connection, Message.forFriendOnlineStatus(connection.getPlayer(), true));
            }
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
          case FETCH_PROFILE:
            sendProfile(connection, msg.getPlayerID());
            break;
          case PROFILE_PIC_UPDATE:
            PlayerRegistry.updateProfilePicture(msg.getPlayerID(), msg.getProfilePicture());
            notifyFriends(connection, Message.forSimpleInstruction(Type.FETCH_FRIENDS));
            break;
          case GAME_INVITATION:
            // forward the invitation to the target player
            sendToByID(msg.getInvited(), msg);
            break;
          case GAME_INVITATION_RESPONSE:
            // send the target player the response
            sendToByID(msg.getInvitor(), msg);
            // if we need to start a game, do it
            if (msg.isSuccess()) {
              getClientByID(msg.getInvitor()).ifPresent(con2 -> GameManager.acceptInvitation(connection, con2));
            }
            break;
          case FETCH_FRIENDS:
            sendFriends(connection);
            break;
          case FRIEND_CHAT:
            sendToByID(msg.getPlayerID(), msg);
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

  private static void sendFriends(ServerClient client) {
    try {
      ArrayList<UserProfile> friends = PlayerRegistry.getUsersFriendList(client.getPlayer().getID());
      client.sendMessage(Message.forFriendListData(friends));
    } catch (IOException e) {
    }
  }

  private static void sendServerInfo(ServerClient client) {
    try {
      client.sendMessage(Message.forServerInfo(PlayerRegistry.loggedInCount(), GameManager.getActiveGameCount()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void sendProfile(ServerClient client, Long playerID) {
    try {
      client.sendMessage(Message.forProfileData(PlayerRegistry.getUserProfileByID(playerID).get()));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (NoSuchElementException dne) {
      dne.printStackTrace();
    }
  }

  public static void sendToByID(Long targetPlayer, Message msg) {
    getClientByID(targetPlayer).ifPresent(target -> {
      try {
        target.sendMessage(msg);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    });
  }

  private static void sendTo(ServerClient client, Message msg) {
    try {
      client.sendMessage(msg);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private static void notifyFriends(ServerClient client, Message msg) {
    synchronized (clients) {
      HashSet<Long> friendIDs = PlayerRegistry.getUsersFriendIDs(client.getPlayer().getID());
      friendIDs.stream().filter(id -> PlayerRegistry.playerIsOnline(id))
          .forEach(id -> getClientByID(id).ifPresent(c -> {
            try {
              c.sendMessage(msg);
            } catch (IOException ioe) {
              ioe.printStackTrace();
            }
          }));
    }
  }

  /**
   * Get the client associated with this ID if it exists.
   *
   * It may not exist for one of the following reasons:
   * 1. The client already disconnected.
   * 2. The client is in a game
   * 3. The id never corresponded to any client
   */
  public static Optional<ServerClient> getClientByID(Long id) {
    Optional<ServerClient> sc;
    synchronized (clients) {
      sc = clients.stream().filter(c -> c.getPlayer().getID().equals(id)).findFirst();
    }
    if (sc.isPresent()) {
      return sc;
    } else {
      synchronized (clientsInGame) {
        return clientsInGame.stream().filter(c -> c.getPlayer().getID().equals(id)).findFirst();
      }
    }
  }

  /**
   * Communicate with the player registry to handle logging in
   */
  private static boolean attemptLogin(ServerClient client, Message msg) {
    // get the player associated with the username/password
    PlayerRegistrationInfo loginInfo = PlayerRegistry.getRegisteredPlayer(msg.getUsername(), msg.getPassword());

    try {
      if (loginInfo.isSuccess()) {
        Player p = loginInfo.getPlayer();
        client.setPlayer(p);
        client.sendMessage(Message.forServerLoginResponse(true, null, p));
        return true;
      } else {
        client.sendMessage(Message.forServerLoginResponse(false, loginInfo.getReason(), null));
      }
    } catch (IOException e) {
      // if an IOException occurs, the client is disconnected
      clients.remove(client);
    }
    return false;
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
