package utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {
  private static MediaPlayer backgroundPlayer;

  public static enum SoundEffect {
    ButtonPress,
    Selection,
    ChipDrop,
    ChatSent,
    ChatRecieved,
    Win,
    Loss,
  }

  public static void playContinuous(String file) {
    try {
      Media m = new Media(
          AudioManager.class.getResource("/assets/sounds/" + file).toExternalForm().toString());
      backgroundPlayer = new MediaPlayer(m);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      backgroundPlayer.setVolume(0.5);
      backgroundPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void play(String file) {
    try {
      Media m = new Media(AudioManager.class.getResource("assets/sounds/" + file).toURI().toString());
      MediaPlayer player = new MediaPlayer(m);
      player.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void playSoundEffect(SoundEffect effect) {
    String file = "";
    switch (effect) {
      case ButtonPress:
        break;
      case ChipDrop:
        break;
      case Selection:
        break;
      case Win:
        break;
      case ChatRecieved:
        break;
      case ChatSent:
        break;
      case Loss:
        break;
    }
    if (file != "") {
      play(file);
    }
  }

  /**
   * @param volume requested volume needs to be between 0.0 and 1.0
   */
  public static void setVolume(double volume) {
    if (backgroundPlayer != null) {
      backgroundPlayer.setVolume(volume);
    }
  }

  public static double getVolume() {
    if (backgroundPlayer != null) {
      return backgroundPlayer.getVolume();
    } else {
      return 0.0;
    }
  }
}
