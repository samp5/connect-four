package utils;

import java.util.HashMap;

import javax.sound.midi.SysexMessage;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioManager {
  private static MediaPlayer backgroundPlayer;
  private static double soundFXVolumeFactor = 1.0;

  // we need to statically load these to ensure responsive play times
  private static HashMap<SoundEffect, MediaPlayer> soundEFfectPlayers = new HashMap<>();
  static {
    SoundEffect[] effects = SoundEffect.values();
    for (int i = 0; i < effects.length; i++) {
      MediaPlayer player_i = new MediaPlayer(new Media(asURIString(effects[i].toFileName())));
      soundEFfectPlayers.put(effects[i], player_i);
      player_i.setVolume(effects[i].volume());
    }
  }

  public static enum SoundEffect {
    BUTTON_PRESS, /* SELECTION, */ CHIP_DROP, CHAT_SENT, CHAT_RECIEVED, WIN/* , LOSE */;

    public String toFileName() {
      return "/assets/sounds/" + this.toString().toLowerCase() + ".wav";
    }

    public void play() {
      MediaPlayer mp = soundEFfectPlayers.get(this);
      mp.seek(mp.getStartTime());
      mp.play();
    }

    public double volume() {
      switch (this) {
        case CHIP_DROP:
          return 0.3;
        case BUTTON_PRESS:
        case CHAT_RECIEVED:
        case CHAT_SENT:
        case WIN:
          return 0.4;
        default:
          return 0.0;
      }

    }

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
      String filepath = AudioManager.class.getResource("/assets/sounds/" + file).toURI().toString();
      Media m = new Media(filepath);
      MediaPlayer player = new MediaPlayer(m);
      player.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String asURIString(String path) {
    try {
      return AudioManager.class.getResource(path).toURI().toString();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(path);
      return "error";
    }
  }

  public static void playSoundEffect(SoundEffect effect) {
    effect.play();
  }

  /**
   * @param volume requested volume needs to be between 0.0 and 1.0
   */
  public static void setVolume(double volume) {
    if (backgroundPlayer != null) {
      backgroundPlayer.setVolume(volume);
    }
  }

  /**
   * @param volume requested volume needs to be between 0.0 and 1.0
   */
  public static void setSoundEffectVolume(double volume) {
    soundFXVolumeFactor = volume;
    soundEFfectPlayers.forEach((effect, player) -> {
      player.setVolume(soundFXVolumeFactor * effect.volume());
    });
  }

  public static double getVolume() {
    if (backgroundPlayer != null) {
      return backgroundPlayer.getVolume();
    } else {
      return 0.0;
    }
  }

  public static double getSoundFXVolume() {
    return soundFXVolumeFactor;
  }
}
