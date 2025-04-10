package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class ServerClient {
  private final SocketChannel connection;
  private Player player;

  public ServerClient(SocketChannel connection) {
    this.connection = connection;
  }

  public void sendMessage(Message message) throws IOException {
    this.connection.write(message.asByteBuffer());
  }

  public ArrayList<Message> getMessages() {
    ArrayList<Message> messages = new ArrayList<>();
    try {
      InputStream inputStream = this.connection.socket().getInputStream();
      int sz = inputStream.available();
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
