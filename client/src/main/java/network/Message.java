package network;

import java.io.Serializable;

/**
 * Maybe rework this into an abstract class situation
 */
public class Message implements Serializable {
  public static enum Type {
    REG, CHAT, MOVE,
  };

  private Type type;
  private String username;
  private Integer column;
  private String from;
  private Integer playerID;
  private Integer winner;
  private String chatMessage;
  private Player player;


  /**
   * For game registration messages
   */
  public Message(Player player) {
    this.type = Type.REG;
    this.player = player;
    this.playerID = player.getID();
  }

  /**
   * For chat messages
   */
  public Message(String username, String chatMessage, Integer playerID) {
    this.type = Type.CHAT;
    this.username = username;
    this.chatMessage = chatMessage;
    this.playerID = playerID;
  }

  /**
   * For move messages
   */
  public Message(String username, Integer column, Integer playerID) {
    this.type = Type.MOVE;
    this.username = username;
    this.column = column;
    this.playerID = playerID;
  }

  /**
   * autogen by LSP
   */
  public Type getType() {
    return type;
  }

  public String getUsername() {
    return username;
  }

  public Integer getColumn() {
    return column;
  }

  public String getFrom() {
    return from;
  }

  public Integer getPlayerID() {
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
}
