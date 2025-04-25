package controller;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import controller.utils.RecentConnection;
import controller.utils.RecentConnectionRegistry;
import controller.utils.ValidatedInput;
import controller.utils.ValidatedInput.ValidationMethod;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import network.NetworkClient;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;
import utils.NotificationManager.NotificationType;
import utils.AudioManager;
import utils.CursorManager;
import utils.NotificationManager;

public class ConnectionsController extends Controller {
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
  ImageView background;
  int grassState = 0;

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

  ValidatedInput validIpInput;
  ValidatedInput validPortInput;
  ValidatedInput validConnNameInput;
  ValidatedInput validUsernameInput;
  ValidatedInput validPasswordInput;

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

    notificationManager = new NotificationManager(notificationPane, notificationText, notificationIcon);

    validIpInput = new ValidatedInput(ipInput, ValidationMethod.IP_ADDRESS);
    validPortInput = new ValidatedInput(portInput, ValidationMethod.PORT);
    validConnNameInput = new ValidatedInput(connectionNameInput, ValidationMethod.LENGTH, ValidationMethod.NOT_EMPTY);
    validUsernameInput = new ValidatedInput(usernameInput, ValidationMethod.NO_SPACES, ValidationMethod.LENGTH, ValidationMethod.NOT_EMPTY);
    validPasswordInput = new ValidatedInput(passwordInput, ValidationMethod.NO_SPACES, ValidationMethod.NOT_EMPTY);

    // set backgrounds
    connectionListView.setBackground(
        new Background(
            new BackgroundImage(new Image("/assets/recent_connection_background.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(300, 300, false, false, false, false))));

    // set our custom cell factory
    connectionListView.setCellFactory(new RecentConnection.ConnectionCellFactory());

    // animate grass
    animateGrass();

    // load recent connections
    RecentConnectionRegistry.load();
    connectionListView
        .setItems(FXCollections.observableArrayList(RecentConnectionRegistry.getConnections()));
    connectionListView.getSelectionModel().select(0);

    CursorManager.setHandCursor(addNewConnectionButton, addConnectionButton, addConnectionBackButton, connectButton, backButton, loginButton, loginBackButton);
    AudioManager.setAudioButton(addNewConnectionButton, addConnectionButton, addConnectionBackButton, connectButton, backButton, loginButton, loginBackButton);

    setHandlers();
  }

  private void animateGrass() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        grassState = (grassState + 1) % 2;
        try {
          Platform.runLater(() -> {
            background.setViewport(new Rectangle2D((3840 * grassState), 0, 3840, 2560));
          });
        } catch (Exception e) {}
      }
    }, 0, 1000);
  }

  private void setHandlers() {
    addNewConnectionButton.setOnAction(e -> {
      addConnectionPane.setVisible(true);
      ipInput.requestFocus();
    });
    addNewConnectionButton.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        addNewConnectionButton.getOnAction().handle(null);
        e.consume();
      }
    });

    addConnectionButton.setOnAction(e -> {
      addConnection();
    });
    addConnectionButton.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        addConnectionButton.getOnAction().handle(null);
        e.consume();
      }
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
      SceneManager.showScene(SceneSelections.MAIN_MENU);
    });
    backButton.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        backButton.getOnAction().handle(null);
    });

    // enter and escape press handlers
    // -- login screen
    loginPane.setVisible(false);
    loginPane.setOnKeyReleased(e -> {
      if (loginPane.isVisible()) {
        e.consume();
      }

      if (e.getCode() == KeyCode.ESCAPE) {
        loginPane.setVisible(false);
      } else if (e.getCode() == KeyCode.ENTER) {
        attemptLogin();
      }
    });

    // -- add connection screen
    addConnectionPane.setVisible(false);
    addConnectionPane.setOnKeyReleased(e -> {
      if (addConnectionPane.isVisible()) {
        e.consume();
      }

      if (e.getCode() == KeyCode.ESCAPE) {
        addConnectionPane.setVisible(false);
      } else if (e.getCode() == KeyCode.ENTER) {
        addConnection();
      }
    });

    // -- connection screen
    menuPane.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        SceneManager.showScene(SceneSelections.MAIN_MENU);
      } else if (e.getCode() == KeyCode.ENTER) {
        if (connectionListView.getSelectionModel().getSelectedItem() != null) {
          loginPane.setVisible(true);
          usernameInput.requestFocus();
        }
      }
    });

    menuPane.requestFocus();
  }

  private void addConnection() {
    if (!validIpInput.isValid()) {
      notificationManager.recieve(
          String.format("Input IP %s", validIpInput.getReason()), NotificationType.ERROR);
      return;
    }
    if (!validPortInput.isValid()) {
      notificationManager.recieve(
          String.format("Input Port %s", validPortInput.getReason()), NotificationType.ERROR);
      return;
    }
    if (!validConnNameInput.isValid()) {
      notificationManager.recieve(
          String.format("Input name %s", validConnNameInput.getReason()), NotificationType.ERROR);
      return;
    }

    try {
      Integer port = Integer.valueOf(portInput.getText());
      RecentConnection c = new RecentConnection(ipInput.getText(), port, connectionNameInput.getText());
      connectionListView.getItems().add(c);
      connectionListView.getSelectionModel().select(c);
      RecentConnectionRegistry.add(c);
      notificationManager.recieve("Added connection");
    } catch (NumberFormatException f) {

      return;
    }
    addConnectionPane.setVisible(false);
  }

  private void attemptLogin() {
    if (!validUsernameInput.isValid()) {
      notificationManager.recieve(
          String.format("Input Username %s", validUsernameInput.getReason()), NotificationType.ERROR);
      return;
    }
    if (!validPasswordInput.isValid()) {
      notificationManager.recieve(
          String.format("Input Password %s", validPasswordInput.getReason()), NotificationType.ERROR);
      return;
    }

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
        .recieve("Attempting to connect to server at " + c.getIp() + ":" +
            c.getPort());
    try {

      NetworkClient.connect(c.getIp(), c.getPort(), username, password, () -> {
        Platform.runLater(() -> {
          notificationManager
            .recieve("Failed to connect to server at " + c.getIp() + ":" +
                c.getPort(), NotificationType.CONNECTION_ERROR);
        });
        return null;
      });

      c.updateLastConnected();

    } catch (IOException e1) {

      notificationManager
          .recieve("Connection refused", NotificationType.CONNECTION_ERROR);

    }
  }
}
