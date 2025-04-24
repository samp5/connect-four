package controller;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
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
import logic.AI;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import network.NetworkClient;
import network.UserProfile;
import utils.AudioManager;
import utils.CursorManager;
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
 *
 * Bind to Chat.fxml
 */
public class ChatController extends Controller {

  @FXML
  Button sendButton;
  @FXML
  Button resignButton;
  @FXML
  Button drawButton;
  @FXML
  Button requestResignButton;

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

  // Opp profile
  @FXML
  BorderPane oppProfileButton;
  @FXML
  Text userNameText;
  @FXML
  Pane oppProfilePane;
  @FXML
  Button oppProfileBackButton;
  @FXML
  Text oppUsername;
  @FXML
  Text oppElo;
  @FXML
  Text oppWinPercent;
  @FXML
  Button addFriend;
  Timer profileTimer;
  int aiProfileState = 0;

  @FXML
  Pane notificationPane;
  @FXML
  TextFlow notificationText;
  @FXML
  ImageView notificationIcon;

  private NotificationManager notificationManager;

  public void initialize() {
    NetworkClient.bindChatController(this);
    setHandlers();

    if (GameLogic.getGameMode() == GameMode.LocalAI) {
      profileTimer = new Timer();
      oppProfileButton.setVisible(true);
      userNameText.setText(AI.getName());
      oppProfileButton.setOnMouseClicked(e -> {
        recieveMessage("ow stop :cry:", AI.getName(), false);
      });

      checkAIMaxMode();
    }

    // auto scroll the chat history based on the height of the vbox
    chatHistoryScroll.vvalueProperty().bind(chatHistory.heightProperty());
    notificationManager = new NotificationManager(notificationPane, notificationText, notificationIcon);

    CursorManager.setHandCursor(resignButton, drawButton, requestResignButton, sendButton, oppProfileButton,
        oppProfileBackButton, popupConfirmButton, popupCancelButton);
    AudioManager.setAudioButton(resignButton, drawButton, requestResignButton, sendButton, oppProfileButton,
        oppProfileBackButton, popupConfirmButton, popupCancelButton);

    // can't do this in fxml easily
    drawButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/draw.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(34, 28, false, false, false, false))));
    requestResignButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/red-flag.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(33, 28, false, false, false, false))));
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

    resignButton.setOnAction(e -> {
      confirmPopup.setVisible(true);
      confirmText.setText("Are you sure you want to resign?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.setVisible(false);
        NetworkClient.resign();
      });
    });

    resignButton.setTooltip(ToolTipHelper.make("Resign"));

    drawButton.setOnAction(e -> {
      confirmPopup.setVisible(true);
      confirmText.setText("Are you sure you want to request a draw?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.setVisible(false);
        NetworkClient.drawRequest();
      });
    });
    drawButton.setTooltip(ToolTipHelper.make("Request a draw"));

    requestResignButton.setOnAction(e -> {
      confirmPopup.setVisible(true);
      confirmText.setText("You want to suggest the opponent resigns?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.setVisible(false);
        NetworkClient.resignRequest();
      });
    });
    requestResignButton.setTooltip(ToolTipHelper.make("Request your opponent resigns"));

    oppProfileButton.setOnMouseClicked(e -> {
      oppProfilePane.setVisible(true);
    });
    oppProfileBackButton.setOnAction(e -> {
      oppProfilePane.setVisible(false);
    });
    addFriend.setOnAction(e -> {
      NetworkClient.sendOpponentFriendRequest();
    });
  }

  public void opponentDisconnect() {
    notificationManager.recieve("Your opponent has disconnected",
        NotificationType.CONNECTION_ERROR);

  }

  public void rematchRequest() {
    notificationManager.recieve("You opponent wants a rematch",
        NotificationType.INFORMATION);
  }

  public void recieveResign() {
    notificationManager.recieve("Your opponent has resigned.", NotificationType.INFORMATION);
  }

  public void recieveFriendRequest() {
    confirmPopup.setVisible(true);
    confirmText.setText(GameLogic.getRemotePlayer().getUsername() + " sent you a friend request");
    popupConfirmButton.setOnAction(e -> {
      confirmPopup.setVisible(false);
      addFriend.setText("Already Friends");
      addFriend.setDisable(true);
      NetworkClient.replyFriendRequest(true);
    });
    EventHandler<ActionEvent> oldHandler = popupCancelButton.getOnAction();
    popupCancelButton.setOnAction(e -> {
      NetworkClient.replyFriendRequest(false);
      confirmPopup.setVisible(false);
      popupCancelButton.setOnAction(oldHandler);
    });
  }

  public void recieveFriendRequestResponse(boolean accepted) {
    if (accepted) {
      notificationManager.recieve("You opponent accepted your friend request", NotificationType.FRIEND_GOOD);
      addFriend.setText("Already Friends");
    } else {
      notificationManager.recieve("You opponent denied your friend request", NotificationType.FRIEND_BAD);
    }
    addFriend.setDisable(true);
  }

  public void resignAccepted() {
    notificationManager.recieve("Your opponent has accepted your resign request.",
        NotificationType.INFORMATION);
  }

  public void resignDeclined() {
    notificationManager.recieve("Your opponent has declined your resign request.",
        NotificationType.INFORMATION);
  }

  public void drawAccepted() {
    notificationManager.recieve("Your opponent has accepted your draw offer.",
        NotificationType.INFORMATION);
  }

  public void drawDeclined() {
    notificationManager.recieve("Your opponent has declined your draw offer.", NotificationType.INFORMATION);
  }

  public void recieveOpponentProfile(UserProfile profile) {
    oppProfileButton.setVisible(true);
    ((ImageView) (oppProfileButton.getCenter())).setImage(new Image(profile.getProfilePicture().getAssetFileName()));
    if (profile.getFriends().contains(GameLogic.getLocalPlayer().getID())) {
      addFriend.setText("Already Friends");
      addFriend.setDisable(true);
    }
    populateOpponentProfile(profile);
  }

  private void populateOpponentProfile(UserProfile profile) {
    oppElo.setText(String.valueOf((int) profile.getElo()));
    oppUsername.setText(profile.getUserName());
    userNameText.setText(profile.getUserName());
    oppWinPercent.setText(
        String.format("%d", (int) ((float) profile.getGamesWon() / (float) profile.getGamesPlayed() * 100)) + "%");
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

  public void fetchOpponentProfiles() {
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      NetworkClient.fetchOpponentProfile();
    }
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

  public void checkAIMaxMode() {
    if (AI.getDifficulty() == 7) {
      setAIMaxMode();
    } else {
      setAIDefaultMode();
    }
  }

  private void setAIMaxMode() {
    ((ImageView) oppProfileButton.getCenter()).setImage(new Image("/assets/robot_max.png", 150, 75, false, false));
    ((ImageView) oppProfileButton.getCenter()).setViewport(new Rectangle2D(0, 0, 75, 75));
    profileTimer.cancel();
    profileTimer = new Timer();

    profileTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        aiProfileState = (aiProfileState + 1) % 2;
        try {
          Platform.runLater(() -> {
            ((ImageView) oppProfileButton.getCenter())
                .setViewport(new Rectangle2D((75 * aiProfileState), 0, 75, 75));
          });
        } catch (Exception e) {
        }
      }
    }, 0, 1000);
  }

  private void setAIDefaultMode() {
    profileTimer.cancel();
    ((ImageView) oppProfileButton.getCenter()).setImage(new Image("/assets/robot.png"));
    ((ImageView) oppProfileButton.getCenter()).setViewport(null);
  }
}
