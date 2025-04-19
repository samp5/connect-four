package controller;

import java.util.Timer;
import java.util.TimerTask;

import controller.utils.GameSettings;
import controller.utils.RecentConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import network.NetworkClient;
import utils.SceneManager;

/**
 * Bind to server_menu.fxml
 *
 */
public class ServerMenuController extends Controller {
  @FXML
  Pane menuPane;

  @FXML
  Button disconnectButton;
  @FXML
  Button joinButton;
  @FXML
  Button settingsButton;
  @FXML
  Button leaderBoardButton;

  @FXML
  Text serverName;
  @FXML
  Text nGames;
  @FXML
  Text nPlayers;
  @FXML
  Text portTxt;
  @FXML
  Text ipText;

  private static final String placeHolderText = "...";
  private static RecentConnection connection;
  private static ServerPlayerInfo playerInfo;
  private Timer timer;

  private static class ServerPlayerInfo {
    int online;
    int activeGames;

    ServerPlayerInfo(int online, int games) {
      this.online = online;
      this.activeGames = games;
    }
  }

  public void initialize() {

    NetworkClient.bindServerMenuController(this);
    NetworkClient.getServerInfo();

    scheduleDataFetch();

    if (connection == null) {
      ipText.setText(placeHolderText);
      portTxt.setText(placeHolderText);
    } else {
      setServerInfo(connection);
    }

    if (playerInfo == null) {
      nGames.setText(placeHolderText);
      nPlayers.setText(placeHolderText);
    } else {
      setPlayerInfo(playerInfo.online, playerInfo.activeGames);
    }
    setHandlers();
  }

  private void setHandlers() {
    joinButton.setOnAction(e -> {
      NetworkClient.joinGame();
      SceneManager.showScene("loading.fxml");
    });
    settingsButton.setOnAction(e -> {
      GameSettings.loadOnto(menuPane);
    });
    leaderBoardButton.setOnAction(e -> {
      LeaderBoardController.loadOnto(menuPane);
    });
    disconnectButton.setOnAction(e -> {
      NetworkClient.disconnect();
      SceneManager.showScene("menu.fxml");
    });
  }

  public void setServerInfo(RecentConnection conn) {
    connection = conn;
    setServerInfo(conn.getName(), conn.getIp(), conn.getPort());
  }

  private void setServerInfo(String name, String IP, int Port) {
    serverName.setText(name);
    ipText.setText(IP);
    portTxt.setText(String.valueOf(Port));
  }

  public void setPlayerInfo(int numPlayers, int numGames) {
    playerInfo = new ServerPlayerInfo(numPlayers, numGames);
    nPlayers.setText(String.valueOf(numPlayers));
    nGames.setText(String.valueOf(numGames));
  }

  private void scheduleDataFetch() {
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        NetworkClient.getServerInfo();
      }
    }, 5000, 5000);
  }
}
