package org.sandbox.starbat;

import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) {
        new StarBatGame(primaryStage).start();
    }

    public static void main(String[] args) {
        launch(Launcher.class);
    }
}
