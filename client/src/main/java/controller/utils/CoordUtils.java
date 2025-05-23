package controller.utils;

import java.util.Optional;
import logic.GameLogic;
import network.Player.PlayerRole;

public class CoordUtils {
  public final static int pieceRadius = 45;
  public final static int gamePaneWidth = 720;
  public final static int gamePaneHeight = 720;
  public final static int boardWidth = 630;
  public final static int boardHeight = 540;

  /**
   * @param col the desired column
   * @return A {@code Point} directly above the top slot of the specifed column.
   *         This point will be relative to the {@code GamePane}
   */
  public static Point topOfColumn(int col) {
    Point top = CoordUtils.onGameScene(CoordUtils.fromRowCol(0, col));
    top.setY((gamePaneHeight - boardHeight) / 2 - pieceRadius * 2);
    return top;
  }

  public static Point chipHolder(PlayerRole role) {
    Point p = new Point(0, gamePaneHeight - pieceRadius - 71, CoordSystem.GamePane);
    switch (role) {
      case None:
        break;
      case PlayerOne:
        p.setX(pieceRadius + 10);
        break;
      case PlayerTwo:
        p.setX(gamePaneWidth - pieceRadius - 10);
        break;
      default:
        break;
    }
    return p;
  }

  public static Optional<Point> onBoard(Point from) {
    switch (from.relativeTo) {

      case GameBoard:
        return Optional.of(from.copy());

      case GamePane: {
        if (from.x < (gamePaneWidth - boardWidth) / 2
            || from.x > (gamePaneWidth - boardWidth) / 2 + boardWidth) {
          return Optional.empty();
        } else if (from.y < (gamePaneHeight - boardHeight) / 2
            || from.y > (gamePaneHeight - boardHeight) / 2 + boardHeight) {
          return Optional.empty();
        } else {
          return Optional.of(new Point(from.x - (gamePaneWidth - boardWidth) / 2,
              from.y - (gamePaneHeight - boardHeight) / 2, CoordSystem.GameBoard));
        }
      }
      default:
        return Optional.empty();
    }
  }

  public static Point onGameScene(Point from) {
    switch (from.relativeTo) {
      case GameBoard:
        new Point(from.x + (gamePaneWidth - boardWidth) / 2,
            from.y + (gamePaneHeight - boardHeight) / 2, CoordSystem.GamePane);
      case GamePane: {
        return from.copy();
      }
      default:
        return null;
    }
  }

  public static Point fromRowCol(int row, int col) {
    return new Point(col * (pieceRadius * 2) + pieceRadius - 1.5,
        boardHeight - (row * (pieceRadius * 2) + pieceRadius),
        CoordSystem.GameBoard);
  }

  // NOTE: Power of Optionals
  public static Optional<BoardPosition> toRowCol(Point p) {
    return CoordUtils.onBoard(p).map(point -> {
      int col = (int) point.x / (pieceRadius * 2);
      int row = (int) point.y / (pieceRadius * 2);
      return new BoardPosition(GameLogic.numRows() - row - 1, col);
    });
  }
}
