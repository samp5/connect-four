package controller.utils;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class RecentConnection {
  String ip;
  int port;
  String name;

  public RecentConnection(String ip, int port, String name) {
    this.ip = ip;
    this.port = port;
    this.name = name;
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

              // construct our HBox and label
              VBox item = new VBox(connectionLabel, details);

              // Add the appropriate style classes
              connectionLabel.getStyleClass().add("recent-connection-cell-text-selected");
              details.getStyleClass().add("recent-connection-cell-text-selected");
              item.getStyleClass().add("recent-connection-cell-selected");

              // set the graphic for this cell
              setGraphic(item);

            } else {
              Text connectionLabel = new Text(
                  connection.name);
              Text details = new Text(
                  "\tIP: " + connection.getIp() + "Port:" + String.valueOf(connection.getPort()));
              // construct our HBox and label
              VBox item = new VBox(connectionLabel, details);

              // Add the appropriate style classes
              connectionLabel.getStyleClass().add("recent-connection-cell-text");
              details.getStyleClass().add("recent-connection-cell-text");
              item.getStyleClass().add("recent-connection-cell");

              item.setOnMouseEntered(e -> {
              });
              item.setOnMouseExited(e -> {
              });

              // set the graphic for this cell
              setGraphic(item);
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

}
