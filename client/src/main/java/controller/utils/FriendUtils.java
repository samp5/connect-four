package controller.utils;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.image.Image;

import java.util.stream.Collectors;

import javafx.scene.Node;
import network.UserProfile;

// Generate entries for a userprofile meant for a users friend list
public class FriendUtils {

  public static HBox createComponent(UserProfile profile) {
    Image indicatorImage;
    if (profile.isOnline()) {
      indicatorImage = new Image("/assets/online.png", 10, 10, false, false);
    } else {
      indicatorImage = new Image("/assets/offline.png", 10, 10, false, false);
    }
    ImageView onlineIndicator = new ImageView(indicatorImage);

    Text userNameText = new Text(profile.getUserName() + "  ");
    userNameText.getStyleClass().add("text");
    TextFlow userName = new TextFlow(userNameText, onlineIndicator);

    HBox box = new HBox(userName);
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
