package controller;

import controller.utils.Point;

import java.util.ArrayList;
import java.util.Optional;

import controller.utils.BoardPosition;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import controller.utils.GameSettings;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import logic.AI;
import network.NetworkClient;
import network.Player;
import network.Message.WinType;
import network.Player.PlayerRole;
import utils.CursorManager;

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
  private boolean canMove = true;

  @FXML
  private Pane gamePaneBackground;
  @FXML
  private AnchorPane gamePane;
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
  @FXML
  private BorderPane settingsButton;
  @FXML
  private ImageView clouds;

  @FXML
  private Pane drawRequest;
  @FXML
  private Button drawRequestAccept;
  @FXML
  private Button drawRequestReject;

  private GameLogic gameLogic;

  public static void setGameMode(GameMode mode) {
    GameLogic.setGameMode(mode);
  }

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

  public void initialize() {
    gameLogic = new GameLogic();

    CursorManager.setHandCursor(chipPane1, chipPane2);
    animateClouds();

    NetworkClient.bindGameController(this);
    settingsButton.setBackground(SettingsController.getButtonBackground(40));
    overlayPane.setMouseTransparent(true);
    setHandlers();
  }

  public void animateClouds() {
    Path cloudPath = new Path(new MoveTo(0, 0), new LineTo(720, 0));
    PathTransition cloudAnimation = new PathTransition(Duration.seconds(65), cloudPath, clouds);
    cloudAnimation.play();

    cloudAnimation.onFinishedProperty().set(e -> {
      Path fullPath = new Path(new MoveTo(-720, 0), new HLineTo(720));
      PathTransition fullAnimation = new PathTransition(Duration.seconds(130), fullPath, clouds);
      fullAnimation.setCycleCount(PathTransition.INDEFINITE);
      fullAnimation.play();
    });
  }

  private void handleRelease(MouseEvent e) {
    // this means the piece is NOT in a valid position
    if (dropHint == null) {
      overlayPane.getChildren().remove(draggedPiece);
      draggedPiece = null;
      return;
    }

    // get positions and role
    BoardPosition rowCol = CoordUtils.toRowCol(dropHint.position).get();
    PlayerRole role = gameLogic.getCurrentPlayerRole();
    Point startPos = new Point(draggedPiece.getCenterX(), draggedPiece.getCenterY(), CoordSystem.GamePane);

    handleMove(rowCol.getColumn(), role);
    animateMove(rowCol, startPos, role);

    // send the move to the client
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      NetworkClient.sendMove(rowCol.getColumn());
    }

    // remove the drop hint
    midgroundPane.getChildren().remove(dropHint);
    dropHint = null;
  }

  private void handleMove(int col, PlayerRole role) {
    gameLogic.getAvailableRow(col).ifPresentOrElse((r) -> {
      gameLogic.placePiece(gameLogic.getAvailableRow(col).orElse(6), col, role);
      gameLogic.switchPlayer();
    }, () -> {
      System.err.println("Recieved invalid move");
    });

  }

  private PathTransition animateToTop(BoardPosition rowCol, Point chipPos, PlayerRole role) {
    if (draggedPiece == null) {
      draggedPiece = new Piece(role, CoordUtils.chipHolder(role));
      overlayPane.getChildren().add(draggedPiece);
    }
    // lock playing a move until animation pt 1 is done
    canMove = false;

    // calc the point for the top of this column
    Point topOfCol = CoordUtils.topOfColumn(rowCol.getColumn());
    double topX = topOfCol.getX();
    double topY = topOfCol.getY() - CoordUtils.pieceRadius;

    // construct a path
    Path path = new Path();
    path.getElements().add(new MoveTo(chipPos.getX(), chipPos.getY()));
    path.getElements().add(new LineTo(topX + CoordUtils.pieceRadius,
        topY));

    // build the animation
    PathTransition pathTransition = new PathTransition();
    pathTransition.setDuration(Duration.millis(1 * chipPos.distanceTo(topOfCol)));
    pathTransition.setPath(path);
    pathTransition.setNode(draggedPiece);
    return pathTransition;
  }

  private void animateDrop(Point from, BoardPosition rowCol, PlayerRole role) {
    double fromX = from.getX();
    double fromY = from.getY() - CoordUtils.pieceRadius;

    Point topSlot = CoordUtils.fromRowCol(GameLogic.numRows() - 1, rowCol.getColumn());
    Piece pieceToDrop = new Piece(role, topSlot.copy());
    Point finalPosition = CoordUtils.fromRowCol(rowCol.getRow(), rowCol.getColumn());
    midgroundPane.getChildren().add(pieceToDrop);

    Path pth = new Path();
    pth.getElements().add(new MoveTo(fromX, fromY - pieceToDrop.getRadius()));
    pth.getElements().add(new LineTo(finalPosition.getX(), finalPosition.getY()));

    // build the animation
    PathTransition pTrans = new PathTransition();
    pTrans.setDuration(Duration.millis(1500));
    pTrans.setPath(pth);
    pTrans.setNode(pieceToDrop);

    pTrans.play();

    pTrans.setOnFinished(k -> {
      // chack for game over
      checkGameOver(role);

      // run our AI if we need to
      if (GameLogic.getGameMode() == GameMode.LocalAI
          && gameLogic.getCurrentPlayerRole() == AI.getRole()) {
        int aiCol = AI.bestColumn(gameLogic.getBoard());
        BoardPosition bp = new BoardPosition(gameLogic.getAvailableRow(aiCol).get(), aiCol);
        handleMove(AI.bestColumn(gameLogic.getBoard()), AI.getRole());
        if (Math.random() < 0.4) {
          NetworkClient.handleChat(AI.getQuip(), "AI", false);
        }
        animateMove(bp, CoordUtils.chipHolder(AI.getRole()),
            AI.getRole());
      }
    });

  }

  private void animateMove(BoardPosition rowCol, Point chipPos, PlayerRole role) {
    PathTransition pathTransition = animateToTop(rowCol, chipPos, role);
    pathTransition.play();

    Point topOfCol = CoordUtils.topOfColumn(rowCol.getColumn());
    double topX = topOfCol.getX();
    double topY = topOfCol.getY() - CoordUtils.pieceRadius;

    pathTransition.setOnFinished(f -> {
      overlayPane.getChildren().remove(draggedPiece);
      animateDrop(topOfCol, rowCol, role);
      draggedPiece = null;
      canMove = true;
    });
  }

  private void handleDrag(MouseEvent e, PlayerRole playerPile) {
    if (!canMove || !GameLogic.getGameMode().canMove(gameLogic, playerPile)) {
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

    settingsButton.setOnMouseClicked(e -> {
      GameSettings.loadOnto(foregroundPane);
    });

    drawRequestAccept.setOnAction(e -> {
      staleMate();
      drawRequest.setVisible(false);
      NetworkClient.replyDrawRequest(true);
    });

    drawRequestReject.setOnAction(e -> {
      drawRequest.setVisible(false);
      NetworkClient.replyDrawRequest(false);
    });
  }

  public Player getLocalPlayer() {
    return GameLogic.getLocalPlayer();
  }

  public ArrayList<Integer> getMoveHistory() {
    return this.gameLogic.getMoveHistory();
  }

  public void restoreGameBoard(ArrayList<Integer> moves) {
    for (int move_i = 0; move_i < moves.size(); move_i++) {
      int col = moves.get(move_i);
      PlayerRole role;
      if (move_i % 2 == 0) {
        role = PlayerRole.PlayerOne;
      } else {
        role = PlayerRole.PlayerTwo;
      }
      gameLogic.getAvailableRow(col).ifPresent((row) -> {
        // get locations and role
        BoardPosition rowCol = new BoardPosition(row, col);
        handleMove(rowCol.getColumn(), role);
        animateDrop(CoordUtils.topOfColumn(rowCol.getColumn()), rowCol, role);
      });
    }
  }

  public void recieveMove(int col) {
    // handle the move logic and animate it
    gameLogic.getAvailableRow(col).ifPresent((row) -> {
      // get locations and role
      BoardPosition rowCol = new BoardPosition(row, col);
      PlayerRole role = GameLogic.getRemotePlayer().getRole();
      Point chipHolder = CoordUtils.chipHolder(role);

      // add the chip to animate
      draggedPiece = new Piece(role, chipHolder);
      overlayPane.getChildren().add(draggedPiece);

      handleMove(rowCol.getColumn(), role);
      animateMove(rowCol, chipHolder, role);
    });
  }

  private void checkGameOver(PlayerRole role) {
    Optional<BoardPosition[]> winningComboOpt = gameLogic.checkWin(role);

    if (winningComboOpt.isPresent()) {
      gameOver(role, winningComboOpt.get());
    } else if (gameLogic.staleMate()) {
      staleMate();
    }
  }

  private void gameOver(PlayerRole winner, BoardPosition[] winningPositions) {
    canMove = false;

    GameLogic.setCurrentPlayerRole(PlayerRole.None);
    // Piece[] winningPieces = new Piece[4];
    // for (int i = 0; i < 4; i++) {
    // BoardPosition bp = winningPositions[i];
    // winningPieces[i] = new Piece(winner, CoordUtils.fromRowCol(bp.getRow(),
    // bp.getColumn()));
    // }
    Object[] pieces = midgroundPane.getChildren().toArray();
    // midgroundPane.getChildren().addAll(winningPieces);
    for (Object pi : pieces) {
      Piece p = (Piece) pi;

      // calc the point for the top of this column
      Point topOfCol = CoordUtils.topOfColumn(CoordUtils.toRowCol(p.position).get().getColumn());

      Path path = new Path();
      path.getElements().add(new MoveTo(p.getCenterX(), p.getCenterY()));
      path.getElements()
          .add(new LineTo(topOfCol.getX(), topOfCol.getY()));

      // build the animation
      PathTransition pathTransition = new PathTransition(
          Duration.millis(1.5 * p.position.distanceTo(topOfCol) + Math.random() * 20), path, p);

      pathTransition.setOnFinished(e -> {

        midgroundPane.getChildren().remove(p);

        Point returnPoint = CoordUtils.chipHolder(p.getPlayer());

        Piece pieceToReturn = new Piece(p.getPlayer(), topOfCol);
        overlayPane.getChildren().add(pieceToReturn);

        Path path2 = new Path();
        path2.getElements().add(new MoveTo(pieceToReturn.getCenterX(), pieceToReturn.getCenterY()));
        path2.getElements()
            .add(new LineTo(returnPoint.getX(), returnPoint.getY()));

        // build the animation
        PathTransition pathTransition2 = new PathTransition(Duration
            .millis(1.5 * pieceToReturn.position.distanceTo(returnPoint) + Math.random() * 20),
            path2, pieceToReturn);

        pathTransition2.setOnFinished(g -> {
          overlayPane.getChildren().remove(pieceToReturn);

        });
        pathTransition2.play();

        String loc;
        switch (winner) {
          case None:
            loc = "/assets/draw-message.png";
            break;
          case PlayerOne:
            loc = "/assets/red-wins.png";
            break;
          case PlayerTwo:
            loc = "/assets/blue-wins.png";
            break;
          default:
            loc = "/assets/cloud1.png";
        }
        Image textImage = new Image(loc, 0, 96, true, false);
        ImageView winnerImg = new ImageView(textImage);

        overlayPane.getChildren().add(winnerImg);

        winnerImg.setX((CoordUtils.gamePaneWidth - textImage.widthProperty().get()) / 2);
        winnerImg.setY(100);

        PauseTransition pt = new PauseTransition(Duration.millis(4000));
        pt.setOnFinished(g -> {
          midgroundPane.getChildren().setAll();
          overlayPane.getChildren().remove(winnerImg);
          canMove = true;
        });
        pt.play();
      });

      pathTransition.play();
    }
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      if (winner == GameLogic.getLocalPlayer().getRole()) {
        NetworkClient.gameComplete(WinType.WIN);
      } else {
        NetworkClient.gameComplete(WinType.LOSE);
      }
    } else if (GameLogic.getGameMode() == GameMode.LocalAI && AI.getRole() == winner) {
      NetworkClient.handleChat(AI.getWinningQuip(), "AI", false);
    }
    gameLogic.reset();

  }

  public void recieveForfeit() {
    gameOver(GameLogic.getLocalPlayer().getRole(), null);
  }

  public void forfeit() {
    GameMode mode = GameLogic.getGameMode();
    if (mode == GameMode.Multiplayer) {
      gameOver(GameLogic.getRemotePlayer().getRole(), null);
    } else if (mode == GameMode.LocalMultiplayer) {
      if (gameLogic.getCurrentPlayerRole() == PlayerRole.PlayerOne) {
        gameOver(PlayerRole.PlayerTwo, null);
      } else {
        gameOver(PlayerRole.PlayerOne, null);
      }
    } else { // AI
      gameOver(PlayerRole.PlayerTwo, null);
    }
  }

  public void recieveDrawRequest() {
    drawRequest.setVisible(true);
  }

  public void staleMate() {
    gameOver(PlayerRole.None, null);
  }

}
