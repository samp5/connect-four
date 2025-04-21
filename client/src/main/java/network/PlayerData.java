package network;

import java.util.ArrayList;
import java.util.Optional;

import network.UserProfile.ProfilePicture;

// this class manages the currently logged in player's cached data.
public class PlayerData {
  private static final Object friendsLock = new Object();
  private static ArrayList<UserProfile> friendsList;
  private static boolean friendsDirty = false;
  private static final Object profileLock = new Object();
  private static UserProfile profile;
  private static boolean profileDirty = false;
  private static ArrayList<Runnable> onProfileRecieve = new ArrayList<>();
  private static ArrayList<Runnable> onFriendsRecieve = new ArrayList<>();

  // Get the friends list of this player if it is valid, otherwise, pass a
  // runnable that will be called once the list is valid
  public static Optional<ArrayList<UserProfile>> getFriends(Runnable onRecieve) {
    synchronized (friendsLock) {
      if (friendsDirty || friendsList == null) {
        NetworkClient.fetchFriends();
        onFriendsRecieve.add(onRecieve);
        return Optional.empty();
      }
      return Optional.of(friendsList);
    }
  }

  // Get the profile of this player if it is valid, otherwise, pass a
  // runnable that will be called once the profile is valid
  public static Optional<UserProfile> getProfile(Runnable onRecieve) {
    synchronized (friendsLock) {
      if (profileDirty || profile == null) {
        NetworkClient.fetchProfile();
        onProfileRecieve.add(onRecieve);
        return Optional.empty();
      }

      return Optional.of(profile);
    }
  }

  public static void recieveProfile(UserProfile profile) {
    synchronized (profileLock) {
      PlayerData.profileDirty = false;
      PlayerData.profile = profile;
      onProfileRecieve.stream().forEach(r -> r.run());
      onProfileRecieve.clear();
    }
  }

  public static void recieveFriends(ArrayList<UserProfile> friends) {
    synchronized (friendsLock) {
      PlayerData.friendsDirty = false;
      PlayerData.friendsList = friends;
      onFriendsRecieve.stream().forEach(r -> r.run());
      onFriendsRecieve.clear();
    }
  }

  public static void friendsUpdated() {
    synchronized (friendsLock) {
      PlayerData.friendsDirty = true;
    }
    NetworkClient.fetchFriends();
  }

  public static void profileUpdated() {
    synchronized (profileLock) {
      PlayerData.profileDirty = true;
    }
    NetworkClient.fetchProfile();
  }

  public static void friendOnlineStatus(String username, boolean isOnline) {
    synchronized (friendsLock) {
      friendsList.stream().filter(up -> up.getUserName().equals(username)).forEach(up -> up.setIsOnline(isOnline));
    }
  }

  public static void updateProfilePicture(ProfilePicture newPic) {
    synchronized (profileLock) {
      profile.profilePicture = newPic;
      profileDirty = true;
    }
    NetworkClient.updateProfilePicture(newPic);
  }

  public static void reset() {

    synchronized (profileLock) {
      synchronized (friendsLock) {
        profile = null;
        profileDirty = false;
        onProfileRecieve.clear();

        friendsList = null;
        onFriendsRecieve.clear();
        friendsDirty = false;
      }
    }
  }
}
