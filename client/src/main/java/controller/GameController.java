package controller;

import controller.utils.Point;
import java.util.Optional;

import controller.GameController.Cloud.CloudType;
import controller.utils.BoardPosition;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
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
      Small,
      Large;

      private final String[] smallClouds = { "cloud1.png", "cloud2.png" };
      private final String[] largeClouds = { "cloud3.png" };

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

  /**
   * This function gets called by the FXMLLoader when the fxml gets,, loaded
   *
   */
  public void initialize() {
    gameLogic = new GameLogic();

    // registration
    GameLogic.setLocalPlayer(new Player("dummy1", 0));
    GameLogic.setRemotePlayer(new Player("dummy2", 1));
    GameLogic.setCurrentPlayerRole(PlayerRole.PlayerOne);
    GameLogic.setLocalPlayerRole(PlayerRole.PlayerOne);

    setCustomCursors();
    buildClouds();

    NetworkClient.bindGameController(this);
    foregroundPane.toFront();
    backgroundPane.toBack();
    gamePaneBackground.toBack();
    overlayPane.toFront();
    overlayPane.setMouseTransparent(true);
    gamePaneBackground.setBackground(
        new Background(new BackgroundImage(new Image("/assets/game_background.png"), BackgroundRepeat.NO_REPEAT,
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
    chipPane1.setCursor(new ImageCursor(new Image("/assets/hand_cursor.png")));
    chipPane2.setCursor(new ImageCursor(new Image("/assets/hand_cursor.png")));
  }

  private void buildClouds() {
    Cloud[] clouds = {
        new Cloud(CloudType.Large), new Cloud(CloudType.Small),
        new Cloud(CloudType.Small), new Cloud(CloudType.Large),
        new Cloud(CloudType.Large), new Cloud(CloudType.Small),
        new Cloud(CloudType.Large), new Cloud(CloudType.Large) };

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
      Path path = new Path();

      double yCoord = minAcceptableCloud + Math.random() * (maxAcceptableCloud - minAcceptableCloud);

      path.getElements().add(new MoveTo(-c.type.getWidth(), yCoord));
      path.getElements().add(new LineTo(CoordUtils.gamePaneWidth + c.type.getWidth(), yCoord));

      PathTransition pathTransition = new PathTransition();
      pathTransition.setDuration(Duration.seconds(Math.random() * (maxTimeAcross - minTimeAcross) + minTimeAcross));
      pathTransition.setPath(path);
      pathTransition.setNode(c);
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

      pTrans.setOnFinished(k -> {
        midgroundPane.getChildren().remove(dropHint);
        dropHint = null;
        // update our logic
        gameLogic.placePiece(rowCol.getRow(), rowCol.getColumn(),
            gameLogic.getCurrentPlayerRole());

        if (!gameIsOver()) {
          gameLogic.switchPlayer();
        }
      });
      NetworkClient.sendMove(rowCol.getColumn());
      draggedPiece = null;
    });

  }

  private void handleDrag(MouseEvent e, PlayerRole player) {
    // stop a player from clicking when it is
    // 1. not their turn
    // 2. not their pile
    if (GameLogic.getLocalPlayer().getRole() != gameLogic.getCurrentPlayerRole()
        || GameLogic.getLocalPlayer().getRole() != player) {
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

      Point topOfCol = CoordUtils.topOfColumn(col);
      Point chipHolder = CoordUtils.chipHolder(player.getRole());
      Piece toPlay = new Piece(player.getRole(), chipHolder);

      overlayPane.getChildren().add(toPlay);

      Path path = new Path();
      path.getElements().addAll(new MoveTo(chipHolder.getX(), chipHolder.getY()),
          new LineTo(topOfCol.getX(), topOfCol.getY()));

      PathTransition pathTransition = new PathTransition(Duration.millis(1.5 * chipHolder.distanceTo(topOfCol)), path,
          toPlay);
      pathTransition.play();

      pathTransition.setOnFinished(e -> {
        overlayPane.getChildren().remove(toPlay);
        Point topSlot = CoordUtils.fromRowCol(GameLogic.numRows() - 1, col);
        Point finalPosition = CoordUtils.fromRowCol(r, col);
        Piece toDrop = new Piece(player.getRole(), topSlot);

        midgroundPane.getChildren().add(toDrop);

        Path dropPath = new Path(new MoveTo(topSlot.getX(), topSlot.getY()),
            new LineTo(finalPosition.getX(), finalPosition.getY()));
        PathTransition dropTransition = new PathTransition(Duration.millis(1.5 * topSlot.distanceTo(finalPosition)),
            dropPath,
            toDrop);
        dropTransition.play();

        dropTransition.setOnFinished(f -> {
          gameLogic.placePiece(gameLogic.getAvailableRow(col).orElse(6), col, player.getRole());

          if (!gameIsOver()) {
            gameLogic.switchPlayer();
          }

        });

      });

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
      PathTransition pathTransition = new PathTransition();
      pathTransition.setDuration(Duration.millis(1.5 * p.position.distanceTo(topOfCol) + Math.random() * 20));
      pathTransition.setPath(path);
      pathTransition.setNode(p);
      pathTransition.play();
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
    }
    System.out.println(winner + "wins!");
    gameLogic.reset();

  }

  private void staleMate() {
    System.out.println("Stalemate!!");
  }

}
