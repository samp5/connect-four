package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import logic.GameLogic;
import logic.Piece;
import network.NetworkClient;

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


  @FXML
  private Canvas canvas; // maybe?
  private GameLogic gameLogic;

  private ArrayList<Piece> pieces;

  /**
   * This function gets called by the FXMLLoader when the fxml gets,, loaded
   *
   */
  public void initialize() {
    gameLogic = new GameLogic();
    pieces = new ArrayList<>();
    NetworkClient.bindGameController(this);
    // ... etc
  }

  // this will be harder because we will have to manually calculate everyting, BUT I think it will
  // be worth it because the Canvas is much more flexible
  @FXML
  private void handleColumnClick(ActionEvent event) {}

  @FXML
  private void handleColumnHover(ActionEvent event) {}

  private boolean makeMove(int col) {
    return false;
  }


  private void drawBoard() {
    // we should use images for the pieces and such
  }

  private void animatePieceDrop(Piece piece) {}

}
