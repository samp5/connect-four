package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class ServerClient {
  private final SocketChannel connection;
  private final int connectionID;

  public ServerClient(SocketChannel connection, int connectionID) {
    this.connection = connection;
    this.connectionID = connectionID;
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
      System.out.println("error on getting stream");
    } catch (ClassNotFoundException e) {
      System.out.println("error parsing message");
    }
    return messages;
  }

  public int getConnectionID() {
    return this.connectionID;
  }
}
