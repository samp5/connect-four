package controller.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
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

  public void set(String sender, int senderID, String msg, Font f, boolean local) {
    this.senderName = sender;
    this.senderID = senderID;
    this.message = msg;
    this.sendTime = LocalDateTime.now();
    if (local) {
      rowBox.setAlignment(Pos.CENTER_RIGHT);
    } else {
      rowBox.setAlignment(Pos.CENTER_LEFT);
    }
    messageFlow.getChildren().setAll(Markup.markup(this.message, f));
    Text timestampTxt = new Text(this.sendTime.format(DateTimeFormatter.ofPattern("h:m a")));
    timestampTxt.setFont(new Font(f.getFamily(), 10.0));
    timeStampFlow.getChildren().setAll(timestampTxt);
  }

  public void initialize() {
  }
}
