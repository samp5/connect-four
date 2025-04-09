package controller;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import network.NetworkClient;

/**
 * Bind to Menu.fxml
 *
 * Handle user input for logging in (settings maybe??)
 *
 * Trigger the server connection
 * Navigate to game/chat screens on successful connection
 *
 */
public class MenuController {
  @FXML
  TextField usernameInput;
  @FXML
  TextField ipInput;
  @FXML
  TextField portInput;

  @FXML
  Button joinButton;

  public void initialize() {
    joinButton.setOnMouseClicked((e) -> {
      String username = usernameInput.getText();
      String ip = ipInput.getText();
      String portStr = portInput.getText();

      if (username == "" || ip == "" || portStr == "") {
        return;
      }

      Integer port = Optional.ofNullable(portStr).map(Integer::valueOf).orElse(null);
      if (port == null) return;

      System.out.printf("Attempting to connect user `%s` to server at %s:%d\n", username, ip, port);
      boolean connected = false;
      try {
		    connected = NetworkClient.connect(ip, port);
	    } catch (IOException e1) {
        System.out.println("Error connecting.");
	    }
      if (connected) {
        System.out.println("Sucessfully connected to server.");
        while (NetworkClient.getMessages() < 1);
      }
    });
  }
}
