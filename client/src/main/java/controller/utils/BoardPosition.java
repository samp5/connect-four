package controller.utils;

public class BoardPosition {
  int row;
  int col;

  public BoardPosition(int r, int c) {
    row = r;
    col = c;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return col;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public void setCol(int col) {
    this.col = col;
  }

}
