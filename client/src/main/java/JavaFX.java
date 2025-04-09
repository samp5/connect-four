import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
// import com.gluonhq.emoji.Emoji;
// import com.gluonhq.emoji.EmojiData;
// import com.gluonhq.emoji.EmojiLoaderFactory;
// import com.gluonhq.emoji.util.TextUtils;
import java.util.Optional;

/**
 * Main Application Class.
 * likely should not be referenced.
 */
public class JavaFX extends Application {

  public static void main(String[] args) {
    // attempt to connect to localhost server
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    SceneManager.initialize(primaryStage);
    SceneManager.showScene("menu.fxml");
  }
}
