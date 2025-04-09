package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import network.NetworkClient;
import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.util.TextUtils;

import controller.utils.ChatMessage;
import controller.utils.Markup;

/**
 * Control the UI of the chat window
 * Send chat messages
 * Display messages from this client + others
 *
 * Bind to Chat.fxml
 */
public class ChatController {

  @FXML
  Button sendButton;

  @FXML
  HBox chatEditorBox;
  @FXML
  TextArea chatEditorInput;
  @FXML
  TextFlow chatEditorDisplay;
  @FXML
  VBox chatHistory;

  @FXML
  // something like this
  private Collection<String> message;

  public void initialize() {
    NetworkClient.bindChatController(this);
    System.out.println("Chat controller initialized");
    setHandlers();
    chatEditorInput.setOpacity(0);
    chatEditorInput.toBack();
    chatEditorDisplay.toFront();
  }

  private void setHandlers() {
    sendButton.setOnAction(e -> {
      sendMessage();
    });

    chatEditorInput.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          chatEditorDisplay.getChildren().setAll(Markup.markup(newValue, chatEditorInput.getFont()));
        }
      }
    });

    chatEditorInput.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        sendMessage();
      }
      e.consume();
    });
  }

  private void sendMessage() {
    appendMessage(chatEditorInput.getText().trim());
    chatEditorInput.clear();
    chatEditorDisplay.getChildren().setAll();
    // TODO:
    // Something with NetworkClient
  }

  // // when we need to display a new message
  public void appendMessage(String msg) {
    try {
      FXMLLoader loader = new FXMLLoader(ChatController.class.getResource("/fxml/chatMessage.fxml"));
      Region msgBox = loader.load();
      ChatMessage msgCTL = loader.getController();
      msgCTL.set("player name", 0, msg, chatEditorInput.getFont(), true);
      this.chatHistory.getChildren().add(msgBox);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
