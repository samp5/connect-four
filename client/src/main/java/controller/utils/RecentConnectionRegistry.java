package controller.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class RecentConnectionRegistry {
  private static ArrayList<RecentConnection> connections = new ArrayList<>();

  public static ArrayList<RecentConnection> getConnections() {
    Collections.sort(connections);
    return connections;
  }

  public static Optional<RecentConnection> getMostRecentConnection() {
    Collections.sort(connections);
    if (connections.size() > 0) {
      return Optional.of(connections.getFirst());
    } else {
      return Optional.empty();
    }
  }

  public static void setConnections(ArrayList<RecentConnection> c) {
    connections = c;
  }

  public static void add(RecentConnection c) {
    connections.add(c);
  }

  public static void remove(RecentConnection c) {
    connections.remove(c);
  }

  public static void sort() {
    Collections.sort(connections);
  }

  /**
   * Save the registry to a file
   */
  public static void save() {
    try {
      FileOutputStream fileout = new FileOutputStream("connections.registry");
      ObjectOutputStream objectout = new ObjectOutputStream(fileout);
      objectout.writeObject(connections);
      objectout.close();
    } catch (IOException e) {
    }
  }

  /**
   * Load the registry from a file
   */
  @SuppressWarnings("unchecked")
  public static void load() {
    try {
      FileInputStream filein = new FileInputStream("connections.registry");
      ObjectInputStream objectin = new ObjectInputStream(filein);
      Object obj = objectin.readObject();
      if (obj instanceof ArrayList) {
        connections = (ArrayList<RecentConnection>) obj;
      }
      objectin.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.out.println("Error loading recent connections.");
      e.printStackTrace();
    }
  }

}
