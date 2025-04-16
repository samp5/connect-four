package network;

import java.io.Serializable;
import java.util.ArrayList;

import network.Player.PlayerRole;

/**
 * Maybe rework this into an abstract class situation
 */
public class Message implements Serializable {
  public static enum Type {
    LOGIN,
    START,
    RECONNECT,
    DISCONNECT,
    CHAT,
    MOVE,
    COMPLETE,
    FORFEIT,
    DRAW,
    DRAW_REQUEST,
  };

  public static enum WinType {
    WIN,
    LOSE,
    DRAW,
  };

  private Type type;
  private String username, password;
  private Integer column;
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

  private Message() {}

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

  public WinType getWinType() {
    return winType;
  }
}
