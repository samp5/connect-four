package controller.utils;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import network.NetworkClient;
import network.UserProfile;
import utils.CursorManager;
import utils.SceneManager;
import utils.ToolTipHelper;
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

    Button removeFriend = new Button();
    removeFriend.setMaxSize(51, 48);
    removeFriend.setPrefSize(51, 48);
    removeFriend.setMinSize(51, 48);
    removeFriend.getStyleClass().add("remove-friend-button");

    Button inviteToGame = new Button();
    inviteToGame.setMaxSize(51, 48);
    inviteToGame.setPrefSize(51, 48);
    inviteToGame.setMinSize(51, 48);
    inviteToGame.getStyleClass().add("invite-button");

    Button chatWithFriend = new Button();
    chatWithFriend.setMaxSize(51, 48);
    chatWithFriend.setPrefSize(51, 48);
    chatWithFriend.setMinSize(51, 48);
    chatWithFriend.getStyleClass().add("chat-button");

    inviteToGame.setTooltip(ToolTipHelper.make("Invite " + profile.getUserName() + " to a game"));
    chatWithFriend.setTooltip(ToolTipHelper.make("Open chat with " + profile.getUserName()));
    removeFriend.setTooltip(ToolTipHelper.make("Remove  " + profile.getUserName() + " from your friends "));

    CursorManager.setHandCursor(inviteToGame, chatWithFriend, removeFriend);

    chatWithFriend.setOnAction(e -> {
      openChat(profile.getId());
    });

    inviteToGame.setOnAction(e -> {
      inviteToGame.setDisable(true);
      CursorManager.setPointerCursor(inviteToGame);
      NetworkClient.inviteToGame(profile.getId());
      PauseTransition pt = new PauseTransition(Duration.seconds(20));
      pt.setOnFinished(f -> {
        if (inviteToGame != null) {
          inviteToGame.setDisable(false);
        }
      });
      pt.play();
    });

    HBox friendActions = new HBox(chatWithFriend, inviteToGame, removeFriend);
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

    // need a ref to box so this on action goes here
    removeFriend.setOnAction(e -> {
      NetworkClient.removeFriend(profile.getId());
      ((VBox) box.getParent()).getChildren().remove(box);
    });

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

  public static void reenableInvite(VBox allFriends, String userName) {
    // technically this is one line
    // this is my monument to streams, call it a river
    allFriends.getChildren().stream().map(n -> (HBox) n).filter(hb -> {
      return hb.getChildren()
          .stream()
          .filter(TextFlow.class::isInstance)
          .map(fl -> (TextFlow) fl)
          .filter(fl -> fl.getChildren()
              .stream()
              .filter(Text.class::isInstance)
              .map(t -> (Text) t)
              .filter(t -> t.getText().trim().equals(userName))
              .findFirst()
              .isPresent())
          .findFirst()
          .isPresent();
    }).forEach(n -> {
      n.getChildren()
          .stream()
          .filter(HBox.class::isInstance)
          .map(nd -> (HBox) nd)
          .forEach(hb -> {
            System.out.println("got here");
            hb.getChildren()
                .stream()
                .filter(Button.class::isInstance)
                .map(bt -> (Button) bt)
                .filter(bt -> bt.getStyleClass().contains("invite-button"))
                .forEach(bt -> {
                  bt.setDisable(false);
                  CursorManager.setHandCursor(bt);
                });
          });
      ;
    });
  }

}
