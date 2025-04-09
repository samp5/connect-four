package network;

import java.io.Serializable;

/**
 * Maybe rework this into an abstract class situation
 */
public class Message implements Serializable {
  public static enum Type {
    REG,
    CHAT,
    MOVE,
  };

  private Type type;
  private Integer player;
  private Integer column;
  private String from;
  private Integer playerID;
  private Integer winner;
  private String chatMessage;


  /**
   * For game registration messages
   */
  public Message(Integer playerID) {
    this.type = Type.REG;
    this.playerID = playerID;
  }

  /**
   * For chat messages
   */
  public Message(Integer player, String from, String chatMessage, Integer playerID) {
    this.type = Type.CHAT;
    this.player = player;
    this.from = from;
    this.chatMessage = chatMessage;
    this.playerID = playerID;
  }

  /**
   * For move messages
   */
  public Message(Integer player, Integer column, Integer playerID) {
    this.type = Type.MOVE;
    this.player = player;
    this.column = column;
    this.playerID = playerID;
  }

  /**
   * autogen by LSP
   */
  public Type getType() {
    return type;
  }

  public Integer getPlayer() {
    return player;
  }

  public Integer getColumns() {
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
}
