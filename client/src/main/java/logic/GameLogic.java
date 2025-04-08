package logic;

import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for validating game logic and making moves
 */
public class GameLogic {
  private static final int ROWS = 6;
  private static final int COLS = 7;
  private final int[][] board = new int[6][7];
  private int currentPlayer = 1;

  public GameLogic() {
    for (int[] row : board) {
      Arrays.fill(row, 0);
    }
  }

  public void switchPlayer() {}

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



  public int currentPlayer() {
    return currentPlayer;
  };

  // apply player move to board
  public boolean placePiece(int row, int column, int player) {
    board[row][column] = player;
    return true;
  };

  // check win
  public boolean checkWin(int player) {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        if (board[row][col] == player &&
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
    currentPlayer = 1;
  }
}
