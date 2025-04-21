package controller.utils;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import network.NetworkClient;
import network.UserProfile;
import utils.CursorManager;
import utils.SceneManager;
import controller.FriendChatController;
import network.Player;

// Generate entries for a userprofile meant for a users friend list
public class FriendUtils {

  private static HashMap<Long, FriendChatController> openChats = new HashMap<>();

  public static void receiveChat(Player sender, String chat) {
    FriendChatController ctl = openChats.get(sender.getID());
    if (ctl == null) {
      openChat(sender.getID());
      ctl = openChats.get(sender.getID());
    }
    ctl.recieveMessage(chat, sender.getUsername(), false);
  }

  public static void reset() {
    for (FriendChatController ctl : openChats.values()) {
      ctl.close();
    }
    openChats.clear();
  }

  public static void friendOnlineStatus(Long ID, boolean isOnline) {
    FriendChatController ctl = openChats.get(ID);
    if (ctl == null) {
      return;
    }
    ctl.friendOnlineStatus(isOnline);
  }

  public static void openChat(Long ID) {
    if (openChats.get(ID) != null) {
      return;
    }
    try {
      FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/friend_chat.fxml"));
      Scene scene = new Scene(loader.load(), 360, 720);
      FriendChatController ctl = loader.getController();
      openChats.put(ID, ctl);
      ctl.setFriendID(ID);
      CursorManager.setPointerCursor(scene);
      scene.getStylesheets().add("/css/chat.css");
      scene.getStylesheets().add("/css/game.css");
      scene.getStylesheets().add("/css/menu.css");
      Stage chatStage = new Stage();
      chatStage.setScene(scene);
      chatStage.setResizable(false);
      chatStage.show();
      chatStage.setOnCloseRequest(e -> {
        closeChat(ID);
      });
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static void closeChat(Long ID) {
    openChats.remove(ID);
  }

  public static HBox createComponent(UserProfile profile) {

    Button inviteToGame = new Button("Invite");
    inviteToGame.setMaxSize(100, 40);
    inviteToGame.setPrefSize(100, 40);
    inviteToGame.setMinSize(100, 40);
    inviteToGame.getStyleClass().add("pixel-button");

    Button chatWithFriend = new Button("Chat");
    chatWithFriend.setMaxSize(75, 40);
    chatWithFriend.setPrefSize(75, 40);
    chatWithFriend.setMinSize(75, 40);
    chatWithFriend.getStyleClass().add("pixel-button");

    chatWithFriend.setOnAction(e -> {
      openChat(profile.getId());
    });

    inviteToGame.setOnAction(e -> {
      inviteToGame.setDisable(true);
      inviteToGame.setText("Invited");
      NetworkClient.inviteToGame(profile.getId());
    });

    HBox friendActions = new HBox(chatWithFriend, inviteToGame);
    friendActions.setAlignment(Pos.CENTER_RIGHT);
    friendActions.setMinWidth(200);
    friendActions.setMaxWidth(200);
    friendActions.setSpacing(20);
    friendActions.setMaxHeight(40);

    Text userNameText = new Text(profile.getUserName() + "  ");
    userNameText.getStyleClass().add("text-medium-large");

    Image indicatorImage;
    if (profile.isOnline()) {
      indicatorImage = new Image("/assets/online.png", 10, 10, false, false);
    } else {
      indicatorImage = new Image("/assets/offline.png", 10, 10, false, false);
      inviteToGame.setDisable(true);
      chatWithFriend.setDisable(true);
    }

    ImageView onlineIndicator = new ImageView(indicatorImage);
    TextFlow userName = new TextFlow(userNameText, onlineIndicator);
    userName.setMaxHeight(24);
    userName.setMinWidth(130);
    userName.setMaxWidth(130);

    HBox box = new HBox(userName, friendActions);
    box.setPrefSize(330, 50);
    box.setMaxSize(330, 50);
    box.setMinSize(330, 50);
    box.setAlignment(Pos.CENTER);

    return box;
  }

  public static void update(VBox allFriends, UserProfile newProfile) {
    // all nodes of this are HBox
    allFriends.getChildren().replaceAll(n -> {
      HBox box = (HBox) n;
      var match = box.getChildren().filtered(t -> {
        return t instanceof TextFlow && (((TextFlow) t).getChildren().filtered(txt -> {
          return (txt instanceof Text) && ((Text) txt).getText().trim().equals(newProfile.getUserName());
        }).size() > 0);
      });
      if (match.size() > 0) {
        return FriendUtils.createComponent(newProfile);
      } else {
        return n;
      }
    });
  }

}
