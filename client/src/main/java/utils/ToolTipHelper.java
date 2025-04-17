package utils;

import javafx.scene.control.Tooltip;

import javafx.util.Duration;

public class ToolTipHelper {
  public static Tooltip make(String text, Duration delay) {
    Tooltip tl = new Tooltip(text);
    tl.setShowDelay(delay);
    return tl;
  }

  public static Tooltip make(String text) {
    Tooltip tl = new Tooltip(text);
    tl.setShowDelay(Duration.millis(500));
    return tl;
  }
}
