package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import logic.GameLogic;
import network.NetworkClient;

import javafx.scene.paint.Color;
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

  private final static int gridWidth = 90;
  private final static int gridHeight = 90;
  private final static int paneWidth = 700;
  private final static int paneHeight = 700;
  private final static int sceneWidth = 1080;
  private final static int sceneHeight = 720;

  private Piece draggedPiece = null;

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

  public class Piece {
    private int row = -1;
    private int column = -1;
    private double positionX = 0;
    private double positionY = 0;
    private int player;
    private Image texture;

    public static Image getImage(int player) {
      if (player == 1) {
        return new Image("/assets/player1chip.png");
      } else {
        return new Image("/assets/player2chip.png");
      }
    }

    public void setPosition(double x, double y) {
      positionX = x;
      positionY = y;
    }

    public Piece(int player) {

      this.player = player;

      if (player == 1) {
        texture = new Image("/assets/player1chip.png");
      } else {
        texture = new Image("/assets/player2chip.png");
      }
    }

    public void place(int row, int column) {
      this.row = row;
      this.column = column;
    }

    public void render(GraphicsContext gc) {
      gc.drawImage(texture, positionX, positionY);
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
  private void handleColumnClick(ActionEvent event) {}

  @FXML
  private void handleColumnHover(ActionEvent event) {}

  private void setHandlers() {
    chipCanvas.setOnMousePressed(e -> {
      draggedPiece = new Piece(1);
      draggedPiece.setPosition(e.getSceneX() - gridWidth / 2, e.getSceneY() - gridHeight / 2);
      renderOverlay();
    });

    chipCanvas.setOnMouseDragged(e -> {
      if (draggedPiece != null) {
        draggedPiece.setPosition(e.getSceneX() - gridWidth / 2, e.getSceneY() - gridHeight / 2);
        renderOverlay();
      }
    });

    chipCanvas.setOnMouseReleased(e -> {
      draggedPiece = null;
      if (makeMove(getColumn(e.getSceneX(), e.getSceneY()))) {
        animatePieceDrop(draggedPiece);
      }
      renderOverlay();
    });

  }

  private void renderOverlay() {
    GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
    gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
    if (draggedPiece != null) {
      draggedPiece.render(gc);
    }
  }

  private int getColumn(double sceneX, double sceneY) {
    return 0;
  }

  private boolean makeMove(int col) {
    if (col < 0) {
      return false;
    }
    return false;
  }

  private void animatePieceDrop(Piece piece) {}

}
