package controller.utils;

import java.util.Optional;

public class Point {
  double x;
  double y;
  CoordSystem relativeTo;

  public Optional<BoardPosition> onBoard() {
    return CoordUtils.toRowCol(this);
  }

  public Point(double x, double y, CoordSystem relative) {
    this.x = x;
    this.y = y;
    this.relativeTo = relative;
  }

  public double distanceTo(Point other) {
    if (this.relativeTo != other.relativeTo) {
      Point temp1 = CoordUtils.onGameScene(this);
      Point temp2 = CoordUtils.onGameScene(other);
      return Math.sqrt(Math.pow(temp1.x - temp2.x, 2) + Math.pow(temp1.y - temp2.y, 2));
    }
    return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
  }

  public Point copy() {
    return new Point(this.x, this.y, this.relativeTo);
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }
}
