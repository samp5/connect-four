package controller;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import network.UserProfile;
import network.UserProfile.ProfilePicture;
import utils.ToolTipHelper;
import utils.SceneManager.SceneSelections;
import controller.utils.FriendUtils;
import controller.utils.GameSettings;
import controller.utils.RecentConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;
import network.NetworkClient;
import utils.NotificationManager;
import utils.NotificationManager.NotificationType;
import utils.SceneManager;

/**
 * Bind to server_menu.fxml
 *
 */
public class ServerMenuController extends Controller {
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
  private static UserProfile profile = null;

  // FRIENDS PANE
  @FXML
  Pane friendsPane;
  @FXML
  BorderPane friendsButton;
  @FXML
  Button friendsBackButton;
  @FXML
  VBox friendsList;
  private static ArrayList<UserProfile> friends;

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
  private boolean profileVisible;

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

    initProfile();
    scheduleDataFetch();
    requestFriendsList();

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
    setHandlers();
    fillFriendsList();
  }

  private void setHandlers() {

    joinButton.setOnAction(e -> {
      NetworkClient.joinGame();
      SceneManager.showScene(SceneSelections.LOADING);
    });
    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
    leaderBoardButton.setOnMouseClicked(e -> {
      LeaderBoardController.loadOnto(menuPane);
    });
    disconnectButton.setOnAction(e -> {
      NetworkClient.disconnect();
      SceneManager.showScene(SceneSelections.MAIN_MENU);
    });
    profileBackButton.setOnAction(e -> {
      profilePane.setVisible(false);
    });
    profileButton.setOnMouseClicked(e -> {
      profilePane.toFront();
      profilePane.setVisible(true);
    });

    friendsBackButton.setOnAction(e -> {
      friendsPane.setVisible(false);
    });

    friendsButton.setOnMouseClicked(e -> {
      friendsPane.toFront();
      friendsPane.setVisible(true);
    });

    Tooltip.install(leaderBoardButton, ToolTipHelper.make("View leaderboard"));
    Tooltip.install(friendsButton, ToolTipHelper.make("View friends"));
    Tooltip.install(profileButton, ToolTipHelper.make("Open Profile"));

    profilePicSelector.setOnAction(e -> {
      ProfilePicture newProfilePic = profilePicSelector.getSelectionModel().getSelectedItem();
      if (newProfilePic != null) {
        profilePicture.setImage(new Image(newProfilePic.getAssetFileName()));
        ((ImageView) profileButton.getCenter()).setImage(new Image(newProfilePic.getAssetFileName()));
        profile.setProfilePicture(newProfilePic);
        NetworkClient.updateProfilePicture(newProfilePic);
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

  public void recieveProfileData(UserProfile profile) {
    ServerMenuController.profile = profile;
    profilePicSelector.getSelectionModel().select(profile.getProfilePicture());
    updateProfileDisplay();
  }

  private void updateProfileDisplay() {

    profileUserName.setText(profile.getUserName());
    profileGamesWon.setText(String.valueOf(profile.getGamesWon()));
    profileGamesLost.setText(String.valueOf(profile.getGamesLost()));
    profileGamesTied.setText(String.valueOf(profile.getGamesTied()));
    profileWinPercent.setText(
        String.format("%d", (int) ((float) profile.getGamesWon() / (float) profile.getGamesPlayed() * 100)) + "%");
    profileELO.setText(String.valueOf((int) profile.getElo()));
  }

  private void initProfile() {
    // get profile data + populate profile
    if (profile == null) {
      NetworkClient.fetchProfile();
    }

    // setup choice box
    profilePicSelector
        .setItems(FXCollections.observableArrayList(ProfilePicture.values()));

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromString'");
      }

    });
  }

  private void fillFriendsList() {
    if (friends == null) {
      return;
    }

    friendsList.getChildren().setAll();
    for (UserProfile p : friends) {
      friendsList.getChildren().add(FriendUtils.createComponent(p));
    }
  }

  public void recieveFriendsList(ArrayList<UserProfile> friends) {
    ServerMenuController.friends = friends;
    fillFriendsList();
  }

  public void recieveNotification(String msg, NotificationType type) {
    notificationManager.recieve(msg, type);
  }

  private void requestFriendsList() {
    if (friends == null) {
      NetworkClient.fetchFriends();
    }
  }

  public void updateFriendOnlineStatus(String username) {
    if (friends != null) {
      friends.stream().filter(up -> up.getUserName().equals(username)).forEach(up -> {
        up.setIsOnline(true);
        FriendUtils.update(friendsList, up);
      });
    }
  }

  public static void newFriendOnline(String username) {
    if (friends != null) {
      friends.stream().filter(up -> up.getUserName().equals(username)).forEach(up -> {
        up.setIsOnline(true);
      });
    }

  }
}
