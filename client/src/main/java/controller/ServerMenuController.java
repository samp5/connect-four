package controller;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import network.UserProfile;
import network.UserProfile.ProfilePicture;
import utils.ToolTipHelper;
import utils.SceneManager.SceneSelections;
import controller.utils.FriendUtils;
import controller.utils.GameSettings;
import controller.utils.RecentConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;
import network.NetworkClient;
import network.PlayerData;
import utils.AudioManager;
import utils.CursorManager;
import utils.NotificationManager;
import utils.NotificationManager.NotificationType;
import utils.SceneManager;

/**
 * Bind to server_menu.fxml
 *
 */
public class ServerMenuController extends Controller {
  @FXML
  ImageView background;
  int grassState = 0;

  @FXML
  Pane menuPane;

  // MAIN BUTTONS
  @FXML
  Button disconnectButton;
  @FXML
  Button joinButton;
  @FXML
  Button settingsButton;
  @FXML
  BorderPane leaderBoardButton;

  // NOTIFICATIONS
  @FXML
  Pane notificationPane;
  @FXML
  TextFlow notificationText;
  @FXML
  ImageView notificationIcon;
  NotificationManager notificationManager;

  // PLAYER PROFILE
  // icon
  @FXML
  BorderPane profileButton;
  // profile popup
  @FXML
  Pane profilePane;
  @FXML
  Text profileUserName;
  @FXML
  Text profileGamesWon;
  @FXML
  Text profileGamesLost;
  @FXML
  Text profileGamesTied;
  @FXML
  Text profileWinPercent;
  @FXML
  Text profileELO;
  @FXML
  ImageView profilePicture;
  @FXML
  Button profileBackButton;
  @FXML
  ChoiceBox<ProfilePicture> profilePicSelector;

  // FRIENDS PANE
  @FXML
  Pane friendsPane;
  @FXML
  BorderPane friendsButton;
  @FXML
  Button friendsBackButton;
  @FXML
  VBox friendsList;

  // SERVER INFO
  @FXML
  Text serverName;
  @FXML
  Text nGames;
  @FXML
  Text nPlayers;
  @FXML
  Text portTxt;
  @FXML
  Text ipText;

  private static final String placeHolderText = "...";
  private static RecentConnection connection;
  private static ServerPlayerInfo playerInfo;
  private Timer timer;

  private static class ServerPlayerInfo {
    int online;
    int activeGames;

    ServerPlayerInfo(int online, int games) {
      this.online = online;
      this.activeGames = games;
    }
  }

  public void initialize() {

    NetworkClient.bindServerMenuController(this);
    NetworkClient.getServerInfo();

    setHandlers();
    setSelectorBehavior();
    animateGrass();

    // get friends list and profile
    populateProfileDisplay();
    populateFriendsList();

    // this data fetch is for the number of players online
    scheduleDataFetch();

    notificationManager = new NotificationManager(notificationPane, notificationText, notificationIcon);

    if (connection == null) {
      ipText.setText(placeHolderText);
      portTxt.setText(placeHolderText);
    } else {
      setServerInfo(connection);
    }

    if (playerInfo == null) {
      nGames.setText(placeHolderText);
      nPlayers.setText(placeHolderText);
    } else {
      setPlayerInfo(playerInfo.online, playerInfo.activeGames);
    }

    CursorManager.setHandCursor(profileButton, leaderBoardButton, friendsButton, joinButton, settingsButton,
        disconnectButton, profileBackButton, profilePicSelector, friendsBackButton);
    AudioManager.setAudioButton(profileButton, leaderBoardButton, friendsButton, joinButton, settingsButton,
        disconnectButton, profileBackButton, profilePicSelector, friendsBackButton);
  }

  private void animateGrass() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        grassState = (grassState + 1) % 2;
        try {
          Platform.runLater(() -> {
            background.setViewport(new Rectangle2D((3840 * grassState), 0, 3840, 2560));
          });
        } catch (Exception e) {
        }
      }
    }, 0, 1000);
  }

  private void setHandlers() {

    menuPane.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        profilePane.setVisible(false);
        friendsPane.setVisible(false);
        e.consume();
      }
    });

    joinButton.setOnAction(e -> {
      NetworkClient.joinGame();
      SceneManager.showScene(SceneSelections.LOADING);
    });
    joinButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        joinButton.getOnAction().handle(null);
    });

    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
    settingsButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        settingsButton.getOnAction().handle(null);
    });

    disconnectButton.setOnAction(e -> {
      NetworkClient.disconnect();
      SceneManager.showScene(SceneSelections.MAIN_MENU);
    });
    disconnectButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        disconnectButton.getOnAction().handle(null);
    });

    profileButton.setOnMouseClicked(e -> {
      profilePane.toFront();
      profilePane.setVisible(true);
      profilePane.requestFocus();
    });
    profileButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) 
        profileButton.getOnMouseClicked().handle(null);
    });
    profileBackButton.setOnAction(e -> {
      profilePane.setVisible(false);
    });

    leaderBoardButton.setOnMouseClicked(e -> {
      LeaderBoardController.loadOnto(menuPane);
    });
    leaderBoardButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        leaderBoardButton.getOnMouseClicked().handle(null);
    });

    friendsButton.setOnMouseClicked(e -> {
      friendsPane.toFront();
      friendsPane.setVisible(true);
      friendsPane.requestFocus();
    });
    friendsButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        friendsButton.getOnMouseClicked().handle(null);
    });
    friendsBackButton.setOnAction(e -> {
      friendsPane.setVisible(false);
    });

    Tooltip.install(leaderBoardButton, ToolTipHelper.make("View leaderboard"));
    Tooltip.install(friendsButton, ToolTipHelper.make("View friends"));
    Tooltip.install(profileButton, ToolTipHelper.make("Open Profile"));

    profilePicSelector.setOnAction(e -> {
      ProfilePicture newProfilePic = profilePicSelector.getSelectionModel().getSelectedItem();
      if (newProfilePic != null) {
        profilePicture.setImage(new Image(newProfilePic.getAssetFileName()));
        ((ImageView) profileButton.getCenter()).setImage(new Image(newProfilePic.getAssetFileName()));
        PlayerData.updateProfilePicture(newProfilePic);
      }
    });
    profilePicSelector.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        profilePicSelector.show();
        e.consume();
      } else if (profilePicSelector.isShowing()) {
        SingleSelectionModel<ProfilePicture> selector = profilePicSelector.getSelectionModel();

        if (e.getCode() == KeyCode.J) 
          selector.selectNext();
        else if (e.getCode() == KeyCode.K)
          selector.selectPrevious();

        profilePicSelector.show();
        e.consume();
      }
    });
  }

  public void setServerInfo(RecentConnection conn) {
    connection = conn;
    setServerInfo(conn.getName(), conn.getIp(), conn.getPort());
  }

  private void setServerInfo(String name, String IP, int Port) {
    serverName.setText(name);
    ipText.setText(IP);
    portTxt.setText(String.valueOf(Port));
  }

  public void setPlayerInfo(int numPlayers, int numGames) {
    playerInfo = new ServerPlayerInfo(numPlayers, numGames);
    nPlayers.setText(String.valueOf(numPlayers));
    nGames.setText(String.valueOf(numGames));
  }

  private void scheduleDataFetch() {
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        NetworkClient.getServerInfo();
      }
    }, 5000, 5000);
  }

  private void populateProfileDisplay() {
    PlayerData.getProfile(() -> populateProfileDisplay()).ifPresent(profile -> {
      profilePicSelector.getSelectionModel().select(profile.getProfilePicture());
      profileUserName.setText(profile.getUserName());
      profileGamesWon.setText(String.valueOf(profile.getGamesWon()));
      profileGamesLost.setText(String.valueOf(profile.getGamesLost()));
      profileGamesTied.setText(String.valueOf(profile.getGamesTied()));
      profileWinPercent.setText(
          String.format("%d", (int) ((float) profile.getGamesWon() / (float) profile.getGamesPlayed() * 100)) + "%");
      profileELO.setText(String.valueOf((int) profile.getElo()));
    });
  }

  private void setSelectorBehavior() {

    // setup choice box
    profilePicSelector
        .setItems(FXCollections.observableArrayList(ProfilePicture.values())
            .filtered(o -> o != ProfilePicture.UAQ));
    profilePicSelector.setConverter(new StringConverter<UserProfile.ProfilePicture>() {
      public String toString(ProfilePicture object) {
        if (object != null) {
          return object.toDisplayString();
        } else {
          return "null";
        }
      }

      @Override
      public ProfilePicture fromString(String string) {
        throw new UnsupportedOperationException("Unimplemented method 'fromString'");
      }

    });
  }

  private void populateFriendsList() {
    PlayerData.getFriends(() -> populateFriendsList()).ifPresent(frds -> {
      friendsList.getChildren()
          .setAll(frds.stream().map(up -> FriendUtils.createComponent(up)).collect(Collectors.toList()));
    });
  }

  public void recieveNotification(String msg, NotificationType type) {
    notificationManager.recieve(msg, type);
  }

  public void recievePrompt(String msg, NotificationType type, EventHandler<ActionEvent> onAccept,
      EventHandler<ActionEvent> onDeny) {
    notificationManager.recievePrompt(msg, type, onAccept, onDeny);
  }

  public void reenableInvite(String username) {
    FriendUtils.reenableInvite(friendsList, username);
  }

  public void updateFriendOnlineView(String username, boolean isOnline) {
    PlayerData.getFriends(() -> updateFriendOnlineView(username, isOnline)).ifPresent(frds -> {
      frds.stream().filter(up -> up.getUserName().equals(username)).forEach(up -> {
        FriendUtils.update(friendsList, up);
      });
    });
  }
}
