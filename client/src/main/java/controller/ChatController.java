package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import network.NetworkClient;
import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.util.TextUtils;

import controller.utils.Markup;

/**
 * Control the UI of the chat window
 * Send chat messages
 * Display messages from this client + others
 *
 * Bind to Chat.fxml
 */
public class ChatController {

  @FXML
  Button sendButton;

  @FXML
  HBox chatEditorBox;
  @FXML
  TextArea chatEditorInput;
  @FXML
  TextFlow chatEditorDisplay;

  @FXML
  // something like this
  private Collection<String> message;

  public void initialize() {
    NetworkClient.bindChatController(this);
    System.out.println("Chat controller initialized");
    setHandlers();
    chatEditorInput.setOpacity(0);
    chatEditorInput.toBack();
    chatEditorDisplay.toFront();
  }

  private void setHandlers() {
    sendButton.setOnAction(e -> {
    });
    chatEditorInput.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {

        if (newValue != null) {
          String unicodeText = ChatController.createUnicodeText(newValue);
          List<Node> flowNodes = TextUtils.convertToTextAndImageNodes(unicodeText);
          ArrayList<Node> splitFlowNodes = new ArrayList<>();

          flowNodes.stream().forEach(n -> {
            if (Text.class.isInstance(n) && Markup.containsMarkup(((Text) n).getText())) {
              splitFlowNodes.addAll(Markup.splitOnMarkup(((Text) n).getText()).stream().map((s) -> {
                return new Text(s);
              }).collect(Collectors.toList()));
            } else {
              splitFlowNodes.add(n);
            }

          });

          splitFlowNodes.stream().filter(Text.class::isInstance).forEach(n -> {
            Text t = (Text) n;
            if (Markup.isBold(t.getText().strip())) {
              Font f = t.getFont();
              t.setText(t.getText().substring(2, t.getText().length() - 2));
              t.setFont(Font.font(f.getFamily(), FontWeight.BOLD, f.getSize()));
            } else if (Markup.isItalic(t.getText().strip())) {
              Font f = t.getFont();
              t.setText(" " + t.getText().substring(2, t.getText().length() - 2) + " ");
              t.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));
            }
          });
          chatEditorDisplay.getChildren().setAll(splitFlowNodes);
        }
      }
    });
  }

  private static String createUnicodeText(String nv) {
    StringBuilder unicodeText = new StringBuilder();
    String[] words = nv.split(" ");
    for (String word : words) {
      if (word.length() > 2 && word.charAt(word.length() - 1) == ':' && word.charAt(0) == ':') {
        Optional<Emoji> optionalEmoji = EmojiData.emojiFromShortName(word.substring(1, word.length() - 1));
        unicodeText.append(optionalEmoji.isPresent() ? optionalEmoji.get().character() : word);
        unicodeText.append(" ");
      } else {
        unicodeText.append(word);
        unicodeText.append(" ");
      }
    }
    return unicodeText.toString();
  }

  // when the send button clicked maybe?
  @FXML
  public void onSend() {
  }

  // when we need to display a new message
  public void appendMessage(String msg) {
  }
}
