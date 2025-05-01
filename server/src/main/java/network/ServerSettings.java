package network;

import java.io.FileNotFoundException;
import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tracks all players: logged in, playing or not.
 */
public class ServerSettings {
  public static boolean showAnimation;
  public static int PORT;

  /**
   * Load settings from JSON
   */
  public static void load() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(new File("settings.json"));
      JsonNode animate = node.get("server_display");
      if (animate == null) {
        System.err.println("Could not find key \"server_display\" in settings.json, using false");
        showAnimation = false;
      } else {
        showAnimation = animate.asBoolean();
      }

      JsonNode port = node.get("port");

      if (port == null) {
        System.err.println("Could not find key \"port\" in settings.json, using 8000");
        PORT = 8000;
      } else {
        PORT = port.asInt();
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("ServerSettings had an exception on Load. Using default settings.");
      PORT = 8000;
      showAnimation = false;
    }
  }
}
