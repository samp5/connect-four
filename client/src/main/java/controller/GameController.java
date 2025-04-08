package controller;

import java.util.ArrayList;
import java.util.Optional;
import com.sun.prism.paint.Color;
import controller.utils.Point;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import logic.GameLogic;
import network.NetworkClient;

import javafx.util.Duration;
import javafx.util.Pair;

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
  private Pane chipPane;
  private GameLogic gameLogic;

  private ArrayList<Piece> pieces;

  public enum Player {
    PlayerOne, PlayerTwo,
  }

  private class Piece extends Circle {
    Point position;

    Piece(Player player, Point position) {
      super();

      this.position = position.copy();
      if (player == Player.PlayerOne) {
        super.setFill(new ImagePattern(new Image("/assets/player1chip.png")));
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

  /**
   * This function gets called by the FXMLLoader when the fxml gets,, loaded
   *
   */
  public void initialize() {
    gameLogic = new GameLogic();
    pieces = new ArrayList<>();

    foregroundPane.toFront();
    backgroundPane.toBack();
    overlayPane.toFront();
    overlayPane.setMouseTransparent(true);
    foregroundPane
        .setBackground(new Background(
            new BackgroundImage(new Image("/assets/board.png"), BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

    chipPane.setBackground(new Background(
        new BackgroundImage(new Image("/assets/player1chip.png"), BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    chipPane.toFront();

    setHandlers();

    NetworkClient.bindGameController(this);
    System.out.println("Game Controller initialized");
  }

  private void setHandlers() {
    chipPane.setOnMouseDragged(e -> {
      if (draggedPiece == null) {
        draggedPiece = new Piece(Player.PlayerOne,
            new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
        overlayPane.getChildren().addAll(draggedPiece);
      }

      draggedPiece.setPoint(e.getSceneX(), e.getSceneY());

      int col = CoordUtils.toRowCol(draggedPiece.position).map(pair -> {
        return pair.getValue();
      }).orElse(-1);

      gameLogic.getAvailableRow(col).ifPresentOrElse(row -> {
        if (dropHint == null) {
          dropHint = new Piece(Player.PlayerOne,
              new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
          dropHint.setFill(new Image("/assets/player1chip_ghost.png"));
          midgroundPane.getChildren().addAll(dropHint);
        }
        dropHint.setPoint(CoordUtils.fromRowCol(row, col));
      }, () -> {
        midgroundPane.getChildren().remove(dropHint);
        dropHint = null;
      });

    });

    chipPane.setOnMouseReleased(e -> {
      if (dropHint != null) {

        // remove our dragged piece + hint
        overlayPane.getChildren().remove(draggedPiece);

        // add our piece to play
        Piece pieceRaise = new Piece(gameLogic.currentPlayer(), draggedPiece.position.copy());
        overlayPane.getChildren().add(pieceRaise);

        // copy the initial posiiton of our dragged piece
        pieceRaise.setPoint(draggedPiece.position);

        // get the row and column associated with our drop hint
        Pair<Integer, Integer> rowCol = CoordUtils.toRowCol(dropHint.position).get();

        // calc the point for the top of this column
        Point topOfCol =
            CoordUtils.onGameScene(CoordUtils.fromRowCol(rowCol.getKey(), rowCol.getValue()));

        // adjust the top y coord
        topOfCol.setY(
            (overlayPane.getHeight() - foregroundPane.getHeight()) / 2
                - pieceRaise.getRadius() * 2);

        // construct a path
        Path path = new Path();
        path.getElements().add(new MoveTo(pieceRaise.getCenterX(), pieceRaise.getCenterY()));
        path.getElements()
            .add(new LineTo(topOfCol.getX() + pieceRaise.getRadius(), topOfCol.getY()));

        // build the animation
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(1.5 * pieceRaise.position.distanceTo(topOfCol)));
        pathTransition.setPath(path);
        pathTransition.setNode(pieceRaise);
        pathTransition.play();

        pathTransition.setOnFinished(f -> {
          overlayPane.getChildren().remove(pieceRaise);
          Point topSlot = CoordUtils.fromRowCol(GameLogic.numRows() - 1, rowCol.getValue());
          Piece pieceToDrop = new Piece(Player.PlayerOne, topSlot.copy());
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
        });

        // update our logic
        gameLogic.placePiece(rowCol.getKey(), rowCol.getValue(), gameLogic.currentPlayer());
        draggedPiece = null;
      }
    });
  }

  private void renderOverlay() {}

  private boolean makeMove(int col) {
    Optional<Integer> row = gameLogic.getAvailableRow(col);

    if (row.isPresent()) {

      // update the game logic
      gameLogic.placePiece(row.get(), col, gameLogic.currentPlayer());

      return true;
    }
    return false;
  }


  private void gameOver(int winner) {}

}
