package controller;

import controller.utils.Point;
import java.util.Optional;
import controller.utils.BoardPosition;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import javafx.animation.PathTransition;
import javafx.fxml.FXML;
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

    Piece(PlayerRole player, Point pos) {
      super();

      position = pos.copy();

      super.setCenterX(position.getX());
      super.setCenterY(position.getY());

      if (player == PlayerRole.PlayerOne) {
        super.setFill(new ImagePattern(new Image("/assets/player1chip.png")));
      } else {
        super.setFill(new ImagePattern(new Image("/assets/player2chip.png")));
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

    // HACK: for now hardcode the current player, this call should be made on registration
    GameLogic.setLocalPlayer(new Player("dummy1", 0));
    GameLogic.setRemotePlayer(new Player("dummy2", 1));
    GameLogic.setCurrentPlayerRole(PlayerRole.PlayerOne);
    GameLogic.setLocalPlayerRole(PlayerRole.PlayerOne);

    NetworkClient.bindGameController(this);
    foregroundPane.toFront();
    backgroundPane.toBack();
    overlayPane.toFront();
    overlayPane.setMouseTransparent(true);
    foregroundPane
        .setBackground(new Background(
            new BackgroundImage(new Image("/assets/board.png"), BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

    chipPane1.setBackground(new Background(
        new BackgroundImage(new Image("/assets/player1chip.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    chipPane1.toFront();
    chipPane2.setBackground(new Background(
        new BackgroundImage(new Image("/assets/player2chip.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    chipPane2.toFront();

    setHandlers();
  }

  private void handleRelease(MouseEvent e) {
    // this means the piece is NOT in a valid position
    if (dropHint == null) {
      return;
    }

    // get the row and column associated with our drop hint
    BoardPosition rowCol = CoordUtils.toRowCol(dropHint.position).get();

    // calc the point for the top of this column
    Point topOfCol =
        CoordUtils.onGameScene(CoordUtils.fromRowCol(0, rowCol.getColumn()));

    // adjust the top y coord
    topOfCol.setY(
        (overlayPane.getHeight() - foregroundPane.getHeight()) / 2
            - draggedPiece.getRadius() * 2);

    // construct a path
    Path path = new Path();
    path.getElements().add(new MoveTo(draggedPiece.getCenterX(), draggedPiece.getCenterY()));
    path.getElements()
        .add(new LineTo(topOfCol.getX() + draggedPiece.getRadius(), topOfCol.getY()));

    // build the animation
    PathTransition pathTransition = new PathTransition();
    pathTransition.setDuration(Duration.millis(1.5 * draggedPiece.position.distanceTo(topOfCol)));
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
      dropHint = null;

      pTrans.setOnFinished(k -> {
        // update our logic
        gameLogic.placePiece(rowCol.getRow(), rowCol.getColumn(),
            gameLogic.getCurrentPlayerRole());

        // TODO:
        // NetworkClient.sendMove(rowCol.getValue());

        if (!gameIsOver()) {
          gameLogic.switchPlayer();
        }
      });
      draggedPiece = null;
    });

  }

  private void handleDrag(MouseEvent e, PlayerRole player) {
    if (player != gameLogic.getCurrentPlayerRole()) {
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
            imgString = "/assets/player1chip_ghost.png";
            break;
          case PlayerTwo:
            imgString = "/assets/player2chip_ghost.png";
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
      Piece toPlay =
          new Piece(GameLogic.getRemotePlayer().getRole(), CoordUtils.fromRowCol(r, col));

      // TODO: Add animation for remote player move
      midgroundPane.getChildren().add(toPlay);

      if (!gameIsOver()) {
        gameLogic.switchPlayer();
      }

    }, () -> {
      System.err.println("Recieved invalid move");
    });
  }

  private boolean gameIsOver() {
    Optional<BoardPosition[]> winningComboOpt =
        gameLogic.checkWin(gameLogic.getCurrentPlayerRole());

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
    Piece[] winningPieces = new Piece[4];
    for (int i = 0; i < 4; i++) {
      BoardPosition bp = winningPositions[i];
      System.out.println("bp:" + bp.getRow() + bp.getColumn());
      winningPieces[i] = new Piece(winner, CoordUtils.fromRowCol(bp.getRow(), bp.getColumn()));
      System.out.println(
          "piece loc:" + winningPieces[i].getCenterX() + winningPieces[i].getCenterY());
    }
    System.out.println(winner + "wins!");
    midgroundPane.getChildren().setAll(winningPieces);
    gameLogic.reset();
  }

  private void staleMate() {
    System.out.println("Stalemate!!");
  }

}
