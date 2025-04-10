package logic;

import java.util.Arrays;
import java.util.Optional;
import network.Player;
import network.Player.PlayerRole;

/**
 * Utility class for validating game logic and making moves
 */
public class GameLogic {
  private static Player localPlayer;
  private static Player remotePlayer;
  private static final int ROWS = 6;
  private static final int COLS = 7;
  private final int[][] board = new int[6][7];
  private static PlayerRole currentPlayer = PlayerRole.PlayerOne;

  // TODO: Make sure that the registration message initializes this in GameLogic
  public static void setLocalPlayer(Player p) {
    localPlayer = p;
  }

  // TODO: Make sure that the registration message initializes this in GameLogic
  public static void setCurrentPlayer(PlayerRole p) {
    currentPlayer = p;
  }

  // TODO: Make sure that the registration message initializes this in GameLogic
  public static void setRemotePlayer(Player p) {
    remotePlayer = p;
  }

  public static void initialize(Player local, Player remote, PlayerRole startingPlayer) {
    localPlayer = local;
    remotePlayer = remote;
    currentPlayer = startingPlayer;
  }

  public GameLogic() {
    for (int[] row : board) {
      Arrays.fill(row, 0);
    }
  }

  public static Player getLocalPlayer() {
    return localPlayer;
  }

  public static Player getRemotePlayer() {
    return remotePlayer;
  }

  public static void setLocalPlayerRole(PlayerRole p) {
    localPlayer.setRole(p);
  }

  public void switchPlayer() {
    switch (currentPlayer) {
      case None:
        break;
      case PlayerOne:
        currentPlayer = PlayerRole.PlayerTwo;
        break;
      case PlayerTwo:
        currentPlayer = PlayerRole.PlayerOne;
        break;
      default:
        break;
    }
  }

  public Optional<Integer> getAvailableRow(int col) {
    if (col < 0 || col >= COLS) {
      return Optional.empty();
    }
    for (int i = 0; i < ROWS; i++) {
      int[] r_i = board[i];
      if (r_i[col] == 0) {
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }

  public static int numRows() {
    return ROWS;
  }

  public static int numCols() {
    return COLS;
  }



  public PlayerRole getCurrentPlayer() {
    return currentPlayer;
  };

  // apply player move to board
  public void placePiece(int row, int column, PlayerRole player) {
    switch (player) {
      case PlayerOne:
        board[row][column] = 1;
        break;
      case PlayerTwo:
        board[row][column] = 2;
        break;
      default:
        break;
    }
  };

  // check win
  public boolean checkWin(PlayerRole player) {
    int playerID;
    switch (player) {
      case PlayerOne:
        playerID = 1;
        break;
      case PlayerTwo:
        playerID = 2;
        break;
      case None:
      default:
        playerID = -1;
        break;
    }
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        if (board[row][col] == playerID &&
            (checkDirection(row, col, 1, 0) || // horizontal
                checkDirection(row, col, 0, 1) || // vertical
                checkDirection(row, col, 1, 1) || // diagonal /
                checkDirection(row, col, 1, -1))) { // diagonal \
          return true;
        }
      }
    }
    return false; // No winner
  }

  // TODO: Make this a clever algorithm that detects stalemates for non-full boards
  public boolean staleMate() {
    for (int[] row : board) {
      for (int val : row) {
        if (val == 0) {
          return false;
        }
      }
    }
    return true;
  }

  // Check if there is a winning line in a given direction
  private boolean checkDirection(int row, int col, int dRow, int dCol) {
    int count = 0;
    int player = board[row][col];
    for (int i = 0; i < 4; i++) {
      int r = row + i * dRow;
      int c = col + i * dCol;
      if (r < 0 || r >= 6 || c < 0 || c >= 7 || board[r][c] != player) {
        return false;
      }
      count++;
    }
    return count == 4; // If we've found 4 in a row
  }


  public void reset() {
    for (int[] row : board) {
      Arrays.fill(row, 0);
    }
    currentPlayer = PlayerRole.PlayerOne;
  }

}
