package controller.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    Text timestampTxt = new Text(this.sendTime.format(DateTimeFormatter.ofPattern("h:mm a")));
    timestampTxt.getStyleClass().add("text-chat-timestamp");
    timeStampFlow.getChildren().setAll(timestampTxt);

    Text senderTxt = new Text(sender + "  ");
    senderTxt.getStyleClass().add("text-chat-timestamp");
    senderFlow.getChildren().setAll(senderTxt);

    if (local) {
      messageFlow.setTextAlignment(TextAlignment.RIGHT);
      messageBox.getStyleClass().add("message-local");
      messageRowBox.setAlignment(Pos.CENTER_RIGHT);
      timeStampBox.setAlignment(Pos.CENTER_RIGHT);
    } else {
      messageFlow.setTextAlignment(TextAlignment.LEFT);
      messageBox.getStyleClass().add("message-remote");
      messageRowBox.setAlignment(Pos.CENTER_LEFT);
      timeStampBox.setAlignment(Pos.CENTER_LEFT);
    }

  }

  public void initialize() {
  }
}
