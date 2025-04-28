import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
// NO SCENE BUILDER!

public class Main extends Application {
    private final int WIDTH = 1920;
    private final int HEIGHT = 1080;

//EREN VURAL

    @Override
    public void start(Stage primaryStage) {
        Button startButton = getStartButton();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        StackPane gameRoot = new StackPane();
        Scene gameScene = getGameScene(gameRoot);


        root.setStyle("-fx-background-color: #d3cfc1;"); // nice blue
        root.getChildren().add(startButton);

        primaryStage.setTitle("Tower Game");
        primaryStage.setScene(scene);  // Set scene on the stage
        primaryStage.show();

        startButton.setOnAction(e -> {
            primaryStage.setScene(gameScene);
        });

    }

    private Scene getGameScene(StackPane gameRoot) {
        Scene gameScene = new Scene(gameRoot, WIDTH, HEIGHT);
        gameRoot.setStyle("-fx-background-color: #d3cfc1;");


        return gameScene;
    }

    private static Button getStartButton() {
        Button startButton = new Button("Start Game");
        startButton.setPrefWidth(300);  // Preferred width
        startButton.setPrefHeight(150);  // Preferred height
        startButton.setStyle(
                        "-fx-font-size: 32px;" +
                        "-fx-background-color: #c29b57; " +   // red button
                        "-fx-text-fill: black; " +
                        "-fx-background-radius: 40; " +       // rounded edges
                        "-fx-border-radius: 40;"
        );
        return startButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}