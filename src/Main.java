import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;

public class Main extends Application {
    private final int WIDTH = 1080;
    private final int HEIGHT = 720;
    private static final int GRID_SIZE = 10;
    private static final int TILE_SIZE = 40;
    private static final Color[] YELLOW_TONES = {
        Color.web("#FFD700"), Color.web("#FFEA70")
    };
    private static final Color PATH_COLOR = Color.LIGHTGRAY;

    @Override
    public void start(Stage primaryStage) {
        Button startButton = getStartButton();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        StackPane gameRoot = new StackPane();
        Scene gameScene = getGameScene(gameRoot);

        root.setStyle("-fx-background-color: #d3cfc1;");
        root.getChildren().add(startButton);

        primaryStage.setTitle("Tower Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        startButton.setOnAction(e -> {
            primaryStage.setScene(gameScene);
        });
    }

    private Scene getGameScene(StackPane gameRoot) {
        Scene gameScene = new Scene(gameRoot, WIDTH, HEIGHT);
        gameRoot.setStyle("-fx-background-color: #d3cfc1;");

        GridPane grid = new GridPane();
        grid.setHgap(3);
        grid.setVgap(3);
        grid.setAlignment(Pos.CENTER);

        // Gri yolun konumu (örnek olarak orta satır)
        boolean[][] isPath = new boolean[GRID_SIZE][GRID_SIZE];
        for (int col = 0; col < 4; col++) {
            isPath[2][col] = true;
        }
        for (int row = 3; row < 6; row++) {
            isPath[row][3] = true;
        }
        for (int col = 4; col < GRID_SIZE; col++) {
            isPath[5][col] = true;
        }

        // Kareleri oluştur ve animasyon ekle
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                if (isPath[row][col]) {
                    tile.setFill(PATH_COLOR);
                } else {
                    tile.setFill(YELLOW_TONES[(int)(Math.random() * YELLOW_TONES.length)]);
                }
                tile.setOpacity(0); //  Start with all rectangles not visible.For slowly show the rectangles animation 
                grid.add(tile, col, row);

                // Fade-in animasyonu
                FadeTransition ft = new FadeTransition(Duration.millis(500), tile);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.setDelay(Duration.millis((row * GRID_SIZE + col) * 50));
                ft.play();
            }
        }

        gameRoot.getChildren().add(grid);
        return gameScene;
    }

    private static Button getStartButton() {
        Button startButton = new Button("Start Game");
        startButton.setPrefWidth(300);
        startButton.setPrefHeight(150);
        startButton.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-background-color: #c29b57;" +
            "-fx-text-fill: black;" +
            "-fx-background-radius: 40;" +
            "-fx-border-radius: 40;"
        );
        return startButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
