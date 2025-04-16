package controller;

import java.io.IOException;

import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import network.NetworkClient;
import utils.AudioManager;
import utils.AudioManager.SoundEffect;
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
  Button ffButton;
  @FXML
  Button drawButton;
  @FXML
  Button requestFFButton;

  @FXML
  Pane confirmPopup;
  @FXML
  Button popupCancelButton;
  @FXML
  Button popupConfirmButton;
  @FXML
  Text confirmText;

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

  @FXML
  Pane offerRejected;
  @FXML
  Text offerRejectedText;

  public void initialize() {
    NetworkClient.bindChatController(this);
    setHandlers();

    // auto scroll the chat history based on the height of the vbox
    chatHistoryScroll.vvalueProperty().bind(chatHistory.heightProperty());

    // can't do this in fxml easily
    sendButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/send_button.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(100, 50, false, false, false, false))));
    ffButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/surrender-flag.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(33, 28, false, false, false, false))));
    drawButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/draw.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(34, 28, false, false, false, false))));
    requestFFButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/red-flag.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(33, 28, false, false, false, false))));
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

    popupCancelButton.setOnAction(e -> {
      confirmPopup.setVisible(false);
    });

    ffButton.setOnAction(e -> {
      confirmPopup.setVisible(true);
      confirmText.setText("Are you sure you want to resign?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.setVisible(false);
        NetworkClient.forfeit();
      });
    });

    drawButton.setOnAction(e -> {
      confirmPopup.setVisible(true);
      confirmText.setText("Are you sure you want to request a draw?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.setVisible(false);
        NetworkClient.drawRequest();
      });
    });

    requestFFButton.setOnAction(e -> {
      confirmPopup.setVisible(true);
      confirmText.setText("Are you sure you want to request that the opponent resigns?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.setVisible(false);
        NetworkClient.resignRequest();
      });
    });
  }

  public void recieveForfeit() {
    getNotification("Your opponent has resigned.");
  }

  public void draw() {
    getNotification("Your opponent has accepted your draw offer.");
  }

  public void opponentDisconnect() {
    getNotification("Your opponent just disconnected");
  }

  public void drawDeclined() {
    getNotification("Your opponent has declined your draw offer.");
  }

  public void resignAccepted() {
    getNotification("Your opponent has accepted your resign request.");
  }

  public void resignDeclined() {
    getNotification("Your opponent has declined your resign request.");
  }

  private void getNotification(String text) {
    offerRejectedText.setText(text);
    Path extendPath = new Path(new MoveTo(480, -280), new HLineTo(160));
    PathTransition animation = new PathTransition(Duration.seconds(.5), extendPath, offerRejected);
    animation.play();
    offerRejected.setVisible(true);

    Path hidePath = new Path(new MoveTo(160, -280), new HLineTo(480));
    PathTransition reverse = new PathTransition(Duration.seconds(.5), hidePath, offerRejected);

    PauseTransition delay = new PauseTransition(Duration.seconds(4));

    animation.setOnFinished(e -> {
      delay.play();
    });
    delay.setOnFinished(e -> {
      reverse.play();
    });
    reverse.setOnFinished(e -> {
      offerRejected.setVisible(false);
    });
  }

  private void sendMessage() {
    AudioManager.playSoundEffect(SoundEffect.CHAT_SENT);
    String msg = chatEditorInput.getText().trim();
    if (msg.length() == 0) {
      return;
    }
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      appendMessage(msg, GameLogic.getLocalPlayer().getUsername(), true);
      NetworkClient.sendChatMessage(msg);
    } else {
      appendMessage(msg, "you", true);
    }
    chatEditorInput.clear();
    chatEditorDisplay.getChildren().setAll();
  }

  public void recieveMessage(String message, String username, boolean local) {
    AudioManager.playSoundEffect(SoundEffect.CHAT_RECIEVED);
    appendMessage(message, username, local);
  }

  // // when we need to display a new message
  public void appendMessage(String msg, String username, boolean local) {
    try {
      FXMLLoader loader = new FXMLLoader(ChatController.class.getResource("/fxml/chatMessage.fxml"));
      Region msgBox = loader.load();
      ChatMessage newMessageCTL = loader.getController();
      newMessageCTL.build(username, 0, msg, local);
      this.chatHistory.getChildren().add(msgBox);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
