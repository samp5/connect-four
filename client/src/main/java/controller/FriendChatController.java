package controller;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import network.NetworkClient;
import network.UserProfile;
import network.PlayerData;
import utils.AudioManager;
import utils.ToolTipHelper;
import utils.AudioManager.SoundEffect;
import utils.NotificationManager;
import utils.NotificationManager.NotificationType;
import controller.utils.ChatMessage;
import controller.utils.Markup;

/**
 * Control the UI of the chat window
 * Send chat messages
 * Display messages from this client + others
 */
public class FriendChatController extends Controller {

  @FXML
  Button sendButton;

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
  Long friendID;

  // Opp profile
  @FXML
  BorderPane oppProfileButton;
  @FXML
  Pane oppProfilePane;
  @FXML
  Button oppProfileBackButton;
  @FXML
  Text usernameText;
  @FXML
  Text oppUsername;
  @FXML
  Text oppElo;
  @FXML
  Text oppWinPercent;

  @FXML
  Pane notificationPane;
  @FXML
  TextFlow notificationText;
  @FXML
  ImageView notificationIcon;

  private NotificationManager notificationManager;

  public void close() {
    System.out.println("closing scene");
    ((Stage) chatPane.getScene().getWindow()).close();
  }

  public void friendOnlineStatus(boolean isOnline) {
    if (isOnline) {
      recieveNotification(oppUsername.getText() + " is back online!", NotificationType.INFORMATION);
      sendButton.setDisable(false);
      chatEditorInput.setOnKeyPressed(e -> {
        if (e.getCode() == KeyCode.ENTER) {
          sendMessage();
        }
        e.consume();
      });
    } else {
      recieveNotification(oppUsername.getText() + " is now offline", NotificationType.INFORMATION);
      sendButton.setDisable(true);
      chatEditorInput.setOnKeyPressed(e -> e.consume());
    }
  }

  public void initialize() {
    setHandlers();
    oppProfileButton.setVisible(true);

    // auto scroll the chat history based on the height of the vbox
    chatHistoryScroll.vvalueProperty().bind(chatHistory.heightProperty());
    notificationManager = new NotificationManager(notificationPane, notificationText, notificationIcon);

    // can't do this in fxml easily
    sendButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/send_button.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(100, 50, false, false, false, false))));
  }

  public void setFriendID(Long friendID) {
    this.friendID = friendID;
    populateFriendProfile();
  }

  private void setHandlers() {
    sendButton.setOnAction(e -> {
      sendMessage();
    });
    sendButton.setTooltip(ToolTipHelper.make("Send message"));

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

    oppProfileButton.setOnMouseClicked(e -> {
      oppProfilePane.setVisible(true);
    });
    oppProfileBackButton.setOnAction(e -> {
      oppProfilePane.setVisible(false);
    });
  }

  public void opponentDisconnect() {
    notificationManager.recieve("Your opponent has disconnected",
        NotificationType.CONNECTION_ERROR);
  }

  private void populateFriendProfile() {
    PlayerData.getFriends(() -> populateFriendProfile()).ifPresent(friends -> {
      friends.stream().filter(up -> up.getId().equals(friendID)).forEach(profile -> {
        ((ImageView) oppProfileButton.getCenter()).setImage(new Image(profile.getProfilePicture().getAssetFileName()));
        oppElo.setText(String.valueOf((int) profile.getElo()));
        oppUsername.setText(profile.getUserName());
        usernameText.setText(profile.getUserName());
        oppWinPercent.setText(
            String.format("%d", (int) ((float) profile.getGamesWon() / (float) profile.getGamesPlayed() * 100)) + "%");
      });
    });
  }

  private void sendMessage() {
    AudioManager.playSoundEffect(SoundEffect.CHAT_SENT);
    String msg = chatEditorInput.getText().trim();
    if (msg.length() == 0) {
      return;
    }
    PlayerData.getProfile(() -> sendMessage()).ifPresent(up -> {
      appendMessage(msg, up.getUserName(), true);
      NetworkClient.sendChatMessageTo(friendID, msg);
      chatEditorInput.clear();
      chatEditorDisplay.getChildren().setAll();
    });
  }

  public void recieveMessage(String message, String username, boolean local) {
    AudioManager.playSoundEffect(SoundEffect.CHAT_RECIEVED);
    appendMessage(message, username, local);
  }

  public void fetchOpponentProfiles() {
    NetworkClient.fetchOpponentProfile();
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

  public void recieveNotification(String msg, NotificationType type) {
    notificationManager.recieve(msg, type);
  }

}
