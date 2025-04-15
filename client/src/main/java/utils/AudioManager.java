package utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {
  private static MediaPlayer backgroundPlayer;

  public static void playContinuous(String file) {
    try {
      Media m =
          new Media(
              AudioManager.class.getResource("/assets/sounds/" + file).toExternalForm().toString());
      backgroundPlayer = new MediaPlayer(m);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      backgroundPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void play(String file) {
    try {
      Media m =
          new Media(AudioManager.class.getResource("assets/sounds/" + file).toURI().toString());
      MediaPlayer player = new MediaPlayer(m);
      player.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
