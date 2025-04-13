package controller;

import java.io.IOException;
import java.util.Optional;

import controller.utils.RecentConnection;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import network.NetworkClient;
import utils.SceneManager;

public class ConnectionsController {
  @FXML
  Pane menuPane;
  @FXML
  Button addNewConnectionButton;
  @FXML
  Button addConnectionButton;
  @FXML
  Button addConnectionBackButton;
  @FXML
  Button connectButton;
  @FXML
  Button backButton;
  @FXML
  Button loginButton;
  @FXML
  Button loginBackButton;
  @FXML
  Pane addConnectionPane;
  @FXML
  Pane loginPane;

  @FXML
  TextField usernameInput;
  @FXML
  TextField passwordInput;
  @FXML
  TextField ipInput;
  @FXML
  TextField portInput;
  @FXML
  TextField connectionNameInput;
  @FXML
  ListView<RecentConnection> connectionListView;
  RecentConnection selectedConnection;

  @FXML
  Button joinButton;

  public void initialize() {

    // set backgrounds
    menuPane.setBackground(
        new Background(new BackgroundImage(new Image("/assets/load-background.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(1080, 720, false, false, false, false))));

    addConnectionPane.setBackground(
        new Background(new BackgroundImage(new Image("/assets/load-background.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(500, 500, false, false, false, false))));
    loginPane.setBackground(
        new Background(new BackgroundImage(new Image("/assets/load-background.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(500, 500, false, false, false, false))));

    connectionListView.setBackground(
        new Background(
            new BackgroundImage(new Image("/assets/recent_connection_background.png"), BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(300, 300, false, false, false, false))));

    // set our custom cell factory
    connectionListView.setCellFactory(new RecentConnection.ConnectionCellFactory());

    Button[] backButtons = {addConnectionBackButton, loginBackButton, backButton};
    for (Button b : backButtons){
      b.setText("\u21AB");
      b.getStyleClass().add("back-button");
    }

    Button[] allButtons = {addNewConnectionButton, addConnectionButton, addConnectionBackButton, connectButton, backButton, loginButton, loginBackButton};
    for (Button b : allButtons){
      b.setCursor(new ImageCursor(new Image("/assets/hand_cursor.png")));
    }

    setHandlers();
  }

  private void setHandlers() {
    addNewConnectionButton.setOnAction(e -> {
      addConnectionPane.setVisible(true);
    });
    addConnectionButton.setOnAction(e -> {
      try {
        Integer port = Integer.valueOf(portInput.getText());
        connectionListView.getItems().add(new RecentConnection(ipInput.getText(), port, connectionNameInput.getText()));
      } catch (NumberFormatException f) {
        // TODO: Add cool format validation on textinput
        return;
      }
      addConnectionPane.setVisible(false);
    });
    addConnectionBackButton.setOnAction(e -> {
      addConnectionPane.setVisible(false);
    });
    loginBackButton.setOnAction(e -> {
      loginPane.setVisible(false);
    });
    connectButton.setOnAction(e -> {
      if (connectionListView.getSelectionModel().getSelectedItem() != null) {
        loginPane.setVisible(true);
      }
    });
    loginButton.setOnAction(e -> {
      RecentConnection c = connectionListView.getSelectionModel().getSelectedItem();
      String username = usernameInput.getText();
      String password = passwordInput.getText();
      if (c != null && username != "" && password != "") {
        connectToHost(c, username, password);
      } else {
        loginPane.setVisible(false);
      }
    });

    backButton.setOnAction(e -> {
      SceneManager.showScene("menu.fxml");
    });
  }

  private void connectToHost(RecentConnection c, String username, String password) {
    System.out.printf("Attempting to connect user `%s` to server at %s:%d\n", username, c.getIp(), c.getPort());
    try {
      NetworkClient.connect(c.getIp(), c.getPort(), username, password);
    } catch (IOException e1) {
    }
  }
}
