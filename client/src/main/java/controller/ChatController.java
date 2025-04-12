package controller;

import java.io.IOException;
import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import logic.GameLogic;
import network.NetworkClient;
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
  ScrollPane chatHistoryScroll;
  @FXML
  BorderPane chatPane;

  public void initialize() {
    NetworkClient.bindChatController(this);
    System.out.println("Chat controller initialized");
    setHandlers();
    chatHistory.getStyleClass().add("transparent-bkgd");
    chatEditorInput.setOpacity(0);
    chatEditorInput.toBack();
    chatEditorDisplay.toFront();
    chatHistoryScroll.vvalueProperty().bind(chatHistory.heightProperty());
    System.out.println(chatHistoryScroll.getStyleClass());
    chatPane.setBackground(new Background(
        new BackgroundImage(new Image("/assets/chat_background.png", 360, 720, false, true, false),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(360, 720, false, false, false, false))));

    sendButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/send_button.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(100, 50, false, false, false, false))));
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

          chatEditorDisplay.getChildren()
              .setAll(Markup.markup(newValue));
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
    String msg = chatEditorInput.getText().trim();
    appendMessage(msg, GameLogic.getLocalPlayer().getUsername());
    chatEditorInput.clear();
    chatEditorDisplay.getChildren().setAll();
    NetworkClient.sendChatMessage(msg);
  }

  public void recieveMessage(String message, String username) {
    appendMessage(message, username);
  }

  // // when we need to display a new message
  public void appendMessage(String msg, String username) {
    try {
      FXMLLoader loader =
          new FXMLLoader(ChatController.class.getResource("/fxml/chatMessage.fxml"));
      Region msgBox = loader.load();
      ChatMessage newMessageCTL = loader.getController();
      newMessageCTL.build(username, 0, msg, GameLogic.getLocalPlayer().getUsername() == username);
      this.chatHistory.getChildren().add(msgBox);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
