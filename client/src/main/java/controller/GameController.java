package controller;

import java.util.ArrayList;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import logic.GameLogic;
import network.NetworkClient;

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

  private final static int pieceWidth = 90;
  private final static int pieceHeight = 90;
  private final static int paneWidth = 800;
  private final static int paneHeight = 720;
  private final static int sceneWidth = 1080;
  private final static int sceneHeight = 720;
  private final static int boardWidth = 630;
  private final static int boardHeight = 540;

  private Piece draggedPiece = null;
  private GhostPiece dropHintPiece = null;

  @FXML
  private StackPane gamePane;
  @FXML
  private Canvas backgroundCanvas;
  @FXML
  private Canvas midgroundCanvas;
  @FXML
  private Canvas foregroundCanvas;
  @FXML
  private Canvas overlayCanvas;
  @FXML
  private Canvas chipCanvas;
  private GameLogic gameLogic;

  private ArrayList<Piece> pieces;

  public class Point {
    double x;
    double y;
    boolean onScene;

    public double distanceTo(Point other) {
      if (other.onScene != this.onScene) {
        Point thisTemp = this.inScene();
        Point otherTemp = other.inScene();
        return Math.sqrt(Math.pow(thisTemp.x - otherTemp.x, 2) + Math.pow(thisTemp.y - otherTemp.x, 2));
      } else {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.x, 2));
      }
    }

    public void snapToGrid() {
      if (onScene) {
        return;
      } else {

        int col = ((int) this.x) / pieceWidth;
        Optional<Integer> row = gameLogic.getAvailableRow(col);

        if (row.isPresent()) {
          this.x = col * pieceWidth;
          this.y = (GameLogic.numRows() - row.get() - 1) * pieceHeight;
        }
      }
    }

    public void set(Point p) {
      this.x = p.x;
      this.y = p.y;
      this.onScene = p.onScene;
    }

    Point(double x, double y, boolean onScene) {
      this.x = x;
      this.y = y;
      this.onScene = onScene;
    }

    public Point inScene() {
      if (this.onScene) {
        return this;
      } else {
        return new Point(x + (paneWidth - boardWidth) / 2, y + (paneHeight - boardHeight) / 2,
            true);
      }
    }

    public static boolean isOnBoard(double x, double y) {
      if (x < (paneWidth - boardWidth) / 2
          || x > boardWidth + (paneWidth - boardWidth) / 2) {

        return false;
      } else if (y < (paneHeight - boardHeight) / 2
          || y > boardHeight + (paneHeight - boardHeight) / 2) {

        return false;
      } else {
        return true;
      }
    }

    public Optional<Point> onBoard() {
      if (!this.onScene) {

        return Optional.of(this);

      } else {

        if (Point.isOnBoard(x, y)) {
          return Optional
              .of(new Point(x - (paneWidth - boardWidth) / 2, y - (paneHeight - boardHeight) / 2,
                  false));
        } else {
          return Optional.empty();
        }

      }
    }
  }

  public class Piece {
    protected int row = -1;
    protected int column = -1;
    protected Point position;
    protected int player;
    protected Image texture;

    public Timeline moveTo(Point destination) {
      Point start = this.position;
      Point end = destination;
      final int pixelsPerFrame = 20;

      if (!this.position.onScene || !destination.onScene) {
        start = this.position.inScene();
        end = destination.inScene();
      }

      int steps = (int) start.distanceTo(end) / pixelsPerFrame;
      steps = steps == 0 ? 1 : steps;

      DoubleProperty x = new SimpleDoubleProperty();
      DoubleProperty y = new SimpleDoubleProperty();

      Timeline timeline = new Timeline(
          new KeyFrame(Duration.seconds(0),
              new KeyValue(x, start.x),
              new KeyValue(y, start.y)),
          new KeyFrame(Duration.millis(20 * steps),
              new KeyValue(x, end.x),
              new KeyValue(y, end.y)),
          new KeyFrame(Duration.millis(600),
              new KeyValue(x, end.x),
              new KeyValue(x, end.y)

          ));

      return timeline;
    }

    public static Image getImage(int player) {
      if (player == 1) {
        return new Image("/assets/player1chip.png");
      } else {
        return new Image("/assets/player2chip.png");
      }
    }

    public void snapToGrid() {
      position.snapToGrid();
    }

    public void setRowCol(int r, int c) {
      row = r;
      column = c;
    }

    public void setPosition(Point p) {
      position = p;
    }

    public void setPosition(double x, double y) {
      this.setPosition(new Point(x, y, true));
    }

    public Piece(int player) {

      this.player = player;

      if (player == 1) {
        texture = new Image("/assets/player1chip.png");
      } else {
        texture = new Image("/assets/player2chip.png");
      }
    }

    public void render(GraphicsContext gc) {
      gc.drawImage(texture, position.x, position.y);
    }
  }

  public class GhostPiece extends Piece {
    public boolean shouldRender = false;

    public GhostPiece(int player) {
      super(player);
      if (player == 1) {
        texture = new Image("/assets/player1chip_ghost.png");
      } else {
        texture = new Image("/assets/player2chip_ghost.png");
      }
    }

    public static Image getImage(int player) {
      if (player == 1) {
        return new Image("/assets/player1chip_ghost.png");
      } else {
        return new Image("/assets/player2chip_ghost.png");
      }
    }

    @Override
    public void render(GraphicsContext gc) {
      if (shouldRender) {
        gc.drawImage(texture, position.x, position.y);
      }
    }

  }

  /**
   * This function gets called by the FXMLLoader when the fxml gets,, loaded
   *
   */
  public void initialize() {
    gameLogic = new GameLogic();
    pieces = new ArrayList<>();

    overlayCanvas.toFront();
    overlayCanvas.setMouseTransparent(true);

    foregroundCanvas.toFront();
    backgroundCanvas.toBack();
    foregroundCanvas.getGraphicsContext2D().drawImage(new Image("/assets/board.png"), 0, 0);
    chipCanvas.getGraphicsContext2D().drawImage(new Image("/assets/player1chip.png"), 0, 0);

    setHandlers();

    NetworkClient.bindGameController(this);
    System.out.println("Game Controller initialized");
  }

  // this will be harder because we will have to manually calculate everyting, BUT
  // I think it will
  // be worth it because the Canvas is much more flexible
  @FXML
  private void handleColumnClick(ActionEvent event) {
  }

  @FXML
  private void handleColumnHover(ActionEvent event) {
  }

  private void setHandlers() {
    chipCanvas.setOnMousePressed(e -> {
      draggedPiece = new Piece(1);
      draggedPiece.setPosition(e.getSceneX() - pieceWidth / 2, e.getSceneY() - pieceHeight / 2);
      dropHintPiece = new GhostPiece(1);
      renderOverlay();
    });

    chipCanvas.setOnMouseDragged(e -> {
      if (draggedPiece != null) {
        draggedPiece.setPosition(e.getSceneX() - pieceWidth / 2, e.getSceneY() - pieceHeight / 2);

        Optional<Point> ghostPosition = new Point(e.getSceneX(), e.getSceneY(), true).onBoard();

        if (ghostPosition.isPresent()) {
          // set the position using onBoard coordinatese
          dropHintPiece.setPosition(ghostPosition.get());

          // snap the position to the grid
          dropHintPiece.snapToGrid();
          dropHintPiece.shouldRender = true;

          // clear out the mid layer and rerender the pieces
          GraphicsContext gc = midgroundCanvas.getGraphicsContext2D();
          gc.clearRect(0, 0, midgroundCanvas.getWidth(), midgroundCanvas.getHeight());
          drawPieces();

          // render our hint
          dropHintPiece.render(gc);

        } else {

          // the piece is outside the game board
          dropHintPiece.shouldRender = false;

          // clear out the mid layer and rerender the pieces
          GraphicsContext gc = midgroundCanvas.getGraphicsContext2D();
          gc.clearRect(0, 0, midgroundCanvas.getWidth(), midgroundCanvas.getHeight());
          drawPieces();
        }
        renderOverlay();
      }
    });

    chipCanvas.setOnMouseReleased(e -> {
      if (makeMove(getColumn(e.getSceneX(), e.getSceneY()))) {
        Piece toAnimate = pieces.getLast();
        toAnimate.position = new Point(draggedPiece.position.x, draggedPiece.position.y, false);
        animatePieceDrop(toAnimate);
      }
    });

  }

  private void renderOverlay() {
    GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
    gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

    if (draggedPiece != null && dropHintPiece != null) {
      draggedPiece.render(gc);
    }
  }

  private int getColumn(double sceneX, double sceneY) {
    Optional<Point> pointMB = new Point(sceneX, sceneY, true).onBoard();
    if (pointMB.isPresent()) {
      int col = ((int) pointMB.get().x) / pieceWidth;
      return col;
    }
    return -1;
  }

  private boolean makeMove(int col) {
    Optional<Integer> row = gameLogic.getAvailableRow(col);

    if (row.isPresent()) {

      // update the game logic
      gameLogic.placePiece(row.get(), col, gameLogic.currentPlayer());

      // build our new piece
      Piece newPiece = new Piece(gameLogic.currentPlayer());

      // set canvas position
      newPiece.setPosition(
          new Point(col * pieceWidth, (GameLogic.numRows() - row.get() - 1) * pieceHeight, false));

      // set row col
      newPiece.setRowCol(row.get(), col);

      pieces.add(newPiece);
      return true;
    }
    return false;
  }

  private void drawPieces() {
    GraphicsContext gc = midgroundCanvas.getGraphicsContext2D();
    for (Piece p : pieces) {
      p.render(gc);
    }
  }

  private void gameOver(int winner) {
    System.out.println("Game Over " + winner + " wins");
  }

  private void animatePieceDrop(Piece piece) {

    // calc top of column
    Point topOfCol = new Point((piece.column * pieceWidth) + ((gamePane.getWidth() - boardWidth) / 2),
        (sceneHeight - boardHeight) / 2 - pieceHeight, true);

    // calc start and end
    Point start = piece.position;
    Point end = topOfCol;
    final int pixelsPerFrame = 20;

    // make sure both are scene-relative
    if (!piece.position.onScene) {
      start = piece.position.inScene();
    }

    // calc steps
    int steps = (int) start.distanceTo(end) / pixelsPerFrame;
    steps = steps == 0 ? 1 : steps;

    // create our properties
    DoubleProperty x = new SimpleDoubleProperty();
    DoubleProperty y = new SimpleDoubleProperty();

    // build our timeline
    Timeline timeline = new Timeline(
        new KeyFrame(Duration.seconds(0),
            new KeyValue(x, start.x),
            new KeyValue(y, start.y)),
        new KeyFrame(Duration.millis(20 * steps),
            new KeyValue(x, end.x),
            new KeyValue(y, end.y)),
        new KeyFrame(Duration.millis(600),
            new KeyValue(x, end.x),
            new KeyValue(x, end.y)

        ));

    AnimationTimer timer = new AnimationTimer() {

      @Override
      public void handle(long now) {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        piece.setPosition(x.doubleValue(), y.doubleValue());
        piece.render(gc);
      }
    };

    timeline.setOnFinished(e -> {
      timer.stop();
      piece.setPosition(
          new Point(piece.column * pieceWidth, (GameLogic.numRows() - piece.row - 1) * pieceHeight, false));
      drawPieces();
      if (gameLogic.checkWin(gameLogic.currentPlayer())) {
        gameOver(gameLogic.currentPlayer());
      }
      draggedPiece = null;
      dropHintPiece = null;
      renderOverlay();
    });

    timer.start();
    timeline.play();
  }

}
