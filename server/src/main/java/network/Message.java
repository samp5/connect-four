package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import network.Player.PlayerRole;
import network.UserProfile.ProfilePicture;

/**
 * Maybe rework this into an abstract class situation
 */
public class Message implements Serializable {
  public static enum Type {
    LOGIN, START, RECONNECT, DISCONNECT, CHAT, MOVE, COMPLETE, DRAW, DRAW_REQUEST, RESIGN, RESIGN_REQUEST,
    RESIGN_RESPONSE, RETURN_TO_LOBBY, OPPONENT_RETURN_TO_LOBBY, OPPONENT_DISCONNECT, REMATCH, REMATCH_REQUEST,
    FETCH_LEADER_BOARD, LEADER_BOARD_DATA, JOIN_GAME, CANCEL_JOIN, GET_SERVER_STATUS, SERVER_STATUS, FETCH_PROFILE,
    PROFILE_DATA, PROFILE_PIC_UPDATE, FRIEND_REQUEST, FRIEND_REQUEST_RESPONSE, FETCH_FRIENDS, FRIEND_LIST_DATA,
    FRIEND_ONLINE_STATUS, GAME_INVITATION, GAME_INVITATION_RESPONSE;
  };

  public static enum WinType {
    WIN, LOSE, DRAW,
  };

  public static enum LeaderBoardView {
    TOP_TEN,
  };

  private Type type;
  private String username, password;
  private Integer column;
  private Integer numberPlayers;
  private Integer numberActiveGames;
  private String from;
  private Long playerID;
  private Integer winner;
  private String chatMessage;
  private Player player;
  private Player player2;
  private PlayerRole role;
  private boolean success;
  private ArrayList<Integer> restoredMoves;
  private WinType winType;
  private LeaderBoardView viewType;
  private ArrayList<LeaderBoardData> leaderBoardData;
  private ArrayList<UserProfile> friendList;
  private UserProfile profile;
  private ProfilePicture profilePicture;
  private Long befrienderID;
  private Long befriendedID;
  private Long invitorID;
  private Long invitedID;

  private Message() {
  }

  public static Message forSimpleInstruction(Type type) {
    Message toSend = new Message();

    toSend.type = type;

    return toSend;
  }

  public static Message forServerLoginAttempt(String username, String password) {
    Message toSend = new Message();

    toSend.type = Type.LOGIN;
    toSend.username = username;
    toSend.password = password;

    return toSend;
  }

  public static Message forServerLoginResponse(boolean success, String reason, Player player) {
    Message toSend = new Message();

    toSend.type = Type.LOGIN;
    toSend.success = success;
    toSend.chatMessage = reason;
    toSend.player = player;

    return toSend;
  }

  public static Message forServerReconnect(ArrayList<Integer> restoredMoves) {
    Message toSend = new Message();

    toSend.type = Type.RECONNECT;
    toSend.restoredMoves = restoredMoves;

    return toSend;
  }

  public static Message forServerDisconnect(Player player) {
    Message toSend = new Message();

    toSend.type = Type.DISCONNECT;
    toSend.player = player;

    return toSend;
  }

  public static Message forGameResponse(Type type, boolean accepted) {
    Message toSend = new Message();

    toSend.type = type;
    toSend.success = accepted;

    return toSend;
  }

  public static Message forGameStart(Player player1, Player player2, PlayerRole role) {
    Message toSend = new Message();

    toSend.type = Type.START;
    toSend.player = player1;
    toSend.player2 = player2;
    toSend.role = role;

    return toSend;
  }

  public static Message forGameComplete(Player player, WinType winType) {
    Message toSend = new Message();

    toSend.type = Type.COMPLETE;
    toSend.player = player;
    toSend.winType = winType;

    return toSend;
  }

  public static Message forOpponentDisconnect(Player player) {
    Message toSend = new Message();

    toSend.type = Type.OPPONENT_DISCONNECT;
    toSend.player = player;

    return toSend;
  }

  public static Message forChat(String username, String chatMessage, Long playerID) {
    Message toSend = new Message();

    toSend.type = Type.CHAT;
    toSend.username = username;
    toSend.chatMessage = chatMessage;
    toSend.playerID = playerID;

    return toSend;
  }

  public static Message forMove(String username, Integer column, Long playerID) {
    Message toSend = new Message();

    toSend.type = Type.MOVE;
    toSend.username = username;
    toSend.column = column;
    toSend.playerID = playerID;

    return toSend;
  }

  public static Message forReturnToLobbyRequest(Player player) {
    Message toSend = new Message();

    toSend.type = Type.RETURN_TO_LOBBY;
    toSend.player = player;

    return toSend;
  }

  public static Message forOpponentReturnToLobby(Player player) {
    Message toSend = new Message();

    toSend.type = Type.OPPONENT_RETURN_TO_LOBBY;
    toSend.player = player;

    return toSend;
  }

  public static Message forFetchLeaderboard(LeaderBoardView view) {
    Message toSend = new Message();

    toSend.type = Type.FETCH_LEADER_BOARD;
    toSend.viewType = view;

    return toSend;
  }

  public static Message forLeaderBoardData(LeaderBoardView view, ArrayList<LeaderBoardData> data) {
    Message toSend = new Message();
    toSend.type = Type.LEADER_BOARD_DATA;
    toSend.leaderBoardData = data;
    return toSend;
  }

  public static Message forServerInfo(int numberPlayer, int numberActiveGames) {
    Message toSend = new Message();
    toSend.type = Type.SERVER_STATUS;
    toSend.numberActiveGames = numberActiveGames;
    toSend.numberPlayers = numberPlayer;
    return toSend;
  }

  public static Message forFetchProfile(Long playerID) {
    Message toSend = new Message();
    toSend.type = Type.FETCH_PROFILE;
    toSend.playerID = playerID;
    return toSend;
  }

  public static Message forProfileData(UserProfile profile) {
    Message toSend = new Message();
    toSend.type = Type.PROFILE_DATA;
    toSend.profile = profile;
    return toSend;
  }

  public static Message forProfilePictureUpdate(Long playerID, ProfilePicture newPicture) {
    Message toSend = new Message();
    toSend.type = Type.PROFILE_PIC_UPDATE;
    toSend.playerID = playerID;
    toSend.profilePicture = newPicture;
    return toSend;
  }

  public static Message forFriendRequest(Long originPlayerID, Long targetPlayerID) {
    Message toSend = new Message();
    toSend.type = Type.FRIEND_REQUEST;
    toSend.befrienderID = originPlayerID;
    toSend.befriendedID = targetPlayerID;
    return toSend;
  }

  public static Message forFriendRequestReply(Long originPlayerID, Long targetPlayerID, boolean accepted) {
    Message toSend = new Message();
    toSend.type = Type.FRIEND_REQUEST_RESPONSE;
    toSend.success = accepted;
    toSend.befrienderID = originPlayerID;
    toSend.befriendedID = targetPlayerID;
    return toSend;
  }

  public static Message forFriendListData(ArrayList<UserProfile> friendList) {
    Message toSend = new Message();
    toSend.type = Type.FRIEND_LIST_DATA;
    toSend.friendList = friendList;
    return toSend;
  }

  public static Message forFriendOnlineStatus(String friendUserName, boolean isOnline) {
    Message toSend = new Message();
    toSend.type = Type.FRIEND_ONLINE_STATUS;
    toSend.success = isOnline;
    toSend.username = friendUserName;
    return toSend;
  }

  public static Message forGameInvitation(String invitorName, Long invitorID, Long invitedID) {
    Message toSend = new Message();
    toSend.type = Type.GAME_INVITATION;
    toSend.username = invitorName;
    toSend.invitedID = invitedID;
    toSend.invitorID = invitorID;
    return toSend;
  }

  public static Message forGameInvitationResponse(boolean accepted, Long invitorID, Long invitedID) {
    Message toSend = new Message();
    toSend.type = Type.GAME_INVITATION_RESPONSE;
    toSend.success = accepted;
    toSend.invitedID = invitedID;
    toSend.invitorID = invitorID;
    return toSend;
  }

  /**
   * getters
   */
  public Type getType() {
    return type;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public Integer getColumn() {
    return column;
  }

  public String getFrom() {
    return from;
  }

  public Long getPlayerID() {
    return playerID;
  }

  public Integer getWinner() {
    return winner;
  }

  public String getChatMessage() {
    return chatMessage;
  }

  public Player getPlayer() {
    return player;
  }

  public Player getPlayer2() {
    return player2;
  }

  public PlayerRole getRole() {
    return role;
  }

  public boolean isSuccess() {
    return success;
  }

  public ArrayList<Integer> getRestoredMoves() {
    return restoredMoves;
  }

  public ArrayList<LeaderBoardData> getLeaderBoardData() {
    return leaderBoardData;
  }

  public WinType getWinType() {
    return winType;
  }

  public LeaderBoardView getLeaderBoardViewType() {
    return viewType;
  }

  public Integer getNumPlayers() {
    return numberPlayers;
  }

  public Integer getNumActiveGames() {
    return numberActiveGames;
  }

  public UserProfile getProfile() {
    return profile;
  }

  public ProfilePicture getProfilePicture() {
    return profilePicture;
  }

  public Long getBefrienderID() {
    return befrienderID;
  }

  public Long getBefriendedID() {
    return befriendedID;
  }

  public ArrayList<UserProfile> getFriendsList() {
    return friendList;
  }

  public Long getInvitor() {
    return invitorID;
  }

  public Long getInvited() {
    return invitedID;
  }
}
