package network;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Client facing analog of RegistryPlayer
 */
public class UserProfile implements Serializable {
  ProfilePicture profilePicture;

  boolean online;

  Long id;
  String userName;
  HashSet<Long> friends;
  int gamesWon;
  int gamesTied;
  int gamesLost;
  int gamesPlayed;
  double ELO;

  public double getElo() {
    return ELO;
  }

  public UserProfile(Long id, String userName, HashSet<Long> friends, int gamesWon, int gamesTied, int gamesLost,
      int gamesPlayed, double ELO, ProfilePicture picture, boolean online) {
    this.id = id;
    this.userName = userName;
    this.friends = friends;
    this.gamesWon = gamesWon;
    this.gamesTied = gamesTied;
    this.gamesLost = gamesLost;
    this.gamesPlayed = gamesPlayed;
    this.ELO = ELO;
    this.profilePicture = picture;
    this.online = online;
  }

  public enum ProfilePicture {
    BASIC_RED, BASIC_BLUE, MUSCLE_RED, MUSCLE_BLUE, ANGEL_BLUE, DEVIL_RED, SCUBA_BLUE, SUPER_SAIYAN_RED, EVAN_MCCARTY;

    public String getAssetFileName() {
      return "/assets/" + this.toString().toLowerCase() + ".png";
    }

    public String toDisplayString() {
      switch (this) {
        case ANGEL_BLUE:
          return "Angel";
        case BASIC_BLUE:
          return "Basic Blue";
        case BASIC_RED:
          return "Basic Red";
        case DEVIL_RED:
          return "Chip Nightmare";
        case MUSCLE_BLUE:
          return "Anti-red";
        case MUSCLE_RED:
          return "Anti-blue";
        case SUPER_SAIYAN_RED:
          return "SUPER SAIYAN";
        case SCUBA_BLUE:
          return "ScubaBlue";
        case EVAN_MCCARTY:
          return "Evan McCarty";
      }
      return "";
    }
  }

  public Long getId() {
    return id;
  }

  public String getUserName() {
    return userName;
  }

  public HashSet<Long> getFriends() {
    return friends;
  }

  public int getGamesWon() {
    return gamesWon;
  }

  public int getGamesTied() {
    return gamesTied;
  }

  public int getGamesLost() {
    return gamesLost;
  }

  public int getGamesPlayed() {
    return gamesPlayed;
  }

  public ProfilePicture getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(ProfilePicture profilePicture) {
    this.profilePicture = profilePicture;
  }

  public boolean isOnline() {
    return online;
  }

  public void setIsOnline(boolean isOnline) {
    online = isOnline;
  }
}
