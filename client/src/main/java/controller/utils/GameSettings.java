package controller.utils;

import controller.SettingsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import utils.SceneManager;

public class GameSettings {
  public static void loadOnto(Pane onto) {
    if (SettingsController.isAttached()) {
      return;
    }

    try {
      FXMLLoader loader = new FXMLLoader(GameSettings.class.getResource("/fxml/settings.fxml"));
      Pane settings = loader.load();
      SettingsController settingsCTL = loader.getController();
      settingsCTL.attach(onto, settings);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
