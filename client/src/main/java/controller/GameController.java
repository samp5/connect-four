package controller;

import controller.utils.Point;
import java.util.Optional;
import controller.utils.BoardPosition;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import javafx.animation.PathTransition;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import logic.GameLogic;
import network.NetworkClient;
import network.Player;
import network.Player.PlayerRole;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Should bind directly to the main game fxml and provide UI logic
 *
 * - Handle user interaction + input
 * - Animate moves that are made
 * - Call onto GameLogic to validate and apply moves, switch players, etc
 * - Send moves to server via NetworkClient.sendMove()
 * - Maybe handle game-end events
 *
 */
public class GameController {

  private Piece draggedPiece = null;
  private Piece dropHint = null;

  @FXML
  private StackPane gamePane;
  @FXML
  private Pane backgroundPane;
  @FXML
  private Pane midgroundPane;
  @FXML
  private Pane foregroundPane;
  @FXML
  private Pane overlayPane;
  @FXML
  private Pane chipPane1;
  @FXML
  private Pane chipPane2;
  private GameLogic gameLogic;

  private class Piece extends Circle {
    public Point position;
    public PlayerRole role;

    public PlayerRole getPlayer() {
      return role;
    }

    Piece(PlayerRole player, Point pos) {
      super();

      position = pos.copy();
      role = player;

      super.setCenterX(position.getX());
      super.setCenterY(position.getY());

      if (player == PlayerRole.PlayerOne) {
        super.setFill(new ImagePattern(new Image("/assets/red_chip.png")));
      } else {
        super.setFill(new ImagePattern(new Image("/assets/blue_chip.png")));
      }
      super.setRadius(45);
    }

    public void setFill(Image img) {
      super.setFill(new ImagePattern(img));
    }

    public void setPoint(double x, double y) {
      this.position.set(x, y);
      super.setCenterX(x);
      super.setCenterY(y);
    }

    public void setPoint(Point p) {
      this.position = p.copy();
      super.setCenterX(position.getX());
      super.setCenterY(position.getY());
    }
  }

  public void setLocalPlayer(PlayerRole local) {
    GameLogic.setLocalPlayerRole(local);
  }

  /**
   * This function gets called by the FXMLLoader when the fxml gets,, loaded
   *
   */
  public void initialize() {
    gameLogic = new GameLogic();

    NetworkClient.bindGameController(this);
    foregroundPane.toFront();
    backgroundPane.toBack();
    overlayPane.toFront();
    overlayPane.setMouseTransparent(true);
    foregroundPane
        .setBackground(new Background(
            new BackgroundImage(new Image("/assets/board.png"), BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(632, 542, false, false, false, false))));

    chipPane1.setBackground(new Background(
        new BackgroundImage(new Image("/assets/red_chip.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(90, 90, false, false, false, false))));
    chipPane1.toFront();
    chipPane2.setBackground(new Background(
        new BackgroundImage(new Image("/assets/blue_chip.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(90, 90, false, false, false, false))));
    chipPane2.toFront();

    setHandlers();
  }

  private void setCustomCursors() {
    chipPane1.setCursor(new ImageCursor(new Image("/assets/hand_cursor.png")));
    chipPane2.setCursor(new ImageCursor(new Image("/assets/hand_cursor.png")));
  }

  private void handleRelease(MouseEvent e) {
    // this means the piece is NOT in a valid position
    if (dropHint == null) {
      return;
    }

    // get the row and column associated with our drop hint
    BoardPosition rowCol = CoordUtils.toRowCol(dropHint.position).get();

    // calc the point for the top of this column
    Point topOfCol = CoordUtils.topOfColumn(rowCol.getColumn());

    // construct a path
    Path path = new Path();
    path.getElements().add(new MoveTo(draggedPiece.getCenterX(), draggedPiece.getCenterY()));
    path.getElements()
        .add(new LineTo(topOfCol.getX() + draggedPiece.getRadius(), topOfCol.getY()));

    // build the animation
    PathTransition pathTransition = new PathTransition();
    pathTransition.setDuration(Duration.millis(1 * draggedPiece.position.distanceTo(topOfCol)));
    pathTransition.setPath(path);
    pathTransition.setNode(draggedPiece);
    pathTransition.play();

    pathTransition.setOnFinished(f -> {
      overlayPane.getChildren().remove(draggedPiece);

      Point topSlot = CoordUtils.fromRowCol(GameLogic.numRows() - 1, rowCol.getColumn());
      Piece pieceToDrop = new Piece(gameLogic.getCurrentPlayerRole(), topSlot.copy());
      midgroundPane.getChildren().add(pieceToDrop);

      Path pth = new Path();
      pth.getElements().add(new MoveTo(topSlot.getX(), topSlot.getY()));
      pth.getElements().add(new LineTo(dropHint.getCenterX(),
          dropHint.getCenterY()));

      // build the animation
      PathTransition pTrans = new PathTransition();
      pTrans.setDuration(Duration.millis(1500));
      pTrans.setPath(pth);
      pTrans.setNode(pieceToDrop);

      pTrans.play();
      midgroundPane.getChildren().remove(dropHint);
      dropHint = null;

      pTrans.setOnFinished(k -> {
        // update our logic
        gameLogic.placePiece(rowCol.getRow(), rowCol.getColumn(),
            gameLogic.getCurrentPlayerRole());

        NetworkClient.sendMove(rowCol.getColumn());

        if (!gameIsOver()) {
          gameLogic.switchPlayer();
        }
      });
      draggedPiece = null;
      dropHint = null;
    });

  }

  private void handleDrag(MouseEvent e, PlayerRole player) {
    if (GameLogic.getLocalPlayer().getRole() != gameLogic.getCurrentPlayerRole()) {
      return;
    }

    if (draggedPiece == null) {
      draggedPiece = new Piece(gameLogic.getCurrentPlayerRole(),
          new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
      overlayPane.getChildren().addAll(draggedPiece);
    }

    draggedPiece.setPoint(e.getSceneX(), e.getSceneY());

    int col = CoordUtils.toRowCol(draggedPiece.position).map(bp -> {
      return bp.getColumn();
    }).orElse(-1);

    gameLogic.getAvailableRow(col).ifPresentOrElse(row -> {
      if (dropHint == null) {
        dropHint = new Piece(gameLogic.getCurrentPlayerRole(),
            new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
        String imgString;
        switch (gameLogic.getCurrentPlayerRole()) {
          case PlayerOne:
            imgString = "/assets/red_chip_hint.png";
            break;
          case PlayerTwo:
            imgString = "/assets/blue_chip_hint.png";
            break;
          case None:
          default:
            return;
        }
        dropHint.setFill(new Image(imgString));
        midgroundPane.getChildren().addAll(dropHint);
      }
      dropHint.setPoint(CoordUtils.fromRowCol(row, col));
    }, () -> {
      midgroundPane.getChildren().remove(dropHint);
      dropHint = null;
    });
  }

  private void setHandlers() {
    chipPane2.setOnMouseDragged(e -> {
      handleDrag(e, PlayerRole.PlayerTwo);
    });

    chipPane2.setOnMouseReleased(e -> {
      handleRelease(e);
    });

    chipPane1.setOnMouseDragged(e -> {
      handleDrag(e, PlayerRole.PlayerOne);
    });

    chipPane1.setOnMouseReleased(e -> {
      handleRelease(e);
    });
  }

  public Player getLocalPlayer() {
    return GameLogic.getLocalPlayer();
  }

  public void recieveMove(int col) {
    gameLogic.getAvailableRow(col).ifPresentOrElse((r) -> {
      Player player = GameLogic.getRemotePlayer();
      Piece toPlay = new Piece(player.getRole(), CoordUtils.fromRowCol(r, col));

      // TODO: Add animation for remote player move
      midgroundPane.getChildren().add(toPlay);
      gameLogic.placePiece(gameLogic.getAvailableRow(col).orElse(6), col, player.getRole());

      if (!gameIsOver()) {
        gameLogic.switchPlayer();
      }

    }, () -> {
      System.err.println("Recieved invalid move");
    });
  }

  private boolean gameIsOver() {
    Optional<BoardPosition[]> winningComboOpt = gameLogic.checkWin(gameLogic.getCurrentPlayerRole());

    if (winningComboOpt.isPresent()) {
      gameOver(gameLogic.getCurrentPlayerRole(), winningComboOpt.get());
      return true;
    } else if (gameLogic.staleMate()) {
      staleMate();
      return true;
    } else {
      return false;
    }
  }

  private void gameOver(PlayerRole winner, BoardPosition[] winningPositions) {

    // TODO: Animations for winning combination

    gameLogic.setCurrentPlayerRole(PlayerRole.None);

    Piece[] winningPieces = new Piece[4];
    for (int i = 0; i < 4; i++) {
      BoardPosition bp = winningPositions[i];
      winningPieces[i] = new Piece(winner, CoordUtils.fromRowCol(bp.getRow(),
          bp.getColumn()));
    }
    // // midgroundPane.getChildren().setAll(winningPieces);
    //
    // Arrays.sort(winningPieces, (p1, p2) -> {
    // return (int) (p2.position.getY() - p1.position.getY());
    // });

    Object[] pieces = midgroundPane.getChildren().toArray();
    for (Object pi : pieces) {
      Piece p = (Piece) pi;

      // calc the point for the top of this column
      Point topOfCol = CoordUtils.topOfColumn(CoordUtils.toRowCol(p.position).get().getColumn());

      Path path = new Path();
      path.getElements().add(new MoveTo(p.getCenterX(), p.getCenterY()));
      path.getElements()
          .add(new LineTo(topOfCol.getX(), topOfCol.getY()));

      // build the animation
      PathTransition pathTransition = new PathTransition();
      pathTransition.setDuration(Duration.millis(1.5 * p.position.distanceTo(topOfCol) + Math.random() * 20));
      pathTransition.setPath(path);
      pathTransition.setNode(p);
      pathTransition.play();
      pathTransition.setOnFinished(e -> {

        midgroundPane.getChildren().remove(p);

        double retX = p.getPlayer() == PlayerRole.PlayerOne ? p.getRadius() + 10
            : gamePane.getWidth() - p.getRadius() - 10;

        Point returnPoint = new Point(retX, overlayPane.getHeight() - p.getRadius() - 10,
            CoordSystem.GamePane);

        Piece pieceToReturn = new Piece(p.getPlayer(), topOfCol);
        overlayPane.getChildren().add(pieceToReturn);

        Path path2 = new Path();
        path2.getElements().add(new MoveTo(pieceToReturn.getCenterX(), pieceToReturn.getCenterY()));
        path2.getElements()
            .add(new LineTo(returnPoint.getX(), returnPoint.getY()));

        // build the animation
        PathTransition pathTransition2 = new PathTransition();
        pathTransition2
            .setDuration(Duration.millis(1.5 * pieceToReturn.position.distanceTo(returnPoint) + Math.random() * 20));
        pathTransition2.setPath(path2);
        pathTransition2.setNode(pieceToReturn);
        pathTransition2.play();
        pathTransition2.setOnFinished(g -> {
          overlayPane.getChildren().remove(pieceToReturn);
        });

      });
      System.out.println(winner + "wins!");
      gameLogic.reset();
    }

  }

  private void staleMate() {
    System.out.println("Stalemate!!");
  }

}
