package logic;

import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for validating game logic and making moves
 */
public class GameLogic {
  private final int[][] board = new int[6][7];
  private int currentPlayer = 1;

  public void switchPlayer() {}

  public Optional<Integer> getAvailableRow(int col) {
    return Optional.empty();
  }



  // apply player move to board
  public boolean placePiece(int row, int column, int player) {
    return false;
  };

  // check win
  public boolean checkWin(int player) {
    return true;
  };


  public void reset() {
    for (int[] row : board) {
      Arrays.fill(row, 0);
    }
    currentPlayer = 1;
  }
}
