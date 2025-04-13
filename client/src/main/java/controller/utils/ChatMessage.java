package controller.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatMessage {
  String senderName;
  int senderID;
  LocalDateTime sendTime;
  String message;

  @FXML
  HBox messageRowBox;
  @FXML
  HBox messageBox;
  @FXML
  TextFlow messageFlow;
  @FXML
  HBox timeStampBox;
  @FXML
  TextFlow timeStampFlow;
  @FXML
  TextFlow senderFlow;

  public void build(String sender, int senderID, String msg, boolean local) {
    this.senderName = sender;
    this.senderID = senderID;
    this.message = msg;
    this.sendTime = LocalDateTime.now();
    messageFlow.getChildren().setAll(Markup.markup(this.message));
    Text timestampTxt = new Text(this.sendTime.format(DateTimeFormatter.ofPattern("h:m a")));
    timestampTxt.getStyleClass().add("text-chat-timestamp");
    timeStampFlow.getChildren().setAll(timestampTxt);

    if (local) {
      messageBox.setBackground(
          new Background(new BackgroundImage(new Image("/assets/chat_message_local.png"),
              BackgroundRepeat.NO_REPEAT,
              BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
              new BackgroundSize(100, 100, true, true,
                  false,
                  false))));
      messageRowBox.setAlignment(Pos.CENTER_RIGHT);
      timeStampBox.setAlignment(Pos.CENTER_RIGHT);
    } else {
      messageBox.setBackground(
          new Background(new BackgroundImage(new Image("/assets/chat_message_remote.png"),
              BackgroundRepeat.NO_REPEAT,
              BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
              new BackgroundSize(100, 100, true, true,
                  false,
                  false))));
      messageRowBox.setAlignment(Pos.CENTER_LEFT);
      timeStampBox.setAlignment(Pos.CENTER_LEFT);
    }

  }

  public void initialize() {
  }
}
