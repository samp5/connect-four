package controller;

import java.io.IOException;
import java.util.Collection;

import javafx.animation.PauseTransition;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import network.NetworkClient;
import controller.utils.ChatMessage;
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
  Button ffButton;
  @FXML
  Button drawButton;
  @FXML
  Button requestFFButton;

  @FXML
  Pane confirmPopup;
  @FXML
  Button popupCancelButton;
  @FXML
  Button popupConfirmButton;
  @FXML
  Text popupConfirmText;

  @FXML
  HBox chatEditorBox;
  @FXML
  TextArea chatEditorInput;
  @FXML
  TextFlow chatEditorDisplay;
  @FXML
  VBox chatHistory;
  @FXML
  ScrollPane chatHistoryScroll;
  @FXML
  BorderPane chatPane;

  public void initialize() {
    NetworkClient.bindChatController(this);
    setHandlers();

    // auto scroll the chat history based on the height of the vbox
    chatHistoryScroll.vvalueProperty().bind(chatHistory.heightProperty());

    // can't do this in fxml easily
    sendButton
        .setBackground(new Background(new BackgroundImage(new Image("/assets/send_button.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(100, 50, false, false, false, false))));
  }

  private void setHandlers() {
    sendButton.setOnAction(e -> {
      sendMessage();
    });

    chatEditorInput.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {

          chatEditorDisplay.getChildren()
              .setAll(Markup.markup(newValue));
        }
      }
    });

    chatEditorInput.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        sendMessage();
      }
      e.consume();
    });

    popupCancelButton.setOnAction(e -> {
      confirmPopup.visibleProperty().set(false);
    });

    ffButton.setOnAction(e -> {
      confirmPopup.visibleProperty().set(true);
      popupConfirmText.setText("forfeit?");

      popupConfirmButton.setOnAction(e0 -> {
        confirmPopup.visibleProperty().set(false);
        NetworkClient.forfeit();
      });
    });

    // drawButton.setOnAction(e -> {
    //   popup.show(ffButton.getScene().getWindow());
    //   popupConfirmText.setText("request draw?");
    //
    //   popupConfirmButton.setOnAction(e0 -> {
    //     popup.hide();
    //     NetworkClient.drawRequest();
    //   });
    // });
    //
    // requestFFButton.setOnAction(e -> {
    //   popup.show(ffButton.getScene().getWindow());
    //   popupConfirmText.setText("request forfeit?");
    //
    //   popupConfirmButton.setOnAction(e0 -> {
    //     popup.hide();
    //     System.out.println("confirmed forfeit request");
    //   });
    // });
  }

  private void sendMessage() {
    String msg = chatEditorInput.getText().trim();
    if (msg.length() == 0) {
      return;
    }
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      appendMessage(msg, GameLogic.getLocalPlayer().getUsername(), true);
      NetworkClient.sendChatMessage(msg);
    } else {
      appendMessage(msg, "you", true);
    }
    chatEditorInput.clear();
    chatEditorDisplay.getChildren().setAll();
  }

  public void recieveMessage(String message, String username, boolean local) {
    appendMessage(message, username, local);
  }

  // // when we need to display a new message
  public void appendMessage(String msg, String username, boolean local) {
    try {
      FXMLLoader loader =
          new FXMLLoader(ChatController.class.getResource("/fxml/chatMessage.fxml"));
      Region msgBox = loader.load();
      ChatMessage newMessageCTL = loader.getController();
      newMessageCTL.build(username, 0, msg, local);
      this.chatHistory.getChildren().add(msgBox);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
