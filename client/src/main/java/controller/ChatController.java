package controller;

import java.util.Collection;
import javafx.fxml.FXML;
import network.NetworkClient;

/**
 * Control the UI of the chat window
 * Send chat messages
 * Display messages from this client + others
 *
 * Bind to Chat.fxml
 */
public class ChatController {

  // something like this
  private Collection<String> message;

  public void initialize() {
    NetworkClient.bindChatController(this);
    System.out.println("Chat controller initialized");
  }

  // when the send button clicked maybe?
  @FXML
  public void onSend() {
  }

  // when we need to display a new message
  public void appendMessage(String msg) {
  }
}
