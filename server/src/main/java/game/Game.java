package game;

import java.io.IOException;

import network.Message;
import network.ServerClient;
import network.Message.Type;

public class Game {
  ServerClient player1, player2;

  public Game(ServerClient p1, ServerClient p2) {
    this.player1 = p1;
    this.player2 = p2;
  }

  public void begin() {
    try {
      this.player1.sendMessage(new Message(Type.START));
      this.player2.sendMessage(new Message(Type.START));
    } catch (IOException e) {}
  }
}
