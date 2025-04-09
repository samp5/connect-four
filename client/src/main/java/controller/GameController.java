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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
  private Pane chipPane1;
  @FXML
  private Pane chipPane2;
  private GameLogic gameLogic;

  private ArrayList<Piece> pieces;

  public enum Player {
    PlayerOne, PlayerTwo, None
  }

  private class Piece extends Circle {
    Point position;

    Piece(Player player, Point position) {
      super();

      this.position = position.copy();
      if (player == Player.PlayerOne) {
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

  public void setLocalPlayer(Player local) {
    gameLogic.setLocalPlayer(local);
  }

  /**
   * This function gets called by the FXMLLoader when the fxml gets,, loaded
   *
   */
  public void initialize() {
    gameLogic = new GameLogic();
    NetworkClient.bindGameController(this);

    gameLogic.setLocalPlayer(Player.PlayerOne);
    pieces = new ArrayList<>();
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
    System.out.println("Game Controller initialized");
  }

  private void handleRelease(MouseEvent e) {
    if (dropHint != null) {

      // remove our dragged piece + hint
      overlayPane.getChildren().remove(draggedPiece);

      // add our piece to play
      Piece pieceRaise = new Piece(gameLogic.getCurrentPlayer(), draggedPiece.position.copy());
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
        Piece pieceToDrop = new Piece(gameLogic.getCurrentPlayer(), topSlot.copy());
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
          gameLogic.placePiece(rowCol.getKey(), rowCol.getValue(), gameLogic.getCurrentPlayer());

          if (gameLogic.checkWin(gameLogic.getCurrentPlayer())) {
            gameOver(gameLogic.getCurrentPlayer());
          } else if (gameLogic.staleMate()) {
            staleMate();
          }

          gameLogic.switchPlayer();
        });
      });

      draggedPiece = null;
    }
  }

  private void handleDrag(MouseEvent e, Player player) {
    if (player != gameLogic.getCurrentPlayer()) {
      return;
    }
    // if (gameLogic.getCurrentPlayer() != gameLogic.getLocalPlayer()) {
    // return;
    // }
    if (draggedPiece == null) {
      draggedPiece = new Piece(gameLogic.getCurrentPlayer(),
          new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
      overlayPane.getChildren().addAll(draggedPiece);
    }

    draggedPiece.setPoint(e.getSceneX(), e.getSceneY());

    int col = CoordUtils.toRowCol(draggedPiece.position).map(pair -> {
      return pair.getValue();
    }).orElse(-1);

    gameLogic.getAvailableRow(col).ifPresentOrElse(row -> {
      if (dropHint == null) {
        dropHint = new Piece(gameLogic.getCurrentPlayer(),
            new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
        String imgString;
        switch (gameLogic.getCurrentPlayer()) {
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
      handleDrag(e, Player.PlayerTwo);
    });

    chipPane2.setOnMouseReleased(e -> {
      handleRelease(e);
    });

    chipPane1.setOnMouseDragged(e -> {
      handleDrag(e, Player.PlayerOne);
    });

    chipPane1.setOnMouseReleased(e -> {
      handleRelease(e);
    });
  }

  private void gameOver(Player winner) {
    System.out.println(winner + "wins!");
    midgroundPane.getChildren().setAll();
    gameLogic.reset();
  }

  private void staleMate() {
    System.out.println("Stalemate!!");
  }

}
