package registry;

import java.io.Serializable;
import java.util.HashSet;

import network.Player;
import network.UserProfile;
import network.Message.WinType;
import network.UserProfile.ProfilePicture;

class RegistryPlayer implements Serializable {
  Long id;
  String username, password;
  PlayerStats stats;
  HashSet<Long> friends;
  ProfilePicture profilePicture;

  public RegistryPlayer(String username, String password, Long id) {
    this.username = username;
    this.password = password;
    this.id = id;
    this.stats = new PlayerStats();
    this.friends = new HashSet<>();

    if (username.equals("uaq")) {
      this.profilePicture = ProfilePicture.UAQ;
    } else {
      this.profilePicture = ProfilePicture.BASIC_BLUE;
    }
  }

  public Player getClientPlayer() {
    return new Player(username, id);
  }

  public class PlayerStats implements Serializable {
    int gamesWon;
    int gamesTied;
    int gamesLost;
    int gamesPlayed;
    int globalRank;
  }

  public void completeGame(WinType type) {
    stats.gamesPlayed += 1;

    switch (type) {
      case WIN:
        stats.gamesWon += 1;
        break;
      case DRAW:
        stats.gamesTied += 1;
        break;
      case LOSE:
        stats.gamesLost += 1;
        break;
      default:
        break;
    }
  }

  public UserProfile asUserProfile() {

    return new UserProfile(id, this.username, friends, this.stats.gamesWon, this.stats.gamesTied, this.stats.gamesLost,
        this.stats.gamesPlayed, Leaderboard.getElo(id), this.profilePicture, PlayerRegistry.playerIsOnline(this.id));
  }

  public int getGamesWon() {
    return this.stats.gamesWon;
  }

  public int getGamesLost() {
    return this.stats.gamesLost;
  }

  public double getWinPercentage() {
    return (float) this.stats.gamesWon / (float) this.stats.gamesPlayed * 100.0;
  }

  public int getRank() {
    return stats.globalRank;
  }

  public Long getID() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public HashSet<Long> getFriendIDs() {
    return friends;
  }
}
