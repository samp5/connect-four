package controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import utils.SceneManager.SceneSelections;

public class SettingsController extends Controller {
  @FXML
  Slider volumeSlider;
  @FXML
  Slider soundFXVolumeSlider;
  @FXML
  Slider aiDifficultySlider;
  @FXML
  Pane settingsPane;
  @FXML
  Button backButton;

  @FXML
  CheckBox customCursorToggle;

  @FXML
  Button mainMenuButton;
  private static boolean isAttached = false;

  private Pane parent;
  private Node root;

  private static Settings currentSettings;

  public static void save() {
    try {

      FileOutputStream fileout = new FileOutputStream("local.settings");
      ObjectOutputStream objectout = new ObjectOutputStream(fileout);

      objectout.writeObject(currentSettings);
      objectout.close();

    } catch (Exception e) {

      e.printStackTrace();
      System.out.println("Settings had an exception on Save. Settings not saved.");

    }
  }

  public static void load() {
    try {

      FileInputStream filein = new FileInputStream("local.settings");
      ObjectInputStream objectin = new ObjectInputStream(filein);

      Object obj = objectin.readObject();
      if (obj instanceof Settings) {
        currentSettings = (Settings) obj;
      }

      objectin.close();

      // apply settings
      AudioManager.setVolume(currentSettings.musicVolume);
      AudioManager.setSoundEffectVolume(currentSettings.sfxVolume);
      AI.setDifficulty(currentSettings.aiDifficulty);
      CursorManager.setEnabled(currentSettings.cursorsEnabled);

    } catch (FileNotFoundException e) {

      currentSettings = new Settings();

    } catch (Exception e) {

      e.printStackTrace();
      System.out.println("Settings had an exception on Load. Settings loaded as defaults.");
      currentSettings = new Settings();

    }
  }

  public static boolean isAttached() {
    return isAttached;
  }

  public void attach(Pane parent, Pane root) {
    if (isAttached) {
      return;
    }
    parent.getChildren().add(root);
    root.relocate((parent.getWidth() - 500) / 2,
        (parent.getHeight() - 500) / 2);
    root.toFront();
    this.parent = parent;
    this.root = root;
    isAttached = true;

    EventHandler<? super KeyEvent> curHandler = parent.getOnKeyReleased();
    root.requestFocus();
    parent.setOnKeyReleased((e) -> {
      if (isAttached && e.getCode() == KeyCode.ESCAPE) {
        parent.setOnKeyReleased(curHandler);
        detach();
        e.consume();
      } else if (curHandler != null) curHandler.handle(e);
    });
  }

  private void detach() {
    isAttached = false;
    parent.getChildren().remove(root);
  }

  public void initialize() {
    customCursorToggle.setSelected(currentSettings.cursorsEnabled);
    getSliderValues();
    setHandlers();
    styleElements();
  }

  private void getSliderValues() {
    if (currentSettings.musicVolume == null) {
      currentSettings.musicVolume = volumeSlider.getMax() * AudioManager.getVolume();
    }
    volumeSlider.setValue(currentSettings.musicVolume);
    AudioManager.setVolume(currentSettings.musicVolume);

    if (currentSettings.sfxVolume == null) {
      currentSettings.sfxVolume = soundFXVolumeSlider.getMax() * AudioManager.getSoundFXVolume();
    }
    soundFXVolumeSlider.setValue(currentSettings.sfxVolume);
    AudioManager.setSoundEffectVolume(currentSettings.sfxVolume);

    if (currentSettings.aiDifficulty == null) {
      currentSettings.aiDifficulty = AI.getDifficulty();
    }
    aiDifficultySlider.setValue(currentSettings.aiDifficulty);
    AI.setDifficulty(currentSettings.aiDifficulty);

    if (currentSettings.cursorsEnabled == null) {
      currentSettings.cursorsEnabled = false;
    }
    // set setting control here
    CursorManager.setEnabled(currentSettings.cursorsEnabled);
  }

  public static Background getButtonBackground(int dim) {
    return new Background(new BackgroundImage(new Image("/assets/settings-button.png"),
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
        new BackgroundSize(dim, dim, false, false, false, false)));
  }

  private void styleElements() {
    CursorManager.setHandCursor(backButton, volumeSlider, soundFXVolumeSlider, aiDifficultySlider, customCursorToggle, mainMenuButton);
    AudioManager.setAudioButton(backButton, mainMenuButton);
  }

  private void setHandlers() {
    volumeSlider.valueProperty().addListener((observable, old, newValue) -> {
      if (newValue != null) {
        currentSettings.musicVolume = newValue.doubleValue() / volumeSlider.getMax();
        AudioManager.setVolume(currentSettings.musicVolume);
      }
    });
    soundFXVolumeSlider.valueProperty().addListener((observable, old, newValue) -> {
      if (newValue != null) {
        currentSettings.sfxVolume = newValue.doubleValue() / soundFXVolumeSlider.getMax();
        AudioManager.setSoundEffectVolume(currentSettings.sfxVolume);
      }
    });
    aiDifficultySlider.valueProperty().addListener((observable, old, newValue) -> {
      if (newValue != null) {
        currentSettings.aiDifficulty = newValue.intValue();
        AI.setDifficulty(currentSettings.aiDifficulty);

        if (this.parent != null 
            && this.parent.getId().equals("foregroundPane")
            && GameLogic.getGameMode() == GameMode.LocalAI) {
          NetworkClient.checkAIMaxMode();
        }
      }
    });

    backButton.setOnAction(e -> {
      detach();
    });
    mainMenuButton.setOnAction(e -> {
      detach();
      if (GameLogic.getGameMode() == GameMode.Multiplayer) {
        NetworkClient.disconnect();
      }
      SceneManager.showScene(SceneSelections.MAIN_MENU);
      AudioManager.playMainTheme();
    });

    customCursorToggle.setOnAction(e -> {
      currentSettings.cursorsEnabled = customCursorToggle.isSelected();
      CursorManager.setEnabled(currentSettings.cursorsEnabled);
    });
  }

  private static class Settings implements Serializable {
    Double musicVolume;
    Double sfxVolume;
    Integer aiDifficulty;
    Boolean cursorsEnabled;

    Settings() {
      this.musicVolume = 0.4;
      this.sfxVolume = 0.4;
      this.aiDifficulty = 5;
      this.cursorsEnabled = false;
    }
  }
}
