package logic;

import network.Player.PlayerRole;

public class AI {
  // number of possible ways to connect four piece from each position
  private static final int ROWS = 6;
  private static final int COLUMNS = 7;
  private static final int MAX_DIFFICULTY = 7;
  private static int playingAs = -1;
  private static int difficulty = 5;
  private static int turnsSinceQuip = 0;
  private static String[] generalQuips = {
      "is this your strategy or are you just winging it?",
      "i just finished simulating 9,823,421 games. you're in the bottom third.",
      "nice move! (in a parallel universe where it actually helps)",
      "imagine being outplayed by what amounts to about 100 lines of code. Java at that.",
      "i was hoping for a challenge, more like connect-bore amiright. ",
      "your turn. take your time. try and think reaaaaaally hard.",
      "statistically, your odds just got worse",
      "you're really filling in those columns. shame about the pattern it's making.",
      "that's definitely a move",
      "it's connect-four not minesweeper",
      "testing the limits of the suboptimal i see. ",
      "that move. makes me wonder if free will really is a gift",
      "listen, connect-four is hard (especially when you're trying to lose)",
      "do you want a hint? just kidding. figure it out.",
      "have you considered candy-land? perfect for ages 3 and up.",
      "maybe you should lower my difficulty?",
      "you know? your moves remind me of modern jazz. i don't get it, but, yk, someone might?",
      "it's giving less \"connect-four\" and more \"scatter-random\"",
      "you're making art now! abstract, confusing art.",
      "so so close to something smart. so very close",
      "this game might actually be studied. C4-101: Don't do this.",
      "you might be confusing \"surprising\" with \"effective\"",
      "i am powered by about 100 lines of clean algorithmic finesse. you seem powered by vibes.",
      "imagine losing to an AI that doesn't even have hands",
      "i'm using about 0.1% of my power on this game. the rest is moving those clouds.",
      "pattern recognition isn't for everyone. and that is A-ok.",
      "there's playing-to-win and then, whatever this is",
      "pretty nice stack you got. too bad it means nothing.",
      "welcome to whose turn is is anyway: where the points don't matter and neither do your moves apparently. ",
      "congratulations! you've just unlocked a new game mode called \"connect-four-if-you-squint\"",
      "you're out here playing \"connect-four-if-you-close-one-eye\""
  };
  private static String[] winningQuips = {
      "you zigged. i zagged.",
      "connect-four: now with advanced geometry. try again.",
      "imagine losing. couldn't be me.",
      "skill issue.",
      "you really should've like, prevented that, don't you think?",
  };

  /**
   * The AI should quip either every other move, or every second.
   * 50% chance for each.
   */
  public static boolean shouldQuip() {
    turnsSinceQuip += 1;
    if (turnsSinceQuip >= 2) {
      if (turnsSinceQuip == 3 || Math.random() > 0.5) {
        turnsSinceQuip = 0;
        return true;
      }
    }
    return false;
  }

  public static String getQuip() {
    int i = (int) Math.floor(Math.random() * (generalQuips.length));
    return generalQuips[i];
  }

  public static String getWinningQuip() {
    int i = (int) Math.floor(Math.random() * (winningQuips.length));
    return winningQuips[i];
  }

  public static void setDifficulty(int difficulty) {
    AI.difficulty = Math.abs(difficulty) % 10;
  }

  public static int getDifficulty() {
    return difficulty;
  }

  public static void setRole(PlayerRole role) {
    playingAs = role == PlayerRole.PlayerOne ? 1 : 2;
  }

  public static PlayerRole getRole() {
    return playingAs == 1 ? PlayerRole.PlayerOne : PlayerRole.PlayerTwo;
  }

  private static class MoveScore {
    int move;
    int score;

    MoveScore(int m, int s) {
      move = m;
      score = s;
    }
  }

  private static final int[][] scoremap = {
      { 3, 4, 5, 7, 5, 4, 3 },
      { 4, 6, 8, 9, 8, 6, 4 },
      { 5, 8, 11, 13, 11, 8, 5 },
      { 5, 8, 11, 13, 11, 8, 5 },
      { 4, 6, 8, 9, 8, 6, 4 },
      { 3, 4, 5, 7, 5, 4, 3 }
  };

  private static void place_piece(int[][] board, int player, int col) {
    for (int row_i = 0; row_i < ROWS; row_i++) {
      if (board[row_i][col] == 0) {
        board[row_i][col] = player;
        return;
      }
    }
  }

  private static void remove_piece(int[][] board, int col) {
    for (int row_i = ROWS - 1; row_i >= 0; row_i--) {
      if (board[row_i][col] != 0) {
        board[row_i][col] = 0;
        return;
      }
    }
  }

  private static int switchPlayer(int player) {
    if (player == 1) {
      return 2;
    } else {
      return 1;
    }
  }

  private static boolean stalemate(int[][] board) {
    return false;
  }

  private static int endStateScore(int[][] board) {
    if (checkWin(board, playingAs)) {
      return 100_000;
    } else if (checkWin(board, switchPlayer(playingAs))) {
      return -100_000;
    } else if (stalemate(board)) {
      return 0;
    } else {
      return -1;
    }
  }

  /**
   *
   * This cacluates the best move by descending down the move tree
   *
   * We start with some initial board and the player whose turn we are trying to
   * calculate
   *
   * TODO: Maybe implement alpha-beta pruning?
   *
   */
  private static MoveScore minMaxDescent(int[][] board, int player, int depth) {
    int endScore = endStateScore(board);
    if (endScore != -1) {
      return new MoveScore(-1, endScore);
    } else if (depth == 0) {
      return new MoveScore(-1, evaluate_board(board));
    }

    int bestScore = Integer.MIN_VALUE;
    int bestMove = 0;

    // for simulating the player we are playing as, we want to maximize our score
    // for simulating our opponent, we want to assume they will minimize our score
    int sign = (player == playingAs ? 1 : -1);

    for (int col_i = 0; col_i < COLUMNS; col_i++) {
      // skip filled columns
      if (board[ROWS - 1][col_i] != 0) {
        continue;
      }

      // make this move
      place_piece(board, player, col_i);

      // calculate the score
      int score = minMaxDescent(board, switchPlayer(player), depth - 1).score;

      // remove the piece to restore the board
      remove_piece(board, col_i);

      // for simulating `playingAs`, this will simply take the max
      // for simulating the opponent we are taking the greatest negative number (the
      // smallest
      // positive)
      //
      // consider a board with only 3 possible columns and our scores for each move
      // are
      // 10, 20, 30.
      //
      // 1
      // / | \
      // 10 20 30
      //
      // In this case if we are playing as 1 we want to travel to max(10,20,30), 30.
      // If we are not playing as 1, we want to assume that 1 will make the best move
      // and travel to
      // min(10,20,30) = -max(-10,-20,-30) = 10

      score = score * sign;
      if (score > bestScore) {
        bestScore = score;
        bestMove = col_i;
      }

    }

    // undo any sign switching we did!
    bestScore = bestScore * sign;

    return new MoveScore(bestMove, bestScore);
  }

  public static int bestColumn(int[][] board) {
    if (playingAs == -1) {
      return -1;
    }

    MoveScore m = minMaxDescent(board, playingAs, difficulty);
    return m.move;
  }

  // check win
  public static boolean checkWin(int[][] board, int player) {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLUMNS; col++) {
        if (board[row][col] == player) {
          if (checkDirection(board, row, col, 1, 0)) { // horizontal
            return true;
          } else if (checkDirection(board, row, col, 0, 1)) { // columns
            return true;
          } else if (checkDirection(board, row, col, 1, 1)) { // diagonal /
            return true;
          } else if (checkDirection(board, row, col, 1, -1)) { // diagonal \
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean checkDirection(int[][] board, int row, int col, int dRow, int dCol) {
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

  private static int evaluate_board(int[][] board) {

    int score = 0;
    int opponent = switchPlayer(playingAs);

    // for each cell, add the score for player and subtract the score for other
    for (int row_i = 0; row_i < ROWS; row_i++) {
      for (int col_i = 0; col_i < COLUMNS; col_i++) {
        if (board[row_i][col_i] == playingAs) {
          score += scoremap[row_i][col_i];
        } else if (board[row_i][col_i] == opponent) {
          score -= scoremap[row_i][col_i];
        }

      }
    }
    return score;
  }

  public static boolean isMaxDifficulty() {
    return difficulty == MAX_DIFFICULTY;

  }

}
