package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * An instance of a connection to a client, based on the server
 */
public class ServerClient {
  private final SocketChannel connection;
  private Player player;  // the player corresponding to the connection

  public ServerClient(SocketChannel connection) {
    this.connection = connection;
  }

  /**
   * Send a message to the connection
   */
  public void sendMessage(Message message) throws IOException {
    ObjectOutputStream out = new ObjectOutputStream(connection.socket().getOutputStream());
    out.writeObject(message);
  }

  /**
   * Get all messages from this connection.
   * Non-Blocking.
   */
  public ArrayList<Message> getMessages() {
    ArrayList<Message> messages = new ArrayList<>();
    try {
      // see if data is available
      InputStream inputStream = this.connection.socket().getInputStream();
      int sz = inputStream.available();

      // if there is, convert to a message
      if (sz > 0) {
        ObjectInputStream in = new ObjectInputStream(inputStream);
        Message message = (Message) in.readObject();
        messages.add(message);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("error on getting stream");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.out.println("error parsing message");
    }

    return messages;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public Player getPlayer() {
    return this.player;
  }
}
