package controller.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatMessage {
  String senderName;
  int senderID;
  LocalDateTime sendTime;
  String message;

  @FXML
  HBox rowBox;
  @FXML
  TextFlow messageFlow;
  @FXML
  TextFlow timeStampFlow;
  @FXML
  TextFlow senderFlow;

  public void build(String sender, int senderID, String msg, boolean local) {
    this.senderName = sender;
    this.senderID = senderID;
    this.message = msg;
    this.sendTime = LocalDateTime.now();
    if (local) {
      rowBox.setAlignment(Pos.CENTER_RIGHT);
    } else {
      rowBox.setAlignment(Pos.CENTER_LEFT);
    }
    messageFlow.getChildren().setAll(Markup.markup(this.message));
    Text timestampTxt = new Text(this.sendTime.format(DateTimeFormatter.ofPattern("h:m a")));
    timestampTxt.getStyleClass().add("text-chat-timestamp");
    timeStampFlow.getChildren().setAll(timestampTxt);
  }

  public void initialize() {
  }
}
