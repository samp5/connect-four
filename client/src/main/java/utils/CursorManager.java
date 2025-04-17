package utils;

import javafx.geometry.Dimension2D;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class CursorManager {
  private static boolean enabled = false;

  private final static Dimension2D size = ImageCursor.getBestSize(32, 32);
  private final static Image handCursorScaled =
             new Image("/assets/hand_cursor.png",
                       size.getWidth(), size.getHeight(),
                       false, false);
  private final static ImageCursor handCursor =
             new ImageCursor(handCursorScaled,
                             handCursorScaled.getWidth()  / 2,
                             handCursorScaled.getHeight() / 2);
  private final static Image pointerCursorScaled =
             new Image("/assets/regular_cursor.png",
                       size.getWidth(), size.getHeight(),
                       false, false);
  private final static ImageCursor pointerCursor =
             new ImageCursor(pointerCursorScaled);

  public static void setEnabled(boolean enabled) {
    CursorManager.enabled = enabled;
  }

  public static void setHandCursor(Region ...regions) {
    if (!enabled) return;

    for (Region region : regions) {
      region.setCursor(handCursor);
    }
  }

  public static void setPointerCursor(Object ...objs) {
    if (!enabled) return;

    for (Object obj : objs) {
      if (obj instanceof Pane) {
        ((Pane) obj).setCursor(pointerCursor);
      } else if (obj instanceof Scene) {
        ((Scene) obj).setCursor(pointerCursor);
      }
    }
  }
}
