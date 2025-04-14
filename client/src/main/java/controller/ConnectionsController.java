package controller;

import java.io.IOException;

import controller.utils.RecentConnection;
import controller.utils.RecentConnectionRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import network.NetworkClient;
import utils.SceneManager;
import utils.CursorManager;

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

    // load recent connections
    RecentConnectionRegistry.load();
    connectionListView.setItems(FXCollections.observableArrayList(RecentConnectionRegistry.getConnections()));

    Button[] backButtons = { addConnectionBackButton, loginBackButton, backButton };
    for (Button b : backButtons) {
      b.setText("\u21AB");
      b.getStyleClass().add("back-button");
    }

    Button[] allButtons = { addNewConnectionButton, addConnectionButton, addConnectionBackButton, connectButton,
        backButton, loginButton, loginBackButton };
    for (Button b : allButtons) {
      CursorManager.setHandCursor(b);
    }

    setHandlers();
  }

  private void setHandlers() {
    addNewConnectionButton.setOnAction(e -> {
      addConnectionPane.setVisible(true);
      ipInput.requestFocus();
    });
    addConnectionButton.setOnAction(e -> {
      addConnection();
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
        usernameInput.requestFocus();
      }
    });
    loginButton.setOnAction(e -> {
      attemptLogin();
    });

    backButton.setOnAction(e -> {
      RecentConnectionRegistry.save();
      SceneManager.showScene("menu.fxml");
    });

    // enter and escape press handlers
    //   -- login screen
    usernameInput.setOnAction(e -> attemptLogin());
    passwordInput.setOnAction(e -> attemptLogin());
    loginButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ENTER) attemptLogin();
    });
    loginPane.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        loginPane.setVisible(false);
        e.consume();
      }
    });
    //   -- add connection screen
    ipInput.setOnAction(e -> addConnection());
    portInput.setOnAction(e -> addConnection());
    connectionNameInput.setOnAction(e -> addConnection());
    addConnectionPane.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        addConnectionPane.setVisible(false);
        e.consume();
      }
    });
    //   -- connection screen
    menuPane.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) SceneManager.showScene("menu.fxml");
    });
  }

  private void addConnection() {
    try {
      Integer port = Integer.valueOf(portInput.getText());
      RecentConnection c = new RecentConnection(ipInput.getText(), port, connectionNameInput.getText());
      connectionListView.getItems().add(c);
      RecentConnectionRegistry.add(c);
    } catch (NumberFormatException f) {

      // TODO: Add cool format validation on textinput
      return;
    }
    addConnectionPane.setVisible(false);
  }

  private void attemptLogin() {
    RecentConnection c = connectionListView.getSelectionModel().getSelectedItem();
    String username = usernameInput.getText();
    String password = passwordInput.getText();
    if (c != null && username != "" && password != "") {
      connectToHost(c, username, password);
    } else {
      loginPane.setVisible(false);
    }
  }

  private void connectToHost(RecentConnection c, String username, String password) {
    c.updateLastConnected();
    RecentConnectionRegistry.save();

    System.out.printf("Attempting to connect user `%s` to server at %s:%d\n", username, c.getIp(), c.getPort());
    try {
      NetworkClient.connect(c.getIp(), c.getPort(), username, password);
    } catch (IOException e1) {
    }
  }
}
