import network.ClientManager;

/**
 * Main Class.
 * likely should not be referenced.
 */
public class Main {
  public static void main(String[] args) {
    ClientManager.connectToClients();

    redirectLoop();
  }

  private static void redirectLoop() {
    while (true);
  }
}
