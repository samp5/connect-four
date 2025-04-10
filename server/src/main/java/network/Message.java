package network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Maybe rework this into an abstract class situation
 */
public class Message implements Serializable {
  public static enum Type {
    LOGIN,
    START,
    DISCONNECT,
    CHAT,
    MOVE,
  };

  private Type type;
  private String username, password;
  private Integer column;
  private String from;
  private Long playerID;
  private Integer winner;
  private String chatMessage;
  private Player player;
  private boolean success;


  /**
   * For sending simple instructions
   */
  public Message(Type type) {
    this.type = type;
  }

  /**
   * For login with server attempts
   */
  public Message(String username, String password) {
    this.type = Type.LOGIN;
    this.username = username;
    this.password = password;
  }
  /**
   * For server login responses
   */
  public Message(boolean success, String reason, Player player) {
    this.type = Type.LOGIN;
    this.success = success;
    this.chatMessage = reason;
    this.player = player;
  }
  /**
   * For server disconnect messages
   */
  public Message(Player player) {
    this.type = Type.DISCONNECT;
    this.player = player;
  }

  /**
   * For chat messages
   */
  public Message(String username, String chatMessage, Long playerID) {
    this.type = Type.CHAT;
    this.username = username;
    this.chatMessage = chatMessage;
    this.playerID = playerID;
  }

  /**
   * For move messages
   */
  public Message(String username, Integer column, Long playerID) {
    this.type = Type.MOVE;
    this.username = username;
    this.column = column;
    this.playerID = playerID;
  }

  /**
   * Returns the message as a {@code ByteBuffer} representation, for sending
   * over the network.
   */
  public ByteBuffer asByteBuffer() throws IOException {
    return ByteBuffer.wrap(this.asBytes());
  }

  /**
   * Returns the message as a byte array representation, for sending over the 
   * network.
   */
  public byte[] asBytes() throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(byteStream);
    oos.writeObject(this);
    oos.flush();
    oos.close();

    return byteStream.toByteArray();
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

  public boolean isSuccess() {
    return success;
  }
}
