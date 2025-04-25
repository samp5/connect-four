package utils;

import java.util.HashMap;

import javafx.scene.Node;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

public class AudioManager {
  private static MediaPlayer backgroundPlayer;
  private static CurrentlyPlaying currentlyPlaying = CurrentlyPlaying.NONE;
  private static double musicVolume = .5;
  private static double soundFXVolumeFactor = 1.0;

  private static enum CurrentlyPlaying {
    MAIN_THEME, BOSS_MUSIC, NONE
  };
  // we need to statically load these to ensure responsive play times
  private static HashMap<SoundEffect, MediaPlayer> soundEffectPlayers = new HashMap<>();
  static {
    SoundEffect[] effects = SoundEffect.values();
    for (int i = 0; i < effects.length; i++) {
      MediaPlayer player_i = new MediaPlayer(new Media(asURIString(effects[i].toFileName())));
      soundEffectPlayers.put(effects[i], player_i);
      player_i.setVolume(effects[i].volume());
    }
  }

  public static enum SoundEffect {
    BUTTON_DOWN, BUTTON_UP, CHIP_DROP, TRASH, CHAT_SENT, CHAT_RECIEVED, WIN, LOSE;

    public String toFileName() {
      return "/assets/sounds/" + this.toString().toLowerCase() + ".wav";
    }

    public void play() {
      MediaPlayer mp = soundEffectPlayers.get(this);
      if (mp.getStatus() == Status.PLAYING) mp.seek(Duration.ZERO);
      mp.setOnStopped(new Thread(() -> {
        mp.seek(Duration.ZERO);
      }));

      mp.play();
    }

    public double volume() {
      switch (this) {
        case BUTTON_DOWN:
        case BUTTON_UP:
        case CHIP_DROP:
          return 1.0;
        case CHAT_RECIEVED:
        case CHAT_SENT:
        case TRASH:
          return 0.4;
        case LOSE:
          return 0.4;
        case WIN:
          return 0.2;
        default:
          return 0.0;
      }

    }

  }
  
  public static void playMainTheme() {
    switch (currentlyPlaying) {
		case BOSS_MUSIC:
      backgroundPlayer.stop();
			break;
		case MAIN_THEME:
			return;
		case NONE:
		default:
			break;
    }

    currentlyPlaying = CurrentlyPlaying.MAIN_THEME;
    playContinuous("main_theme.mp3");
  }

  public static void playBossMusic() {
    switch (currentlyPlaying) {
		case BOSS_MUSIC:
			return;
		case MAIN_THEME:
      backgroundPlayer.stop();
			break;
		case NONE:
		default:
			break;
    }

    currentlyPlaying = CurrentlyPlaying.BOSS_MUSIC;
    playContinuous("boss_music.mp3");
  }

  private static void playContinuous(String file) {
    try {
      Media m = new Media(
          AudioManager.class.getResource("/assets/sounds/" + file).toExternalForm().toString());
      backgroundPlayer = new MediaPlayer(m);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      backgroundPlayer.setVolume(musicVolume);
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

  public static void setAudioButton(Node... buttons) {
    for (Node btn : buttons) {
      var curDown = btn.getOnMousePressed();
      var curUp = btn.getOnMouseReleased();

      btn.setOnMousePressed(e -> {
        playSoundEffect(SoundEffect.BUTTON_DOWN);
        if (curDown != null)
          curDown.handle(e);
      });

      btn.setOnMouseReleased(e -> {
        playSoundEffect(SoundEffect.BUTTON_UP);
        if (curUp != null)
          curUp.handle(e);
      });
    }
  }

  /**
   * @param volume requested volume needs to be between 0.0 and 1.0
   */
  public static void setVolume(double volume) {
    musicVolume = volume;
    if (backgroundPlayer != null) {
      backgroundPlayer.setVolume(volume);
    }
  }

  /**
   * @param volume requested volume needs to be between 0.0 and 1.0
   */
  public static void setSoundEffectVolume(double volume) {
    soundFXVolumeFactor = volume;
    soundEffectPlayers.forEach((effect, player) -> {
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
