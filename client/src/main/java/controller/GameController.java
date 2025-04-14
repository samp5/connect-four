package controller;

import controller.utils.Point;

import java.util.ArrayList;
import java.util.Optional;

import controller.GameController.Cloud.CloudType;
import controller.utils.BoardPosition;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Dimension2D;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import logic.GameLogic;
import logic.AI;
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
  private GameLogic gameLogic;

  private Button bestMoveButton;

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

  public class Cloud extends Rectangle {

    enum CloudType {
      Small, Large;

      private final String[] smallClouds = {"cloud1.png", "cloud2.png"};
      private final String[] largeClouds = {"cloud3.png"};

      public int getWidth() {
        switch (this) {
          case Large:
            return 200;
          case Small:
            return 100;
          default:
            return 0;
        }
      }

      public int getHeight() {
        switch (this) {
          case Large:
            return 100;
          case Small:
            return 100;
          default:
            return 0;
        }
      }

      public Image getImage() {
        switch (this) {
          case Large:
            return new Image("/assets/" + largeClouds[(int) (Math.random() * largeClouds.length)]);
          case Small:
            return new Image("/assets/" + smallClouds[(int) (Math.random() * largeClouds.length)]);
          default:
            return null;
        }
      }
    }

    CloudType type;

    Cloud(CloudType type) {
      super();
      super.setWidth(type.getWidth());
      super.setHeight(type.getHeight());
      super.setFill(new ImagePattern(type.getImage()));
      super.setX(-type.getWidth());
      super.setY(0);
      this.type = type;
    }
  }

  public void setLocalPlayer(PlayerRole local) {
    GameLogic.setLocalPlayerRole(local);
  }

  public void initialize() {
    gameLogic = new GameLogic();
    bestMoveButton = new Button("Best move");
    bestMoveButton.setOnAction(e -> {
      System.out.println(gameLogic.getBestMove());
    });
    foregroundPane.getChildren().add(bestMoveButton);

    setCustomCursors();
    // buildClouds();
    //

    NetworkClient.bindGameController(this);
    foregroundPane.toFront();
    backgroundPane.toBack();
    gamePaneBackground.toBack();
    overlayPane.toFront();
    overlayPane.setMouseTransparent(true);
    gamePaneBackground.setBackground(
        new Background(new BackgroundImage(new Image("/assets/game_background2.png"),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(720, 720, false, false, false, false))));
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

    Image handCursor = new Image("/assets/hand_cursor.png");
    Dimension2D dim = ImageCursor.getBestSize(handCursor.getWidth(), handCursor.getHeight());
    Image handCursorScaled =
        new Image("/assets/hand_cursor.png", dim.getWidth(), dim.getHeight(), false, false);

    chipPane1.setCursor(new ImageCursor(handCursorScaled, handCursorScaled.getWidth() / 2,
        handCursorScaled.getHeight() / 2));
    chipPane2.setCursor(new ImageCursor(handCursorScaled, handCursorScaled.getWidth() / 2,
        handCursorScaled.getHeight() / 2));
  }

  private void buildClouds() {
    Cloud[] clouds = {
        new Cloud(CloudType.Large), new Cloud(CloudType.Small),
        new Cloud(CloudType.Small), new Cloud(CloudType.Large),
        new Cloud(CloudType.Large), new Cloud(CloudType.Large)};

    gamePaneBackground.getChildren().addAll(clouds);
    buildCloudAnimation(clouds);
  }

  private void buildCloudAnimation(Cloud[] clouds) {
    double maxAcceptableCloud = 300;
    double minAcceptableCloud = 10;
    double maxTimeAcross = 90;
    double minTimeAcross = 75;

    for (int i = 0; i < clouds.length; i++) {
      Cloud c = clouds[i];

      double yCoord =
          minAcceptableCloud + Math.random() * (maxAcceptableCloud - minAcceptableCloud);
      Path path = new Path(new MoveTo(-c.type.getWidth(), yCoord),
          new LineTo(CoordUtils.gamePaneWidth + c.type.getWidth(), yCoord));

      PathTransition pathTransition = new PathTransition(
          Duration.seconds(Math.random() * (maxTimeAcross - minTimeAcross) + minTimeAcross), path,
          c);
      pathTransition.setDelay(Duration.seconds((maxTimeAcross / clouds.length) * i));
      pathTransition.play();

      pathTransition.setOnFinished(e -> {
        pathTransition.playFromStart();
      });
    }
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
    PlayerRole role = getLocalPlayer().getRole();
    Point startPos =
        new Point(draggedPiece.getCenterX(), draggedPiece.getCenterY(), CoordSystem.GamePane);

    handleMove(rowCol.getColumn(), role);
    animateMove(rowCol, startPos, role);

    // send the move to the client
    if (GameLogic.getRemotePlayer() != null) {
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

  private void animateMove(BoardPosition rowCol, Point chipPos, PlayerRole role) {
    // lock playing a move until animation pt 1 is done
    canMove = false;

    // calc the point for the top of this column
    Point topOfCol = CoordUtils.topOfColumn(rowCol.getColumn());
    double topX = topOfCol.getX();
    double topY = topOfCol.getY() - draggedPiece.getRadius();

    // construct a path
    Path path = new Path();
    path.getElements().add(new MoveTo(chipPos.getX(), chipPos.getY()));
    path.getElements().add(new LineTo(topX + draggedPiece.getRadius(),
        topY));

    // build the animation
    PathTransition pathTransition = new PathTransition();
    pathTransition.setDuration(Duration.millis(1 * chipPos.distanceTo(topOfCol)));
    pathTransition.setPath(path);
    pathTransition.setNode(draggedPiece);
    pathTransition.play();

    pathTransition.setOnFinished(f -> {
      overlayPane.getChildren().remove(draggedPiece);

      Point topSlot = CoordUtils.fromRowCol(GameLogic.numRows() - 1, rowCol.getColumn());
      Piece pieceToDrop = new Piece(role, topSlot.copy());
      Point finalPosition = CoordUtils.fromRowCol(rowCol.getRow(), rowCol.getColumn());
      midgroundPane.getChildren().add(pieceToDrop);

      Path pth = new Path();
      pth.getElements().add(new MoveTo(topX, topY - pieceToDrop.getRadius()));
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
      });

      draggedPiece = null;
      canMove = true;
    });
  }

  private void handleDrag(MouseEvent e, PlayerRole player) {
    // stop a player from clicking when it is
    // 1. not their turn
    // 2. not their pile
    // 3. otherwise blocked
    if (GameLogic.getLocalPlayer().getRole() != gameLogic.getCurrentPlayerRole()
        || GameLogic.getLocalPlayer().getRole() != player
        || !canMove) {
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

  public ArrayList<Integer> getMoveHistory() {
    return this.gameLogic.getMoveHistory();
  }

  public void restoreGameBoard(ArrayList<Integer> moves) {
    for (Integer col : moves) {
      recieveMove(col);
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

    GameLogic.setCurrentPlayerRole(PlayerRole.None);
    Piece[] winningPieces = new Piece[4];
    for (int i = 0; i < 4; i++) {
      BoardPosition bp = winningPositions[i];
      winningPieces[i] = new Piece(winner, CoordUtils.fromRowCol(bp.getRow(),
          bp.getColumn()));
    }
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

        ImageView winnerImg = new ImageView(new Image(
            "/assets/" + (winner == PlayerRole.PlayerOne ? "red" : "blue") + "-wins.png",
            CoordUtils.gamePaneWidth - 40, 0, true, false));

        overlayPane.getChildren().add(winnerImg);

        winnerImg.setX(20);
        winnerImg.setY(100);

        PauseTransition pt = new PauseTransition(Duration.millis(4000));
        pt.setOnFinished(g -> {
          overlayPane.getChildren().remove(winnerImg);
        });
        pt.play();
      });

      pathTransition.play();
    }
    System.out.println(winner + "wins!");
    gameLogic.reset();

  }

  private void staleMate() {
    System.out.println("Stalemate!!");
  }

}
