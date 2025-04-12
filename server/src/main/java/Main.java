import network.ClientManager;
import registry.PlayerRegistry;

/**
 * Main Class.
 * likely should not be referenced.
 */
public class Main {
  public static void main(String[] args) {
    // clear the screen, print "title"
    System.out.println("\033[2J\033[H\033[1;35;40m~~Connect-4 Server~~\033[0m");

    // save registry on exit, and load on startup
    Runtime.getRuntime().addShutdownHook(new Thread(() -> PlayerRegistry.save()));
    PlayerRegistry.load();

    ClientManager.connectToClients();
  }
}
