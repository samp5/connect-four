import java.io.FileNotFoundException;
import java.io.PrintStream;

import network.ClientManager;
import network.ServerSettings;
import registry.PlayerRegistry;
import registry.Leaderboard;

/**
 * Main Class.
 * likely should not be referenced.
 */
public class Main {
  public static void main(String[] args) {
    try {
      PrintStream logFile = new PrintStream("errs.log");
      System.setErr(logFile);
    } catch (FileNotFoundException fnf) {
      fnf.printStackTrace();
    }

    // clear the screen, print "title"
    System.out.println("\033[2J\033[H\033[1;35;40m~~Connect-4 Server~~\033[0m");

    // save registry on exit, and load on startup
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      PlayerRegistry.save();
      Leaderboard.save();
    }));
    PlayerRegistry.load();
    Leaderboard.load();
    ServerSettings.load();

    ClientManager.connectToClients();
  }
}
