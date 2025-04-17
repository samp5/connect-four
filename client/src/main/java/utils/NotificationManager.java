package utils;

import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
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
  private boolean notificationOut;
  private PathTransition reverse;

  public NotificationManager(Pane notifPane, TextFlow notifText) {
    this.notificationPane = notifPane;
    this.notificationText = notifText;
  }

  // TODO: update these numbers to be based on the notifPane minWidth
  public void recieve(Node... nodes) {
    System.out.println("recieving notification");
    // queue notifications
    if (notificationOut) {
      EventHandler<ActionEvent> current = reverse.getOnFinished();
      reverse.setOnFinished(e -> {
        current.handle(null);
        recieve(nodes);
      });
      return;
    }

    notificationText.getChildren().setAll(nodes);
    Path extendPath = new Path(new MoveTo(480, -280), new HLineTo(160));
    PathTransition animation =
        new PathTransition(Duration.seconds(.5), extendPath, notificationPane);
    animation.play();
    notificationPane.setVisible(true);
    notificationOut = true;

    Path hidePath = new Path(new MoveTo(160, -280), new HLineTo(480));
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
    t.getStyleClass().add("text");
    recieve(t);
  }
}
