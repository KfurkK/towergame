import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;

/**
 * Main game application class
 */
public class Main extends Application {
    private final int WIDTH = 1920;
    private final int HEIGHT = 1080;
    private static final int GRID_SIZE = 10;
    private static final int SQUARE_SIZE = 45;
    private static final double SPACING = 2.5; // Grid spacing
    private static final Color[] YELLOW_TONES = {
            Color.web("FFCF50"), Color.web("FBC518")
    };
    private static final Color PATH_COLOR = Color.web("FBEBE0");
    private final ArrayList<Animation> transitions = new ArrayList<>();

    // Game state variables
    private static int money = 100;
    private static int lives = 5;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<int[]> pathCoordinates;
    private Pane gameOverlay;

    // UI elements
    private static Label livesLabel = new Label("Lives: " + lives);
    private static Label moneyLabel = new Label("Money: $" + money);
    private static Label debugLabel = new Label("Debug: No path loaded");

    @Override
    public void start(Stage primaryStage) throws Exception {
        Button startButton = getStartButton();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        StackPane gameRoot = new StackPane();
        Scene gameScene = getGameScene(gameRoot);

        root.setStyle("-fx-background-color: #FFF6DA;");
        root.getChildren().add(startButton);

        primaryStage.setTitle("Tower Defense Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        startButton.setOnAction(e -> {
            primaryStage.setScene(gameScene);
            transitions.forEach(Animation::play);
            
            double maxDelay = (1188); // En sağ alt karenin animasyonu bitirmesi için süre ms

            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(maxDelay), ev -> scheduleWaves()));
            delayTimeline.play();

            // Add a button to spawn an enemy after the game starts
          /*  Button spawnEnemyButton = new Button("Spawn Enemy");
            spawnEnemyButton.setPrefWidth(150);
            spawnEnemyButton.setPrefHeight(40);
            spawnEnemyButton.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-background-color: #FBD18B;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: black;"
            );
            spawnEnemyButton.setLayoutX(1520);
            spawnEnemyButton.setLayoutY(500);
            spawnEnemyButton.setOnAction(we -> spawnEnemy());
            gameOverlay.getChildren().add(spawnEnemyButton);*/

            // Add a debug button to visualize path points
            Button debugButton = new Button("Debug Path");
            debugButton.setPrefWidth(150);
            debugButton.setPrefHeight(40);
            debugButton.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-background-color: #FBD18B;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: black;"
            );
            debugButton.setLayoutX(1520);
            debugButton.setLayoutY(550);
            debugButton.setOnAction(ce -> visualizePathPoints());
            gameOverlay.getChildren().add(debugButton);
        });
    }

    private Scene getGameScene(StackPane gameRoot) throws FileNotFoundException {
        // Load path coordinates
        pathCoordinates = tools.readCoordinates("C:\\Users\\erenv\\OneDrive\\Desktop\\TermProject\\levels\\level1.txt");

        // Update debug label with path info
        StringBuilder pathInfo = new StringBuilder("Path loaded with " + pathCoordinates.size() + " points: ");
        for (int i = 0; i < Math.min(5, pathCoordinates.size()); i++) {
            pathInfo.append("[").append(pathCoordinates.get(i)[0]).append(",").append(pathCoordinates.get(i)[1]).append("] ");
        }
        if (pathCoordinates.size() > 5) {
            pathInfo.append("...");
        }
        debugLabel.setText(pathInfo.toString());

        Scene gameScene = new Scene(gameRoot, WIDTH, HEIGHT);
        gameRoot.setStyle("-fx-background-color: #FFF6DA;");

        GridPane grid = new GridPane();
        grid.setHgap(SPACING);
        grid.setVgap(SPACING);
        grid.setAlignment(Pos.CENTER);

        // Define the path
        boolean[][] isPath = new boolean[GRID_SIZE][GRID_SIZE];

        if (pathCoordinates.isEmpty()) {
            System.err.println("Error: Coordinates could not be loaded.");
            System.exit(1);
        }

        for(int i=0;i<pathCoordinates.size();i++) {
            isPath[pathCoordinates.get(i)[0]][pathCoordinates.get(i)[1]]=true;
            }
            

        // Create and animate grid tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle tile = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                if (isPath[row][col]) {
                    tile.setFill(PATH_COLOR);
                } else {
                    tile.setFill(YELLOW_TONES[(int)(Math.random() * YELLOW_TONES.length)]);
                }
                tile.setOpacity(0);
                tile.setScaleX(0.1);
                tile.setScaleY(0.1);

                grid.add(tile, col, row);

                // Fade animation
                FadeTransition ft = new FadeTransition(Duration.millis(500), tile);
                ft.setFromValue(0);
                ft.setToValue(1);

                // Scale animation
                ScaleTransition st = new ScaleTransition(Duration.millis(500), tile);
                st.setFromX(0.1);
                st.setFromY(0.1);
                st.setToX(1.0);
                st.setToY(1.0);

                // Parallel animation
                ParallelTransition pt = new ParallelTransition(ft, st);
                pt.setDelay(Duration.millis((row * GRID_SIZE + col) * 12));
                transitions.add(pt);
            }
        }

        // Create HUD panel
        VBox hud = new VBox(10);
        hud.setStyle("-fx-background-color: #FFF6DA; -fx-padding: 3px;");
        hud.setPrefWidth(240);
        hud.setAlignment(Pos.CENTER);

        Button singleShot = new Button("Single Shot Tower - 50$");
        Button laser = new Button("Laser Tower - 120$");
        Button tripleShot = new Button("Triple Shot Tower - 150$");
        Button missile = new Button("Missile Launcher Tower - 200$");

        for (Button b : new Button[]{singleShot, laser, tripleShot, missile}) {
            b.setPrefWidth(150);
            b.setPrefHeight(90);
            b.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-background-color: #FBD18B;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: black;"
            );
        }

        livesLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        moneyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        debugLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        hud.getChildren().addAll(livesLabel, moneyLabel, debugLabel, singleShot, laser, tripleShot, missile);

        // Create overlay pane for enemies and UI elements
        gameOverlay = new Pane();
        hud.setLayoutX(1520);
        hud.setLayoutY(280);

        gameOverlay.getChildren().add(hud);
        gameRoot.getChildren().add(grid);
        gameRoot.getChildren().add(gameOverlay);

        return gameScene;
    }
    private void scheduleWaves() {
        Timeline startWave1 = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            spawnWave(5, 1.0);
        }));

        Timeline startWave2 = new Timeline();
        	
        	startWave2.getKeyFrames().add(new KeyFrame(Duration.seconds(2+4*1.0+5),e -> {
        		spawnWave(8,0.5);
        	}));
        Timeline startWave3= new Timeline();
            startWave3.getKeyFrames().add(new KeyFrame(Duration.seconds((2+4*1.0+5)+7*0.5+5),e -> {
            	spawnWave(12,0.3);
            }));
      
        	
       
            

        startWave1.play();
        startWave2.play();
        startWave3.play();
    }

    private void spawnWave(int enemyCount, double intervalSeconds) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < enemyCount; i++) {
            int finalI = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(finalI * intervalSeconds), e -> spawnEnemy()));
        }
        timeline.play();
    }

    /**
     * Visualize path points for debugging
     */
    private void visualizePathPoints() {
        if (pathCoordinates == null || pathCoordinates.isEmpty()) {
            return;
        }

        // Get center position of the grid in the scene
        double gridCenterX = gameOverlay.getScene().getWidth() / 2;
        double gridCenterY = gameOverlay.getScene().getHeight() / 2;

        // Calculate grid offset (to center it)
        double gridWidth = (SQUARE_SIZE + SPACING) * GRID_SIZE - SPACING;
        double gridHeight = (SQUARE_SIZE + SPACING) * GRID_SIZE - SPACING;
        double offsetX = gridCenterX - gridWidth / 2;
        double offsetY = gridCenterY - gridHeight / 2;

        // Create a circle at each path point
        for (int i = 0; i < pathCoordinates.size(); i++) {
            int[] point = pathCoordinates.get(i);
            double x = offsetX + point[1] * (SQUARE_SIZE + SPACING) + SQUARE_SIZE / 2;
            double y = offsetY + point[0] * (SQUARE_SIZE + SPACING) + SQUARE_SIZE / 2;

            Circle marker = new Circle(5);
            marker.setFill(i == 0 ? Color.GREEN : (i == pathCoordinates.size() - 1 ? Color.RED : Color.BLUE));
            marker.setTranslateX(x);
            marker.setTranslateY(y);

            // Add label with index
            Label indexLabel = new Label(String.valueOf(i));
            indexLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: black;");
            indexLabel.setTranslateX(x - 5);
            indexLabel.setTranslateY(y - 15);

            gameOverlay.getChildren().addAll(marker, indexLabel);

            // Update debug info
            debugLabel.setText("Point " + i + ": [" + point[0] + "," + point[1] + "] -> (" + x + "," + y + ")");
        }
    }

    /**
     * Spawn a single enemy on the path
     */
    private void spawnEnemy() {
        Enemy enemy = new Enemy(100, gameOverlay);
        enemies.add(enemy);

        // Test damage function - will damage the enemy after 3 seconds
       /* Timeline damageTimer = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                	for(int i=0;i<enemies.size();i++) {
                		if(enemies.get(i).isAlive())
                enemy.damage(10);
                	}
                }
                ));
        damageTimer.play();
        */

        // Start enemy movement along the path
        enemy.moveAlongPath(pathCoordinates);
    }

    /**
     * Static method to decrease player lives
     */
    public static void decreaseLives(int amount) {
        lives -= amount;
        livesLabel.setText("Lives: " + lives);

        // Game over condition
        if (lives <= 0) {
            System.out.println("Game Over!");
        }
    }

    /**
     * Static method to increase player money
     */
    public static void increaseMoney(int amount) {
        money += amount;
        moneyLabel.setText("Money: $" + money);
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

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }
}