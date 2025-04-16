package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import logic.AI;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import network.NetworkClient;
import utils.AudioManager;
import utils.CursorManager;
import utils.SceneManager;

public class SettingsController {
  @FXML
  Slider volumeSlider;
  @FXML
  Slider aiDifficultySlider;
  @FXML
  Pane settingsPane;
  @FXML
  Button backButton;

  @FXML
  Button mainMenuButton;

  private Pane parent;
  private Node root;

  public void attach(Pane parent, Pane root) {
    parent.getChildren().add(root);
    root.relocate((parent.getWidth() - 500) / 2,
        (parent.getHeight() - 500) / 2);
    root.toFront();
    this.parent = parent;
    this.root = root;
  }

  private void detach() {
    parent.getChildren().remove(root);
  }

  public void initialize() {
    setHandlers();
    styleElements();
    volumeSlider.setValue(volumeSlider.getMax() * AudioManager.getVolume());
    aiDifficultySlider.setValue(AI.getDifficulty());
  }

  public static Background getButtonBackground(int dim) {
    return new Background(new BackgroundImage(new Image("/assets/settings-button.png"),
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
        new BackgroundSize(dim, dim, false, false, false, false)));
  }

  private void styleElements() {
    settingsPane.setBackground(
        new Background(new BackgroundImage(new Image("/assets/load-background.png"),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(500, 500, false, false, false, false))));
    CursorManager.setHandCursor(volumeSlider);
  }

  private void setHandlers() {
    volumeSlider.valueProperty().addListener((observable, old, newValue) -> {
      if (newValue != null) {
        AudioManager.setVolume(newValue.doubleValue() / volumeSlider.getMax());
      }
    });
    aiDifficultySlider.valueProperty().addListener((observable, old, newValue) -> {
      if (newValue != null) {
        AI.setDifficulty(newValue.intValue());
      }
    });
    backButton.setOnAction(e -> {
      detach();
    });
    mainMenuButton.setOnAction(e -> {
      if (GameLogic.getGameMode() == GameMode.Multiplayer) {
        NetworkClient.disconnect();
      }
      SceneManager.showScene("menu.fxml");
    });
  }
}
