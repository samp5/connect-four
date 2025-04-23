package utils;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class CursorManager {
  private static boolean enabled = false;
  private static ArrayList<Region> wantsHandCursor = new ArrayList<>();
  private static ArrayList<Object> wantsPointer = new ArrayList<>();
  private final static Dimension2D size = ImageCursor.getBestSize(32, 32);
  private final static Image handCursorScaled = new Image("/assets/hand_cursor.png",
      size.getWidth(), size.getHeight(),
      false, false);
  private final static ImageCursor handCursor = new ImageCursor(handCursorScaled,
      handCursorScaled.getWidth() / 2,
      handCursorScaled.getHeight() / 2);
  private final static Image pointerCursorScaled = new Image("/assets/regular_cursor.png",
      size.getWidth(), size.getHeight(),
      false, false);
  private final static ImageCursor pointerCursor = new ImageCursor(pointerCursorScaled);

  public static void setEnabled(boolean enabled) {
    CursorManager.enabled = enabled;
    if (CursorManager.enabled) {
      wantsHandCursor.stream().filter(r -> {
        return r != null;
      }).forEach(r -> {
        r.setCursor(handCursor);
      });
      wantsPointer.stream().filter(obj -> {
        return obj != null;
      }).forEach(obj -> {
        if (obj instanceof Pane) {
          ((Pane) obj).setCursor(pointerCursor);
        } else if (obj instanceof Scene) {
          ((Scene) obj).setCursor(pointerCursor);
        }
      });
    } else {
      wantsHandCursor.stream().filter(r -> {
        return r != null;
      }).forEach(r -> {
        r.setCursor(Cursor.HAND);
      });
      wantsPointer.stream().filter(obj -> {
        return obj != null;
      }).forEach(obj -> {
        if (obj instanceof Pane) {
          ((Pane) obj).setCursor(Cursor.DEFAULT);
        } else if (obj instanceof Scene) {
          ((Scene) obj).setCursor(Cursor.DEFAULT);
        }
      });
    }
  }

  public static void setHandCursor(Region... regions) {
    wantsHandCursor.addAll(Arrays.asList(regions));

    Cursor c = handCursor;
    if (!enabled) {
      c = Cursor.HAND;
    }

    for (Region region : regions) {
      region.setCursor(c);
    }
  }

  public static void setPointerCursor(Object... objs) {
    wantsPointer.addAll(Arrays.asList(objs));

    Cursor c = pointerCursor;
    if (!enabled) {
      c = Cursor.DEFAULT;
    }

    for (Object obj : objs) {
      if (obj instanceof Pane) {
        ((Pane) obj).setCursor(c);
      } else if (obj instanceof Scene) {
        ((Scene) obj).setCursor(c);
      }
    }
  }
}
