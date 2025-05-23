package controller;

import controller.utils.Point;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import controller.utils.BoardPosition;
import controller.utils.CoordSystem;
import controller.utils.CoordUtils;
import controller.utils.GameSettings;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import logic.GameLogic;
import logic.GameLogic.GameMode;
import logic.AI;
import network.NetworkClient;
import network.Player;
import network.Message.WinType;
import network.Player.PlayerRole;
import utils.AudioManager;
import utils.AudioManager.SoundEffect;
import utils.CursorManager;
import utils.SceneManager;
import utils.SceneManager.SceneSelections;
import utils.ToolTipHelper;
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
public class GameController extends Controller {

  private Piece draggedPiece = null;
  private Piece dropHint = null;
  private boolean canMove = true;

  @FXML
  private StackPane game;

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
  BorderPane settingsButton;
  @FXML
  private ImageView clouds;

  @FXML
  private ImageView gameBG;
  private int grassState = 0;

  @FXML
  private ImageView flameBorder;
  private int flameState = 0;
  private Timer flameTimer;

  @FXML
  private ImageView redTurnIndicator;
  @FXML
  private ImageView blueTurnIndicator;
  private int indicatorState = 0;

  @FXML
  private Pane drawRequest;
  @FXML
  private Button drawRequestAccept;
  @FXML
  private Button drawRequestReject;

  @FXML
  private Pane resignRequest;
  @FXML
  private Button resignRequestAccept;
  @FXML
  private Button resignRequestReject;

  @FXML
  private Pane rematch;
  @FXML
  private Text rematchTitle;
  @FXML
  private Button rematchYes;
  @FXML
  private Button rematchToLobby;
  @FXML
  private Button rematchMainMenu;

  private GameLogic gameLogic;

  public void initialize() {
    gameLogic = new GameLogic();
    updateTurnIndicator();

    CursorManager.setHandCursor(settingsButton, drawRequestAccept, drawRequestReject, resignRequestAccept,
        resignRequestReject, rematchYes, rematchToLobby, rematchMainMenu);
    AudioManager.setAudioButton(settingsButton, drawRequestAccept, drawRequestReject, resignRequestAccept,
        resignRequestReject, rematchYes, rematchToLobby, rematchMainMenu);

    animateClouds();
    animateGrass();
    animateTurnIndicators();

    NetworkClient.bindGameController(this);
    MainController.attachGame(this);
    settingsButton.setBackground(SettingsController.getButtonBackground(40));

    Tooltip.install(settingsButton, ToolTipHelper.make("Settings"));

    overlayPane.setMouseTransparent(true);
    setHandlers();

    if (GameLogic.getGameMode() == GameMode.LocalAI) {
      checkAIMaxMode();
      showPlayerRoles();
    }
  }

  public void checkAIMaxMode() {
    if (AI.getDifficulty() == 7) {
      setAIMaxMode();
    } else {
      setAIDefaultMode();
    }
  }

  public void setAIMaxMode() {
    AudioManager.playBossMusic();

    flameBorder.setVisible(true);
    flameTimer = new Timer();
    flameTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        flameState = (flameState + 1) % 3;
        try {
          Platform.runLater(() -> {
            // don't ask me why this works,,
            flameBorder.setViewport(new Rectangle2D(-1800 + (1800 * flameState), 0, 5400, 1800));
          });
        } catch (Exception e) {
        }
      }
    }, 0, 200);
  }

  public void setAIDefaultMode() {
    AudioManager.playMainTheme();
    if (flameTimer != null) {
      flameTimer.cancel();
      flameBorder.setVisible(false);
    }
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
    drawRequestAccept.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        drawRequestAccept.getOnAction().handle(null);
    });

    drawRequestReject.setOnAction(e -> {
      drawRequest.setVisible(false);
      NetworkClient.replyDrawRequest(false);
    });
    drawRequestReject.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        drawRequestReject.getOnAction().handle(null);
    });

    resignRequestAccept.setOnAction(e -> {
      resignRequest.setVisible(false);
      NetworkClient.replyResignRequest(true);
    });
    resignRequestAccept.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        resignRequestAccept.getOnAction().handle(null);
    });

    resignRequestReject.setOnAction(e -> {
      resignRequest.setVisible(false);
      NetworkClient.replyResignRequest(false);
    });
    resignRequestReject.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        resignRequestReject.getOnAction().handle(null);
    });

    rematchYes.setOnAction(e -> {
      rematchYes.setText("Waiting...");
      rematchYes.setDisable(true);
      if (GameLogic.getGameMode() == GameMode.Multiplayer) {
        NetworkClient.rematchRequest();
      }
    });
    rematchYes.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        rematchYes.getOnAction().handle(null);
        gamePane.requestFocus();
      }
    });

    rematchMainMenu.setOnAction(e -> {
      if (GameLogic.getGameMode() == GameMode.Multiplayer) {
        NetworkClient.disconnect();
      }
      SceneManager.showScene(SceneSelections.MAIN_MENU);
    });
    rematchMainMenu.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        rematchMainMenu.getOnAction().handle(null);
    });

    rematchToLobby.setOnAction(e -> {
      if (GameLogic.getGameMode() == GameMode.Multiplayer) {
        NetworkClient.returnToLobby();
      }
      rematch.setVisible(false);
      SceneManager.showScene(SceneSelections.SERVER_MENU);
    });
    rematchToLobby.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER)
        rematchToLobby.getOnAction().handle(null);
    });

  }

  /*************************************************************************
   * STATIC STATE ACCESSORS
   */
  public static void setGameMode(GameMode mode) {
    GameLogic.setGameMode(mode);
  }

  public void setLocalPlayer(PlayerRole local) {
    GameLogic.setLocalPlayerRole(local);
  }

  /*************************************************************************
   * EVENT HANDLERS
   *
   */
  private void handleDrag(MouseEvent e, PlayerRole playerPile) {
    if (!canMove || !GameLogic.getGameMode().canMove(gameLogic, playerPile)) {
      return;
    }

    if (draggedPiece == null) {
      draggedPiece = new Piece(gameLogic.getCurrentPlayerRole(),
          new Point(e.getSceneX(), e.getSceneY(), CoordSystem.GamePane));
      overlayPane.getChildren().addAll(draggedPiece);
    }

    if (GameLogic.getGameMode() == GameMode.LocalMultiplayer) {
      // listen,,
      draggedPiece.setPoint(e.getSceneX() - 180, e.getSceneY());
    } else {
      draggedPiece.setPoint(e.getSceneX(), e.getSceneY());
    }

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

  /*************************************************************************
   * ANIMATION HELPERS
   *
   */
  public void animateClouds() {
    Path cloudPath = new Path(new MoveTo(0, 0), new LineTo(1440, 0));
    PathTransition cloudAnimation = new PathTransition(Duration.seconds(120), cloudPath, clouds);
    cloudAnimation.play();

    cloudAnimation.onFinishedProperty().set(e -> {
      Path fullPath = new Path(new MoveTo(-720, 0), new HLineTo(1440));
      PathTransition fullAnimation = new PathTransition(Duration.seconds(240), fullPath, clouds);
      fullAnimation.setCycleCount(PathTransition.INDEFINITE);
      fullAnimation.play();
    });
  }

  private PathTransition animateToTop(BoardPosition rowCol, Point chipPos, PlayerRole role) {
    if (draggedPiece == null) {
      draggedPiece = new Piece(role, CoordUtils.chipHolder(role));
      overlayPane.getChildren().add(draggedPiece);
    }
    // lock playing a move until animation is done
    canMove = false;
    CursorManager.setPointerCursor(chipPane1, chipPane2);

    // calc the point for the top of this column
    Point topOfCol = CoordUtils.topOfColumn(rowCol.getColumn());
    double topX = topOfCol.getX();
    double topY = topOfCol.getY();

    // construct a path
    Path path = new Path();
    path.getElements().add(new MoveTo(chipPos.getX(), chipPos.getY()));
    path.getElements().add(new LineTo(topX + CoordUtils.pieceRadius, topY));

    // build the animation
    PathTransition pathTransition = new PathTransition();
    pathTransition.setDuration(Duration.millis(1 * chipPos.distanceTo(topOfCol)));
    pathTransition.setPath(path);
    pathTransition.setNode(draggedPiece);
    return pathTransition;
  }

  private void animateDrop(Point from, BoardPosition rowCol, PlayerRole role) {
    double fromX = from.getX();
    double fromY = from.getY();

    Piece pieceToDrop = new Piece(role, from);
    if (gameLogic.checkWin(role).isPresent()
        || (GameLogic.getGameMode() == GameMode.LocalAI && AI.isMaxDifficulty() && AI.getRole() == role)) {
      pieceToDrop.setFlaming();
    }
    Point finalPosition = CoordUtils.fromRowCol(rowCol.getRow(), rowCol.getColumn());
    midgroundPane.getChildren().add(pieceToDrop);

    Path pth = new Path(
        new MoveTo(fromX, fromY - pieceToDrop.getRadius()),
        new LineTo(finalPosition.getX(), finalPosition.getY()));

    // build the animation
    PathTransition pTrans = new PathTransition();
    pTrans.setDuration(Duration.millis(1500));
    pTrans.setPath(pth);
    pTrans.setNode(pieceToDrop);
    pTrans.setInterpolator(new Interpolator() {
      // I made a desmos graph to find these values:
      // https://www.desmos.com/calculator/bbsip7ftzt
      @Override
      public double curve(double t) {
        if (t < (.869)) {
          return 3. * Math.pow(Math.E, 4.7 * (t - 1.1)) - 0.017;
        } else if (t < .958) {
          return Math.pow((5.2 * t) - 4.75, 2) + .946;
        } else {
          return Math.pow((6.4 * t) - 6.265, 2) + .982;
        }
      }
    });

    PauseTransition soundEffect = new PauseTransition(Duration.millis(1300));
    soundEffect.setOnFinished(e -> {
      AudioManager.playSoundEffect(SoundEffect.CHIP_DROP);
    });

    soundEffect.play();
    pTrans.play();

    pTrans.setOnFinished(k -> {
      pieceToDrop.extinguish();
      // chack for game over
      if (checkGameOver(role)) {
        return;
      }

      // run our AI if we need to
      if (GameLogic.getGameMode() == GameMode.LocalAI
          && gameLogic.getCurrentPlayerRole() == AI.getRole()) {
        int aiCol = AI.bestColumn(gameLogic.getBoard());
        BoardPosition bp = new BoardPosition(gameLogic.getAvailableRow(aiCol).get(), aiCol);
        handleMove(AI.bestColumn(gameLogic.getBoard()), AI.getRole());
        if (AI.shouldQuip()) {
          NetworkClient.handleChat(AI.getQuip(), AI.getName(), false);
        }
        animateMove(bp, CoordUtils.chipHolder(AI.getRole()),
            AI.getRole());
      } else {
        canMove = true;
        // mostly used for cursor updates
        updateTurnIndicator();
      }
    });

  }

  private void animateMove(BoardPosition rowCol, Point chipPos, PlayerRole role) {
    PathTransition pathTransition = animateToTop(rowCol, chipPos, role);
    pathTransition.play();

    Point topOfCol = CoordUtils.topOfColumn(rowCol.getColumn());

    pathTransition.setOnFinished(f -> {
      overlayPane.getChildren().remove(draggedPiece);
      animateDrop(topOfCol, rowCol, role);
      draggedPiece = null;
    });
  }

  private void animateGrass() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        grassState = (grassState + 1) % 2;
        try {
          Platform.runLater(() -> {
            if (GameLogic.getGameMode() == GameMode.LocalMultiplayer) {
              gameBG.setViewport(new Rectangle2D((3840 * grassState), 0, 3840, 2560));
            } else {
              gameBG.setViewport(new Rectangle2D((1536 * grassState), 0, 1536, 1536));
            }
          });
        } catch (Exception e) {
        }
      }
    }, 0, 1000);
  }

  private void animateTurnIndicators() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        indicatorState = (indicatorState + 1) % 17;
        try {
          Platform.runLater(() -> {
            redTurnIndicator.setViewport(new Rectangle2D(0, (200 * indicatorState), 200, 200));
            blueTurnIndicator.setViewport(new Rectangle2D(0, (200 * indicatorState), 200, 200));
          });
        } catch (Exception e) {
        }
      }
    }, 0, 66);
  }

  /************************************************************************
   * PIECE CLASS
   *
   */
  private class Piece extends Circle {
    public Point position;
    public PlayerRole role;
    private ImageView flamin;
    private int flamin_state = 0;
    private Timer flamin_timer;

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

    public void setFlaming() {
      flamin = new ImageView(
          new Image("/assets/flaming_" + (role == PlayerRole.PlayerOne ? "red" : "blue") + "_chip.png", 123.75, 652.5,
              false, false));

      /**
       *
       *
       * A chip is 90 x 90 in Javafx and 16 by 16 pixels (by design) so there are
       * 5.625 jp/p (javafx pixels per pixel)
       *
       * 
       * The flaming chip drawing is 22 x 29 pixels (by design) so
       * to get the imageview of the flame to overlap with the original chip we offset
       * the y property
       * like
       * ( (29p - 16p) + (16p / 2) )* 5.625 jp/p
       * similarly the x property is
       * ( (22p - 16p) / 2 + 8 ) * 5.625 jp/p
       *
       * and so on
       *
       */
      final double f_p_width = 22;
      final double f_p_height = 29;
      final double chip_p_radius = 8;
      final double jp_p_p = 5.625; // java pixels per pixel

      final double width = f_p_width * jp_p_p;
      final double height = f_p_height * jp_p_p;

      midgroundPane.getChildren().add(flamin);
      flamin.xProperty()
          .bind(centerXProperty().subtract(((f_p_width - chip_p_radius * 2) / 2 + chip_p_radius) * jp_p_p));
      flamin.yProperty()
          .bind(centerYProperty().subtract(((f_p_height - chip_p_radius * 2) + (chip_p_radius)) * jp_p_p));
      flamin.translateXProperty().bind(translateXProperty());
      flamin.translateYProperty().bind(translateYProperty());
      flamin.setViewport(new Rectangle2D(0, 0, f_p_width * jp_p_p, f_p_height * jp_p_p));
      super.setFill(Color.TRANSPARENT);
      flamin_timer = new Timer();
      flamin_timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          Platform.runLater(() -> {
            if (flamin != null) {
              flamin.setViewport(new Rectangle2D(0, flamin_state * height + .3,
                  width, height));
              flamin_state = (flamin_state + 1) % 4;
            }
          });
        }
      }, 0, 100);
    }

    public void extinguish() {
      if (flamin_timer != null) {
        flamin_timer.cancel();
        flamin_timer = null;
        flamin_state = 0;
        midgroundPane.getChildren().remove(flamin);
        flamin = null;
        if (role == PlayerRole.PlayerOne) {
          super.setFill(new ImagePattern(new Image("/assets/red_chip.png")));
        } else {
          super.setFill(new ImagePattern(new Image("/assets/blue_chip.png")));
        }
      }
    }
  }

  /*************************************************************************
   * INTERNAL GAME UPDATES
   */
  private void handleMove(int col, PlayerRole role) {
    gameLogic.getAvailableRow(col).ifPresentOrElse((r) -> {
      gameLogic.placePiece(gameLogic.getAvailableRow(col).orElse(6), col, role);
      gameLogic.switchPlayer();
      updateTurnIndicator();
    }, () -> {
      System.err.println("Recieved invalid move");
    });
  }

  private void updateTurnIndicator() {
    switch (gameLogic.getCurrentPlayerRole()) {
      case None:
        redTurnIndicator.setVisible(false);
        blueTurnIndicator.setVisible(false);
        CursorManager.setPointerCursor(chipPane1, chipPane2);
        break;
      case PlayerOne:
        redTurnIndicator.setVisible(true);
        blueTurnIndicator.setVisible(false);
        CursorManager.setHandCursor(chipPane1);
        CursorManager.setPointerCursor(chipPane2);
        break;
      case PlayerTwo:
        redTurnIndicator.setVisible(false);
        blueTurnIndicator.setVisible(true);
        CursorManager.setPointerCursor(chipPane1);
        CursorManager.setHandCursor(chipPane2);
        break;
    }

  }

  private void resetGameState() {
    gameLogic.reset();
    updateTurnIndicator();
  }

  private boolean checkGameOver(PlayerRole role) {
    Optional<BoardPosition[]> winningComboOpt = gameLogic.checkWin(role);

    if (winningComboOpt.isPresent()) {
      gameOver(role, winningComboOpt.get());
      return true;
    } else if (gameLogic.staleMate()) {
      staleMate();
      return true;
    }
    return false;
  }

  private void displayLargeText(String fileName, Duration duration, Optional<EventHandler<ActionEvent>> onFinish) {
    if (fileName == null) {
      return;
    }

    Image textImage = new Image(fileName, 0, 96, true, false);
    ImageView largeText = new ImageView(textImage);

    overlayPane.getChildren().add(largeText);

    largeText.setX((CoordUtils.gamePaneWidth - textImage.widthProperty().get()) / 2);
    largeText.setY(100);

    PauseTransition pt = new PauseTransition(duration);
    pt.setOnFinished(g -> {
      overlayPane.getChildren().remove(largeText);
      onFinish.ifPresent(f -> {
        f.handle(g);
      });
    });
    pt.play();
  }

  private void displayWinner(PlayerRole winner) {
    String loc = null;
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
    }
    switch (GameLogic.getGameMode()) {
      case LocalAI:
        if (winner == AI.getRole()) {
          AudioManager.playSoundEffect(SoundEffect.LOSE);
        } else {
          AudioManager.playSoundEffect(SoundEffect.WIN);
        }
        break;
      case LocalMultiplayer:
        if (winner != PlayerRole.None)
          AudioManager.playSoundEffect(SoundEffect.WIN);
        else
          AudioManager.playSoundEffect(SoundEffect.LOSE);
        break;
      case Multiplayer:
        if (winner == GameLogic.getLocalPlayer().getRole()) {
          AudioManager.playSoundEffect(SoundEffect.WIN);
        } else {
          AudioManager.playSoundEffect(SoundEffect.LOSE);
        }
        break;
      case None:
        break;
    }

    displayLargeText(loc, Duration.millis(3000), Optional.of(g -> {

      // we have to grab a copy or else we get a concurrent modification exception
      var wps = midgroundPane.getChildren().stream().filter(Piece.class::isInstance).map(o -> (Piece) o)
          .collect(Collectors.toList());
      for (Piece wp : wps) {
        wp.extinguish();
      }
      midgroundPane.getChildren().setAll();
      canMove = true;
    }));
  }

  private void gameOver(PlayerRole winner, BoardPosition[] winningPositions) {
    canMove = false;
    GameLogic.setCurrentPlayerRole(PlayerRole.None);
    updateTurnIndicator();
    Object[] pieces = midgroundPane.getChildren().toArray();

    if (winningPositions != null) {
      Piece[] winningPieces = new Piece[4];
      for (int i = 0; i < 4; i++) {
        BoardPosition bp = winningPositions[i];
        winningPieces[i] = new Piece(winner, CoordUtils.fromRowCol(bp.getRow(),
            bp.getColumn()));
        winningPieces[i].setFlaming();
      }
      midgroundPane.getChildren().addAll(winningPieces);
    }

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
        displayWinner(winner);
      });

      pathTransition.play();
    }

    if (pieces.length == 0) {
      displayWinner(winner);
    }

    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      switch (winner) {
        case None:
          NetworkClient.gameComplete(WinType.DRAW);
          break;
        case PlayerOne:
          NetworkClient.gameComplete(WinType.WIN);
          break;
        case PlayerTwo:
          NetworkClient.gameComplete(WinType.LOSE);
          break;
        default:
          break;
      }
      rematch.setVisible(true);
      gamePane.requestFocus();
      return;
    } else {
      if (GameLogic.getGameMode() == GameMode.LocalAI && AI.getRole() == winner) {
        NetworkClient.handleChat(AI.getWinningQuip(), AI.getName(), false);
      }
      resetGameState();
    }

  }

  /*****************************************************************
   * GAME STATE INTERFACE
   */
  public void recieveDrawRequest() {
    drawRequest.setVisible(true);
  }

  public void showPlayerRoles() {
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      switch (GameLogic.getLocalPlayer().getRole()) {
        case PlayerOne:
          displayLargeText("/assets/red_player.png", Duration.millis(4000), Optional.empty());
          break;
        case PlayerTwo:
          displayLargeText("/assets/blue_player.png", Duration.millis(4000), Optional.empty());
          break;
        default:
          break;
      }
    } else if (GameLogic.getGameMode() == GameMode.LocalAI) {
      displayLargeText("/assets/red_player.png", Duration.millis(2000), Optional.empty());
    }
  }

  public void resign() {
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

  public void recieveResign() {
    switch (GameLogic.getGameMode()) {
      case LocalMultiplayer:
        gameOver(gameLogic.getCurrentPlayerRole(), null);
        break;
      case Multiplayer:
        gameOver(GameLogic.getLocalPlayer().getRole(), null);
        break;
      case LocalAI:
      case None:
      default:
        break;
    }
  }

  public void rematch() {
    if (GameLogic.getGameMode() == GameMode.Multiplayer) {
      // hide our menu
      rematch.setVisible(false);
      // reset our text
      rematchYes.setText("Yes");
      rematchYes.setDisable(false);
      // reset the button
      rematchYes.setOnAction(e -> {
        rematchYes.setText("Waiting...");
        rematchYes.setDisable(true);
        if (GameLogic.getGameMode() == GameMode.Multiplayer) {
          NetworkClient.rematchRequest();
        }
      });
      // reset the game logic
      resetGameState();
      gameLogic.swapLocalRemotePlayerRoles();
      showPlayerRoles();
    }
  }

  public void recieveRematchRequest() {
    // edge case of network being slow
    if (rematchYes.getText().equals("Wating...")) {
      rematch();
    } else {
      rematchYes.setOnAction(e -> {
        NetworkClient.acceptRematch();
        rematch();
      });
    }
  }

  public void recieveResignRequest() {
    resignRequest.setVisible(true);
  }

  public void recieveOpponentReconnect() {
    rematch.setVisible(false);
    rematchYes.setDisable(false);
    rematchYes.setText("Yes");
    rematchYes.setOnAction(e -> {
      rematchYes.setText("Wating...");
      if (GameLogic.getGameMode() == GameMode.Multiplayer) {
        NetworkClient.rematchRequest();
      }
    });
  }

  public void recieveOpponentDisconnect() {
    rematchTitle.setText("Opponent fled");
    rematchYes.setText("Wait...");
    rematchYes.setOnAction(e -> {
      rematchYes.setText("Waiting...");
      rematchYes.setDisable(true);
    });
    rematch.setVisible(true);
  }

  public void recieveOpponentReturnToLobby() {
    GameLogic.setCurrentPlayerRole(PlayerRole.None);
    updateTurnIndicator();
    rematch.setVisible(true);
    rematchYes.setDisable(true);
    rematchYes.setOnAction(e -> {
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

  public void staleMate() {
    gameOver(PlayerRole.None, null);
  }

}
