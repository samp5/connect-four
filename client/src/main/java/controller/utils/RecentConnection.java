package controller.utils;

import java.io.Serializable;
import java.time.LocalTime;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class RecentConnection implements Comparable<RecentConnection>, Serializable {
  String ip;
  int port;
  String name;
  LocalTime lastConnected = LocalTime.MAX;

  public RecentConnection(String ip, int port, String name) {
    this.ip = ip;
    this.port = port;
    this.name = name;
  }

  public void updateLastConnected() {
    lastConnected = LocalTime.now();
  }

  public static class ConnectionCellFactory
      implements Callback<ListView<RecentConnection>, ListCell<RecentConnection>> {

    @Override
    public ListCell<RecentConnection> call(ListView<RecentConnection> param) {

      return new ListCell<>() {

        /**
         * custom {@code updateItem} function for {@code ListCell<City>}
         */
        @Override
        public void updateItem(RecentConnection connection, boolean empty) {
          super.updateItem(connection, empty);

          if (empty) {
            setGraphic(null);

          } else if (connection != null) {

            // determine whether this city is selected
            boolean isSelected = getListView().getSelectionModel().getSelectedItem() == connection;

            if (isSelected) {
              Text connectionLabel = new Text(
                  "\u261e " + connection.name);
              Text details = new Text(
                  "\tIP: " + connection.getIp() + "Port:" + String.valueOf(connection.getPort()));

              HBox boxLeft = new HBox(connectionLabel);
              boxLeft.setAlignment(Pos.CENTER_LEFT);
              boxLeft.setMinWidth(240);

              BorderPane bp = new BorderPane();
              bp.setCenter(new ImageView(new Image("/assets/garbage.png", 25, 25, false, false)));
              HBox boxRight = new HBox(bp);
              boxRight.setAlignment(Pos.CENTER_RIGHT);
              boxRight.setMinWidth(25);

              HBox top = new HBox(boxLeft, boxRight);
              top.setMinWidth(280);

              bp.setOnMouseClicked(e -> {
                param.getItems().remove(connection);
              });
              bp.setOnMouseEntered(e -> {
                bp.setCenter(new ImageView(new Image("/assets/garbage_opened.png", 25, 25, false, false)));
              });
              bp.setOnMouseExited(e -> {
                bp.setCenter(new ImageView(new Image("/assets/garbage.png", 25, 25, false, false)));
              });

              // construct our HBox and label
              VBox info = new VBox(top, details);

              // Add the appropriate style classes
              connectionLabel.getStyleClass().add("recent-connection-cell-text-selected");
              details.getStyleClass().add("recent-connection-cell-text-selected");
              info.getStyleClass().add("recent-connection-cell-selected");

              // set the graphic for this cell
              setGraphic(info);

            } else {
              Text connectionLabel = new Text(
                  connection.name);
              Text details = new Text(
                  "\tIP: " + connection.getIp() + " Port:" + String.valueOf(connection.getPort()));
              HBox boxLeft = new HBox(connectionLabel);
              boxLeft.setAlignment(Pos.CENTER_LEFT);
              boxLeft.setMinWidth(240);

              HBox top = new HBox(boxLeft);
              top.setMinWidth(280);

              // construct our HBox and label
              VBox info = new VBox(top, details);

              // Add the appropriate style classes
              connectionLabel.getStyleClass().add("recent-connection-cell-text");
              details.getStyleClass().add("recent-connection-cell-text");
              info.getStyleClass().add("recent-connection-cell");

              // set the graphic for this cell
              setGraphic(info);
            }
          } else {

            /**
             * This case should never occur
             * a {@code City} should never be null
             */
            setGraphic(new Text("null"));
          }
        }
      };

    }
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  public String getName() {
    return name;
  }

  // Returns a negative integer, zero, or a positive integer as this object is
  // less than, equal to, or greater than the specified object.
  @Override
  public int compareTo(RecentConnection arg0) {
    if (this.lastConnected.isBefore(arg0.lastConnected)) {
      return -1;
    } else if (this.lastConnected.isAfter(arg0.lastConnected)) {
      return 1;
    } else {
      return this.name.compareTo(arg0.name);
    }
  }
}
