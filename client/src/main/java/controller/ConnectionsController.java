package controller;

import java.io.IOException;

import controller.utils.RecentConnection;
import controller.utils.RecentConnectionRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import network.NetworkClient;
import utils.SceneManager;
import utils.NotificationManager.NotificationType;
import utils.CursorManager;
import utils.NotificationManager;

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

  // notification
  @FXML
  Pane notificationPane;
  @FXML
  TextFlow notificationText;
  @FXML
  ImageView notificationIcon;

  private NotificationManager notificationManager;

  public void initialize() {
    NetworkClient.bindConnectionController(this);

    notificationManager =
        new NotificationManager(notificationPane, notificationText, notificationIcon);

    // set backgrounds
    connectionListView.setBackground(
        new Background(
            new BackgroundImage(new Image("/assets/recent_connection_background.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(300, 300, false, false, false, false))));

    // set our custom cell factory
    connectionListView.setCellFactory(new RecentConnection.ConnectionCellFactory());

    // load recent connections
    RecentConnectionRegistry.load();
    connectionListView
        .setItems(FXCollections.observableArrayList(RecentConnectionRegistry.getConnections()));

    Button[] allButtons =
        {addNewConnectionButton, addConnectionButton, addConnectionBackButton, connectButton,
            backButton, loginButton, loginBackButton};

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
    // -- login screen
    usernameInput.setOnAction(e -> attemptLogin());
    passwordInput.setOnAction(e -> attemptLogin());
    loginButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ENTER)
        attemptLogin();
    });
    loginPane.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        loginPane.setVisible(false);
        e.consume();
      }
    });
    // -- add connection screen
    ipInput.setOnAction(e -> addConnection());
    portInput.setOnAction(e -> addConnection());
    connectionNameInput.setOnAction(e -> addConnection());
    addConnectionPane.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        addConnectionPane.setVisible(false);
        e.consume();
      }
    });
    // -- connection screen
    menuPane.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE)
        SceneManager.showScene("menu.fxml");
    });
  }

  private void addConnection() {
    try {
      Integer port = Integer.valueOf(portInput.getText());
      RecentConnection c =
          new RecentConnection(ipInput.getText(), port, connectionNameInput.getText());
      connectionListView.getItems().add(c);
      RecentConnectionRegistry.add(c);
      notificationManager.recieve("Added connection");
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

  public void recieveNotification(String message) {
    notificationManager.recieve(message);
  }

  private void connectToHost(RecentConnection c, String username, String password) {
    RecentConnectionRegistry.save();

    notificationManager
        .recieve("Attempting to connect user " + username + " to server at " + c.getIp() + ":" +
            c.getPort());
    try {
      if (!NetworkClient.connect(c.getIp(), c.getPort(), username, password)) {
        notificationManager
            .recieve("Failed to connect to server at " + c.getIp() + ":" +
                c.getPort(), NotificationType.CONNECTION_ERROR);
      } else {
        c.updateLastConnected();
      }
    } catch (IOException e1) {
      notificationManager
          .recieve("Connection refused", NotificationType.CONNECTION_ERROR);
    }
  }
}
