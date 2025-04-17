package utils;

import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class NotificationManager {
  Pane notificationPane;
  TextFlow notificationText;
  ImageView notificationIcon;
  private boolean notificationOut;
  private PathTransition reverse;

  public enum NotificationType {
    CONNECTION_ERROR, INFORMATION;

    public Image getIcon() {
      Image i = null;
      switch (this) {
        case CONNECTION_ERROR:
          i = new Image("/assets/connection_error_icon.png", 25, 25, false, false);
          break;
        case INFORMATION:
          i = new Image("/assets/information_icon.png", 25, 25, false, false);
          break;
      }
      return i;
    }

  }

  public NotificationManager(Pane notifPane, TextFlow notifText, ImageView notifImage) {
    this.notificationPane = notifPane;
    this.notificationText = notifText;
    this.notificationIcon = notifImage;
  }

  // TODO: update these numbers to be based on the notifPane minWidth
  private void recieve(Text text, Image icon) {

    // queue notifications
    if (notificationOut) {
      EventHandler<ActionEvent> current = reverse.getOnFinished();
      reverse.setOnFinished(e -> {
        current.handle(null);
        recieve(text, icon);
      });
      return;
    }


    double shownX = 120;
    double hiddenX = 600;
    double y = 40;

    notificationText.getChildren().setAll(text);
    notificationIcon.setImage(icon);

    Path extendPath = new Path(new MoveTo(hiddenX, y), new HLineTo(shownX));
    PathTransition animation =
        new PathTransition(Duration.seconds(.5), extendPath, notificationPane);
    animation.play();
    notificationPane.setVisible(true);
    notificationOut = true;

    Path hidePath =
        new Path(new MoveTo(shownX, y), new HLineTo(hiddenX));
    reverse = new PathTransition(Duration.seconds(.5), hidePath, notificationPane);

    PauseTransition delay = new PauseTransition(Duration.seconds(3));

    animation.setOnFinished(e -> {
      delay.play();
    });
    delay.setOnFinished(e -> {
      reverse.play();
    });
    reverse.setOnFinished(e -> {
      notificationPane.setVisible(false);
      notificationOut = false;
    });

  }

  public void recieve(String text) {
    Text t = new Text(text);
    notificationIcon.imageProperty().set(null);
    t.getStyleClass().add("text");
    recieve(t, null);
  }

  public void recieve(String text, NotificationType type) {
    Image i = type.getIcon();
    Text t = new Text(text);
    t.getStyleClass().add("text");
    recieve(t, i);
  }
}
