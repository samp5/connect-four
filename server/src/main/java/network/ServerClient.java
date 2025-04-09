package network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerClient {
  private final SocketChannel connection;
  private final int connectionID;

  public ServerClient(SocketChannel connection, int connectionID) {
    this.connection = connection;
    this.connectionID = connectionID;
  }

  public void sendMessage(Message message) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(byteStream);
    oos.writeObject(message);
    oos.flush();
    oos.close();

    ByteBuffer buf = ByteBuffer.wrap(byteStream.toByteArray());
    this.connection.write(buf);
  }

  public int getConnectionID() {
    return this.connectionID;
  }
}
