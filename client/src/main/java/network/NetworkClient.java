package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import controller.ChatController;
import controller.ConnectionsController;
import controller.GameController;
import controller.LeaderBoardController;
import controller.LoadingController;
import controller.ServerMenuController;
import controller.utils.RecentConnectionRegistry;
import controller.utils.FriendUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;
import logic.AI;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import network.Message.LeaderBoardView;
import network.Message.Type;
import network.Message.WinType;
import network.UserProfile.ProfilePicture;
import utils.NotificationManager.NotificationType;

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
  private static LoadingController loadCTL;
  private static ChatController chatCTL;
  private static ServerMenuController serverMenuCTL;
  private static ConnectionsController connectionCTL;
  private static Player player;

  // connect to a host
  public static void connect(String host, int port, String username, String password, Callable<Void> failCallback)
      throws IOException {
    // run this in a thread so that non-instant connections dont hang
    new Thread(() -> {
      if (socket == null || socket.isClosed()) {
        // try to connect for 5 seconds. if fail, stop
        try {

          final Future<Object> f = Executors.newSingleThreadExecutor().submit(() -> {
            socket = new Socket(host, port);
            return 1;
          });
          f.get(5, TimeUnit.SECONDS);

        } catch (Exception e0) {

          // on fail, execute the failure callback
          try {
            failCallback.call();
          } catch (Exception e) {
          }
          return;

        }
      }

      onConnect(username, password);
    }).start();
  }

  private static void onConnect(String username, String password) {
    NetworkThread.reset();
    sendMessage(Message.forServerLoginAttempt(username, password));
    listener = new NetworkThread(socket);
    listener.start();
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
      return -1;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.out.println("error parsing message");
      return -1;
    }
    return handled;
  }

  // send to proper controller
  private static void handleMessage(Message msg) {
    switch (msg.getType()) {
      case LOGIN:
        if (msg.isSuccess()) {
          player = msg.getPlayer();
          SceneManager.showScene(SceneSelections.SERVER_MENU);
          ServerMenuController ctl = (ServerMenuController) SceneManager.getCurrentController();
          ctl.setServerInfo(RecentConnectionRegistry.getMostRecentConnection().get());
        } else {
          connectionCTL.recieveNotification("Error logging in " + msg.getChatMessage());
        }
        break;
      case CHAT:
        handleChat(msg.getChatMessage(), msg.getUsername(), false);
        break;
      case MOVE:
        gameCTL.recieveMove(msg.getColumn());
        break;
      case START:
        SceneManager.showScene(SceneSelections.GAME);
        GameLogic.initialize(msg.getPlayer(), msg.getPlayer2(), msg.getRole());
        gameCTL.showPlayerRoles();
        chatCTL.fetchOpponentProfiles();
        break;
      case RECONNECT:
        ArrayList<Integer> restoredMoves = msg.getRestoredMoves();
        if (restoredMoves == null) {
          // get moves to send to other player
          sendMessage(Message.forServerReconnect(gameCTL.getMoveHistory()));
          gameCTL.recieveOpponentReconnect();
          chatCTL.recieveNotification("Opponent has reconnected", NotificationType.INFORMATION);
        } else {
          // restore moves from other player
          gameCTL.restoreGameBoard(restoredMoves);
        }
        break;
      case DRAW:
        if (msg.isSuccess()) {
          gameCTL.staleMate();
          chatCTL.drawAccepted();
        } else {
          chatCTL.drawDeclined();
        }
        break;
      case DRAW_REQUEST:
        gameCTL.recieveDrawRequest();
        break;
      case OPPONENT_DISCONNECT:
        chatCTL.opponentDisconnect();
        gameCTL.recieveOpponentDisconnect();
        break;
      case OPPONENT_RETURN_TO_LOBBY:
        chatCTL.opponentDisconnect();
        gameCTL.recieveOpponentReturnToLobby();
        break;
      case RESIGN:
        gameCTL.recieveResign();
        chatCTL.recieveResign();
        break;
      case RESIGN_REQUEST:
        gameCTL.recieveResignRequest();
        break;
      case RESIGN_RESPONSE:
        if (msg.isSuccess()) {
          gameCTL.recieveResign();
          chatCTL.resignAccepted();
        } else {
          chatCTL.resignDeclined();
        }
        break;
      case REMATCH:
        gameCTL.rematch();
        break;
      case REMATCH_REQUEST:
        chatCTL.rematchRequest();
        gameCTL.recieveRematchRequest();
        break;
      case LEADER_BOARD_DATA:
        LeaderBoardController.recieveData(msg.getLeaderBoardData(), msg.getLeaderBoardViewType());
        break;
      case SERVER_STATUS:
        serverMenuCTL.setPlayerInfo(msg.getNumPlayers(), msg.getNumActiveGames());
        break;
      case FRIEND_REQUEST:
        chatCTL.recieveFriendRequest();
        break;
      case FRIEND_REQUEST_RESPONSE:
        if (msg.isSuccess()) {
          PlayerData.friendsUpdated();
        }
        chatCTL.recieveFriendRequestResponse(msg.isSuccess());
        break;
      case FRIEND_ONLINE_STATUS:
        PlayerData.friendOnlineStatus(msg.getUsername(), msg.isSuccess());
        FriendUtils.friendOnlineStatus(msg.getPlayerID(), msg.isSuccess());
        String notificationStr = msg.getUsername() + " is now " + (msg.isSuccess() ? "online" : "offline");
        NotificationType type = msg.isSuccess() ? NotificationType.FRIEND_GOOD : NotificationType.FRIEND_BAD;
        switch (SceneManager.getCurrentScene()) {
          case SERVER_MENU:
            serverMenuCTL.recieveNotification(notificationStr, type);
            serverMenuCTL.updateFriendOnlineView(msg.getUsername(), msg.isSuccess());
            break;
          case GAME:
            chatCTL.recieveNotification(notificationStr, type);
            break;
          case LOADING:
            loadCTL.recieveNotification(notificationStr, type);
            break;
          default:
            break;
        }
        break;
      case FRIEND_CHAT:
        FriendUtils.receiveChat(msg.getSender(), msg.getChatMessage());
        break;
      case FETCH_FRIENDS:
        PlayerData.friendsUpdated();
        break;
      case GAME_INVITATION_CANCEL:
        switch (SceneManager.getCurrentScene()) {
          case LOADING:
            loadCTL.recieveNotification(msg.getUsername() + " canceled their invitation", NotificationType.FRIEND_BAD);
            break;
          case SERVER_MENU:
            serverMenuCTL.recieveNotification(msg.getUsername() + " canceled their invitation",
                NotificationType.FRIEND_BAD);
            break;
          default:
            break;
        }
        break;
      case GAME_INVITATION_RESPONSE:
        // this is the response from the other player
        switch (SceneManager.getCurrentScene()) {
          case LOADING:
            if (msg.isSuccess()) {
              loadCTL.recievePrompt(msg.getUsername() + " accepted. Start game?",
                  NotificationType.FRIEND_GOOD, (e -> {
                    NetworkClient.sendMessage(Message.forGameInvitationGameStart(msg.getInvitor(), msg.getInvited()));
                  }), (e -> {
                    NetworkClient.sendMessage(
                        Message.forGameInvitationCancel(player.getUsername(), msg.getInvitor(), msg.getInvited()));
                  }));
            } else {
              loadCTL.recieveNotification(msg.getUsername() + " denied your invitation", NotificationType.FRIEND_BAD);
            }
            break;
          case SERVER_MENU:
            if (msg.isSuccess()) {
              serverMenuCTL.recievePrompt(msg.getUsername() + " accepted your invitation. Start game?",
                  NotificationType.FRIEND_GOOD, (e -> {
                    NetworkClient.sendMessage(Message.forGameInvitationGameStart(msg.getInvitor(), msg.getInvited()));
                  }), (e -> {
                    serverMenuCTL.reenableInvite(msg.getUsername());
                    NetworkClient.sendMessage(
                        Message.forGameInvitationCancel(player.getUsername(), msg.getInvitor(), msg.getInvited()));
                  }));
            } else {
              serverMenuCTL.recieveNotification(msg.getUsername() + " denied your invitation",
                  NotificationType.FRIEND_BAD);
            }
            break;
          default:
            break;

        }
        break;
      case GAME_INVITATION:

        String msgStr = msg.getUsername() + " is inviting you to a game";
        NotificationType nT = NotificationType.FRIEND_GOOD;
        EventHandler<ActionEvent> onYes = (e -> NetworkClient
            .sendMessage(
                Message.forGameInvitationResponse(player.getUsername(), true, msg.getInvitor(), msg.getInvited())));
        EventHandler<ActionEvent> onNo = (e -> NetworkClient
            .sendMessage(
                Message.forGameInvitationResponse(player.getUsername(), false, msg.getInvitor(), msg.getInvited())));
        switch (SceneManager.getCurrentScene()) {
          case LOADING:
            loadCTL.recievePrompt(msgStr, nT, onYes, onNo);
            break;
          case SERVER_MENU:
            serverMenuCTL.recievePrompt(msgStr, nT, onYes, onNo);
            break;
          default:
            break;
        }
        if (SceneManager.getCurrentScene() == SceneSelections.SERVER_MENU) {
        }
        break;
      case PROFILE_DATA:
        /*
         * These might need to be split into different message types if we need to do
         * anything more than these two things
         */
        if (msg.getProfile().getId().equals(player.getID())) {
          PlayerData.recieveProfile(msg.getProfile());
        } else {
          // requesting the opponent for the main game view
          chatCTL.recieveOpponentProfile(msg.getProfile());
        }
        break;
      case FRIEND_LIST_DATA:
        PlayerData.recieveFriends(msg.getFriendsList());
        break;
      default:
        break;
    }
  }

  public static void handleChat(String msg, String username, boolean local) {
    chatCTL.recieveMessage(msg, username, local);
  }

  public static void sendOpponentFriendRequest() {
    sendMessage(Message.forFriendRequest(player.getID(), GameLogic.getRemotePlayer().getID()));
  }

  // send move to server
  public static void sendMove(int column) {
    Player localPlayer = gameCTL.getLocalPlayer();
    Message toSend = Message.forMove(localPlayer.getUsername(), column, localPlayer.getID());
    sendMessage(toSend);
  }

  public static void sendChatMessageTo(Long ID, String message) {
    sendMessage(Message.forChatTo(player, message, ID));
  }

  // send chat to server
  public static void sendChatMessage(String message) {
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      Player localPlayer = gameCTL.getLocalPlayer();
      Message toSend = Message.forChat(localPlayer.getUsername(), message, localPlayer.getID());
      sendMessage(toSend);
    }
  }

  public static void removeFriend(Long friendID) {
    sendMessage(Message.forFriendRemove(friendID));
  }

  // get the leader board with the specified view
  public static void fetchLeaderBoard(LeaderBoardView view) {
    sendMessage(Message.forFetchLeaderboard(view));
  }

  public static void fetchFriends() {
    sendMessage(Message.forSimpleInstruction(Type.FETCH_FRIENDS));
  }

  // get the profile for this player
  public static void fetchProfile() {
    sendMessage(Message.forFetchProfile(player.getID()));
  }

  // get the profile for the opponent player
  public static void fetchOpponentProfile() {
    sendMessage(Message.forFetchProfile(GameLogic.getRemotePlayer().getID()));
  }

  public static void updateProfilePicture(ProfilePicture pic) {
    sendMessage(Message.forProfilePictureUpdate(player.getID(), pic));
  }

  // alert server of game complete
  public static void gameComplete(WinType winType) {
    sendMessage(Message.forGameComplete(player, winType));
  }

  // resign a match
  public static void resign() {
    gameCTL.resign();
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      sendMessage(Message.forSimpleInstruction(Type.RESIGN));
    }
  }

  public static void returnToLobby() {
    sendMessage(Message.forReturnToLobbyRequest(player));
  }

  public static void rematchRequest() {
    sendMessage(Message.forSimpleInstruction(Type.REMATCH_REQUEST));
  }

  public static void acceptRematch() {
    sendMessage(Message.forSimpleInstruction(Type.REMATCH));
  }

  // request a draw
  public static void drawRequest() {
    switch (GameLogic.getGameMode()) {
      case LocalAI:
        chatCTL.drawDeclined();
        handleChat("what? no.", AI.getName(), false);
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

  public static void replyFriendRequest(boolean accepted) {
    sendMessage(Message.forFriendRequestReply(GameLogic.getRemotePlayer().getID(), player.getID(), accepted));

    if (accepted) {
      PlayerData.friendsUpdated();
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
        handleChat("i'm impressed you even considered the option.", AI.getName(), false);
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

  public static void joinGame() {
    sendMessage(Message.forSimpleInstruction(Type.JOIN_GAME));
  }

  public static void cancelJoinGame() {
    sendMessage(Message.forSimpleInstruction(Type.CANCEL_JOIN));
  }

  public static void getServerInfo() {
    sendMessage(Message.forSimpleInstruction(Type.GET_SERVER_STATUS));
  }

  public static void replyResignRequest(boolean accepted) {
    switch (GameLogic.getGameMode()) {
      case LocalMultiplayer:
        if (accepted) {
          gameCTL.recieveResign();
          chatCTL.resignAccepted();
        } else {
          chatCTL.resignDeclined();
        }
        break;
      case Multiplayer:
        sendMessage(Message.forGameResponse(Type.RESIGN_RESPONSE, accepted));
      case LocalAI:
        if (accepted)
          gameCTL.resign();
        break;
      case None:
      default:
        break;
    }
  }

  private static void sendMessage(Message m) {
    if (socket == null) {
      return;
    }
    synchronized (socket) {
      try {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(m);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  public static void checkAIMaxMode() {
    chatCTL.checkAIMaxMode();
    gameCTL.checkAIMaxMode();
  }

  // set controllers so that the network client can "message" the ui
  //
  public static void bindGameController(GameController gc) {
    gameCTL = gc;
  }

  public static void bindLoadingController(LoadingController lc) {
    loadCTL = lc;
  }

  // set controllers so that the network client can "message" the ui
  //
  public static void bindConnectionController(ConnectionsController cc) {
    connectionCTL = cc;
  }

  public static void bindChatController(ChatController cc) {
    chatCTL = cc;
  }

  public static void bindServerMenuController(ServerMenuController sc) {
    serverMenuCTL = sc;
  }

  public static void inviteToGame(Long id) {
    sendMessage(Message.forGameInvitation(player.getUsername(), player.getID(), id));
  }

  public static void disconnect() {
    PlayerData.reset();
    FriendUtils.reset();
    LeaderBoardController.reset();

    if (socket == null) {
      return;
    }
    try {
      if (player != null) {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(Message.forServerDisconnect(player));
      }
      socket.close();
      socket = null;
    } catch (Exception e) {
    }
  }

  private static class NetworkThread extends Thread {
    private final Socket socket;
    private boolean toStop;
    private static final ArrayList<NetworkThread> runningList = new ArrayList<>();

    public static void reset() {
      for (NetworkThread t : runningList) {
        t.stopThread();
      }
    }

    public NetworkThread(Socket socket) {
      this.socket = socket;
      this.toStop = false;

      runningList.add(this);
    }

    @Override
    public void run() {
      while (!socket.isClosed()) {
        if (toStop)
          break;

        getMessages();
      }

      runningList.remove(this);
    }

    public void stopThread() {
      this.toStop = true;
    }
  }
}
