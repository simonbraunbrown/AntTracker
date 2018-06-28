package sample;


import javafx.scene.layout.BorderPane;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.layout.GridPane;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage)  {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            BorderPane root = (BorderPane) loader.load();
            //GridPane root = FXMLLoader.load(getClass().getResource("sample.fxml"));

            primaryStage.setTitle("Ant Tracker");
            primaryStage.setScene(new Scene(root, 1300, 1080));
            primaryStage.setResizable(false);
            primaryStage.show();

            Controller controller = loader.getController();
            controller.init();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {

                public void handle(WindowEvent we) {
                    controller.setClosed();
                }
            }));

        }

		catch(
    Exception e)

    {
        e.printStackTrace();
    }

}


    public static void main(String[] args) {

        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        launch(args);
    }
}
