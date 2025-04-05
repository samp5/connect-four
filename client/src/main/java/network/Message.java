package network;

/**
 * Maybe rework this into an abstract class situation
 */
public class Message {
  private String type;
  private Integer player;
  private Integer column;
  private String from;
  private Integer playerID;
  private Integer winner;
  private String chatMessage;



  /**
   * For chat messages
   */
  public Message(String type, Integer player, String from, String chatMessage, Integer playerID) {
    this.type = type;
    this.player = player;
    this.from = from;
    this.chatMessage = chatMessage;
    this.playerID = playerID;
  }

  /**
   * For move messages
   */
  public Message(String type, Integer player, Integer column, Integer playerID) {
    this.type = type;
    this.player = player;
    this.column = column;
    this.playerID = playerID;
  }

  /**
   * autogen by LSP
   */
  public String getType() {
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
