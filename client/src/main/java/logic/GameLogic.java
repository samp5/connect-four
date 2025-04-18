package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import controller.utils.BoardPosition;
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
  private final ArrayList<Integer> moveHistory = new ArrayList<>();
  private static PlayerRole currentPlayerRole = PlayerRole.PlayerOne;
  private static GameMode mode = GameMode.None;

  public int[][] getBoard() {
    return board.clone();
  }

  public enum GameMode {
    LocalMultiplayer, LocalAI, Multiplayer, None;

    public boolean canMove(GameLogic currentState, PlayerRole chipPile) {
      switch (this) {
        case LocalAI:
          // A player can move in local AI mode when
          // 1. the current player is player one
          // 2. they are dragging from their pile
          return currentState.getCurrentPlayerRole() == PlayerRole.PlayerOne
              && currentState.getCurrentPlayerRole() == chipPile;
        case LocalMultiplayer:
          // A player can move in local multiplayer mode when
          // 1. they are the current player and the chip pile belongs to them
          return chipPile == currentState.getCurrentPlayerRole();
        case Multiplayer:
          // A player can move in multiplayer mode when
          // 1. they are the local player and,
          // 2. they are dragging from their pile
          return GameLogic.getLocalPlayer().getRole() == currentState.getCurrentPlayerRole()
              && GameLogic.getLocalPlayer().getRole() == chipPile;
        case None:
        default:
          return false;
      }

    }
  }

  public static void setGameMode(GameMode mode) {
    GameLogic.mode = mode;
    if (mode == GameMode.LocalAI) {
      AI.setRole(PlayerRole.PlayerTwo);
      localPlayer = new Player("", 0L);
    }
  }

  public static GameMode getGameMode() {
    return mode;
  }

  public static void setLocalPlayer(Player p) {
    localPlayer = p;
  }

  public static void setRemotePlayer(Player p) {
    remotePlayer = p;
  }

  public static void setCurrentPlayerRole(PlayerRole p) {
    currentPlayerRole = p;
  }

  public static void initialize(Player local, Player remote, PlayerRole localRole) {
    localPlayer = local;
    remotePlayer = remote;

    localPlayer.setRole(localRole);
    if (localRole == PlayerRole.PlayerOne) {
      remotePlayer.setRole(PlayerRole.PlayerTwo);
    } else {
      remotePlayer.setRole(PlayerRole.PlayerOne);
    }
  }

  public GameLogic() {
    reset();
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
    switch (currentPlayerRole) {
      case None:
        break;
      case PlayerOne:
        currentPlayerRole = PlayerRole.PlayerTwo;
        break;
      case PlayerTwo:
        currentPlayerRole = PlayerRole.PlayerOne;
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

  public ArrayList<Integer> getMoveHistory() {
    return this.moveHistory;
  }

  public static int numRows() {
    return ROWS;
  }

  public static int numCols() {
    return COLS;
  }

  public PlayerRole getCurrentPlayerRole() {
    return currentPlayerRole;
  };

  public Player getCurrentPlayer() {
    if (getLocalPlayer().getRole() == currentPlayerRole) {
      return localPlayer;
    } else {
      return remotePlayer;
    }
  }

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
    moveHistory.add(column);
  };

  // check win
  public Optional<BoardPosition[]> checkWin(PlayerRole player) {
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
        if (board[row][col] == playerID) {
          // horizontal
          if (checkDirection(row, col, 1, 0)) {

            return Optional.of(buildWinningArray(row, col, 1, 0));

          } else if (checkDirection(row, col, 0, 1)) { // columns

            return Optional.of(buildWinningArray(row, col, 0, 1));

          } else if (checkDirection(row, col, 1, 1)) { // diagonal /

            return Optional.of(buildWinningArray(row, col, 1, 1));

          } else if (checkDirection(row, col, 1, -1)) { // diagonal \

            return Optional.of(buildWinningArray(row, col, 1, -1));

          }
        }
      }
    }
    return Optional.empty(); // No winner
  }

  private BoardPosition[] buildWinningArray(int row, int col, int dRow, int dCol) {
    BoardPosition[] ret = new BoardPosition[4];
    for (int i = 0; i < 4; i++) {
      int r = row + i * dRow;
      int c = col + i * dCol;
      ret[i] = new BoardPosition(r, c);
    }
    return ret;

  }

  // TODO: Make this a clever algorithm that detects stalemates for non-full
  // boards
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
    currentPlayerRole = PlayerRole.PlayerOne;
    moveHistory.clear();
  }

}
