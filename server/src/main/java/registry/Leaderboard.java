package registry;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import network.Message.WinType;

public class Leaderboard {
  private static final int MAX_GAIN = 25;
  // id -> elo
  private static HashMap<Long, Double> trackedElo = new HashMap<>();
  private static ArrayList<LeaderboardEntry> leaderboard = new ArrayList<>();

  public static void updateElo(Long idA, Long idB, WinType outcomeA) {
    // get the current elo
    Double eloA = trackedElo.get(idA);
    Double eloB = trackedElo.get(idB);
    if (eloA == null) {
      eloA = 800.;
    }
    if (eloB == null) {
      eloB = 800.;
    }

    // find the expected scores. note eA + eB = 1
    double expectedA = calcExpectedScore(eloA, eloB);
    double expectedB = 1 - expectedA;

    // get the actual score from the game outcome
    double actualA, actualB;
    switch (outcomeA) {
		case WIN:
      actualA = 1;
      actualB = 0;
			break;
		case DRAW:
      actualA = 0.5;
      actualB = 0.5;
			break;
		case LOSE:
      actualA = 0;
      actualB = 1;
			break;
		default:
      actualA = 0;
      actualB = 0;
			break;
    }

    // calc the new elo
    Double newEloA = eloA + (MAX_GAIN * (actualA - expectedA));
    Double newEloB = eloB + (MAX_GAIN * (actualB - expectedB));

    // replace old elo with new elo
    trackedElo.put(idA, newEloA);
    trackedElo.put(idB, newEloB);

    // update the leaderboard
    LeaderboardEntry currentA = leaderboard.stream().filter(o -> o.id == idA).findFirst().get();
    LeaderboardEntry currentB = leaderboard.stream().filter(o -> o.id == idB).findFirst().get();

    if (currentA == null) {
      currentA = new LeaderboardEntry(idA, newEloA);
      leaderboard.add(currentA);
    } else {
      currentA.elo = newEloA;
    }
    if (currentB == null) {
      currentB = new LeaderboardEntry(idB, newEloB);
      leaderboard.add(currentB);
    } else {
      currentB.elo = newEloB;
    }

    // sort the leaderboard to have highest elo first
    Collections.sort(leaderboard);
  }

  private static double calcExpectedScore(Double eloA, Double eloB) {
    double exp = (eloB - eloA) / 400;
    double denom = 1 + Math.pow(10, exp);
    return 1. / denom;
  }

  public static List<LeaderboardEntry> getTopN(int n) {
    return leaderboard.subList(0, Math.min(n, leaderboard.size()));
  }

  public static List<LeaderboardEntry> getNAroundPlayer(Long playerId, int n) {
    LeaderboardEntry entry = leaderboard.stream().filter(o -> o.id == playerId).findFirst().get();
    if (entry == null) return null;

    int ndx = leaderboard.indexOf(entry);
    int ndxTop = Math.min(0, ndx - Math.ceilDiv(n, 2));
    int ndxBot = ndxTop + n;

    return leaderboard.subList(ndxTop, Math.min(n, ndxBot));
  }

  public static List<LeaderboardEntry> getPlayerFriendsBoard(RegistryPlayer player) {
    return leaderboard.stream().filter(o -> player.friends.contains(o.id)).toList();
  }

  /**
   * Save the leaderboard to a file
   */
  public static void save() {
    try {

    FileOutputStream fileout = new FileOutputStream("leaderboard.registry");
    ObjectOutputStream objectout = new ObjectOutputStream(fileout);

    objectout.writeObject(trackedElo);
    objectout.writeObject(leaderboard);
    objectout.close();

    } catch (IOException e) {

      e.printStackTrace();
      System.out.println("Leaderboard had an exception on save. Leaderboard not saved.");

    }
  }

  /**
   * Load the leaderboard from a file
   */
  public static void load() {
    try {

      FileInputStream filein = new FileInputStream("leaderboard.registry");
      ObjectInputStream objectin = new ObjectInputStream(filein);

      Object obj = objectin.readObject();
      if (obj instanceof HashMap) {
        trackedElo = (HashMap<Long, Double>) obj;
      }
      obj = objectin.readObject();
      if (obj instanceof ArrayList) {
        leaderboard = (ArrayList<LeaderboardEntry>) obj;
      }

      objectin.close();
    } catch (ClassNotFoundException e) {
      System.out.println("Error loading leaderboard. class not found:");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Leaderboard had an exception on Load. Leaderboard not loaded.");
    }
  }

  public static class LeaderboardEntry implements Comparable<LeaderboardEntry>, Serializable {
    Long id;
    Double elo;

    LeaderboardEntry(Long id, Double elo) {
      this.id = id;
      this.elo = elo;
    }

    public Long getId() {
      return id;
    }

    public Double getElo() {
      return elo;
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
      return -elo.compareTo(o.elo);
    }
  }
}
