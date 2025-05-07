import java.io.FileNotFoundException;
import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	private static Stage mainStage;
	private static Button loseButton;
    private final static int WIDTH = 1920;
    private final static int HEIGHT = 1080;
    private static final int GRID_SIZE = 10;
    private static final int TILE_SIZE = 45;
    private static final double SPACING = 2.5; // Grid spacing
    private static final Color[] YELLOW_TONES = {
            Color.web("FFCF50"), Color.web("FBC518")
    };
    private static final Color PATH_COLOR = Color.web("FBEBE0");
    final static ArrayList<Animation> transitions = new ArrayList<>();
    private final ArrayList<int[]> placedTowerCells = new ArrayList<>();

    // Grid positioning variables
    private double offsetX;
    private double offsetY;
    private final double gridUnit = TILE_SIZE + SPACING;

    // Game state variables
    static int money = 100;
    static int lives = 5;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<int[]> pathCoordinates;
    private Pane gameOverlay;

    // UI elements
    private static Label livesLabel = new Label("Lives: " + lives);
    private static Label moneyLabel = new Label("Money: $" + money);
    private static Label debugLabel = new Label("Debug: No path loaded");

    public Enemy currentEnemy = null;

    // Tower selection and management
    public int selectedTowerType = 1; // 1: Single, 2: Laser, 3: Triple, 4: Missile
    public Tower selectedTower = null;
    public boolean dragging = false;
    

    @Override
    public void start(Stage primaryStage) throws Exception {
    	mainStage=primaryStage;
        Button startButton = getStartButton();
        Game.root = new Pane();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        StackPane gameRoot = new StackPane();
        Scene gameScene = getGameScene(gameRoot);

        root.setStyle("-fx-background-color: #FFF6DA;");
        root.getChildren().add(startButton);

        primaryStage.setTitle("Tower Defense Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        getloseButton().setOnAction(we ->{
        	
        	primaryStage.setScene(scene);
        	resetGame();
			
        });

        // Set up game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Game.update();
            }
        };
        gameLoop.start();

        startButton.setOnAction(e -> {
            primaryStage.setScene(gameScene);
            transitions.forEach(Animation::play);

            // Add game buttons and start wave scheduling after animations
            double maxDelay = 1188; // Time for rightmost animation to complete in ms

            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(maxDelay), ev -> {
                addGameButtons();
                scheduleWaves(1);
            }));
            delayTimeline.play();
        });
       
        }
    public void resetGame() {
    	
    	Game.root.getChildren().clear();
    	Game.towers.clear();
        Game.enemies.clear();
        Game.bullets.clear();
        Game.missiles.clear();
        //Game.gameOverlay.getChildren().remove(getNode());
        //Game.gameOverlay.getChildren().remove(rangeCircle);
        //towerlear silinmiyo
        money = 100;
        lives = 5;
        if (Main.transitions != null) {
            Main.transitions.forEach(Animation::stop);
        }
        if (moneyLabel != null) moneyLabel.setText("Money: " + money);
        if (livesLabel != null) livesLabel.setText("Lives: " + lives);
        scheduleWaves(1);
    	
    }
    

    /**
     * Add game control buttons to the overlay
     */
    private void addGameButtons() {
        // Spawn Enemy button
        Button spawnEnemyButton = createGameButton("Spawn Enemy", 1520, 500);
        spawnEnemyButton.setOnAction(event -> spawnEnemy());
        gameOverlay.getChildren().add(spawnEnemyButton);

        // Debug Path button
        Button debugButton = createGameButton("Debug Path", 1520, 550);
        debugButton.setOnAction(we -> {
            try {
                visualizePathPoints();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        gameOverlay.getChildren().add(debugButton);

        // Damage Enemy button
        Button damageButton = createGameButton("Damage Enemy", 120, 550);
        damageButton.setOnAction(we -> damageEnemy());
        gameOverlay.getChildren().add(damageButton);
    }

    /**
     * Schedule enemy waves to spawn at specific intervals
     */
    private void scheduleWaves(int level) {
        double[][] waveData = tools.getWaveData(level);

        Timeline master = new Timeline();
        double delay = 2.0;  // initial delay before Wave 1

        for (int i = 0; i < waveData.length; i++) {
            int    count = (int) waveData[i][0];
            double rate  = waveData[i][1];

            // schedule this wave at absolute time "delay"
            master.getKeyFrames().add(
                    new KeyFrame(
                            Duration.seconds(delay),
                            e -> spawnWave(count, rate)
                    )
            );

            // compute when this wave's last enemy spawns
            double end = delay + (count - 1) * rate;

            // use the **next** wave's buffer value (or 0 if last wave)
            double buffer = (i + 1 < waveData.length)
                    ? waveData[i + 1][2]
                    : 0;

            delay = end + buffer;
            System.out.printf("Next wave scheduled at t=%.3fs%n", delay);
        }

        master.play();
    }

    /**
     * Spawn a wave of enemies with given parameters
     *
     * @param enemyCount Number of enemies to spawn
     * @param intervalSeconds Time between spawning each enemy
     */
    private void spawnWave(int enemyCount, double intervalSeconds) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < enemyCount; i++) {
            int finalI = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(finalI * intervalSeconds), e -> spawnEnemy()));
        }
        timeline.play();
    }

    /**
     * Helper method to create consistent game buttons
     */
    private Button createGameButton(String text, double x, double y) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-background-color: #FBD18B;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: black;"
        );
        button.setLayoutX(x);
        button.setLayoutY(y);
        return button;
    }

    /**
     * Create the main game scene
     */
    private Scene getGameScene(StackPane gameRoot) throws FileNotFoundException {
        // Load path coordinates - use appropriate path based on your file structure
        // Use a relative path or allow path to be configurable
        pathCoordinates = tools.readCoordinates("C:\\Users\\erenv\\OneDrive\\Desktop\\TermProject\\levels\\level1.txt");

        // Update debug label with path info
        updatePathDebugInfo();

        Scene gameScene = new Scene(gameRoot, WIDTH, HEIGHT);
        gameRoot.setStyle("-fx-background-color: #FFF6DA;");

        GridPane grid = createGameGrid();

        // Create HUD panel
        VBox hud = createHudPanel();

        // Create overlay pane for enemies and UI elements
        gameOverlay = new Pane();
        hud.setLayoutX(1520);
        hud.setLayoutY(280);

        gameOverlay.getChildren().add(hud);
        gameRoot.getChildren().add(grid);
        gameRoot.getChildren().add(gameOverlay);
        Game.gameOverlay = gameOverlay;

        // Calculate grid dimensions for proper tower placement
        double gridWidth = gridUnit * GRID_SIZE - SPACING;
        double gridHeight = gridUnit * GRID_SIZE - SPACING;

        offsetX = (WIDTH - gridWidth) / 2;
        offsetY = (HEIGHT - gridHeight) / 2;

        // Setup tower placement on click
        setupTowerPlacement();

        return gameScene;
    }
    private static void goEndScene() {
    	
    	StackPane endRoot=new StackPane();
    	Scene endScene=new Scene(endRoot,WIDTH,HEIGHT);
    	Label endLabel=new Label("GAME OVER! ");
    	endLabel.setStyle("-fx-font-size: 24px;");
    	VBox bL=new VBox(20);//ButtonAndLabel
    	 bL.setStyle("-fx-background-color: #FFF6DA; -fx-padding: 3px;");
         bL.setPrefWidth(240);
         bL.setAlignment(Pos.CENTER);
         bL.getChildren().addAll(endLabel,getloseButton());
        
    	
    	endRoot.getChildren().addAll(bL);
    	endRoot.setStyle("-fx-background-color: #FFF6DA;");;
    	
        mainStage.setScene(endScene);
    	
    }

    /**
     * Update debug label with path information
     */
    private void updatePathDebugInfo() {
        StringBuilder pathInfo = new StringBuilder("Path loaded with " + pathCoordinates.size() + " points: ");
        for (int i = 0; i < Math.min(5, pathCoordinates.size()); i++) {
            pathInfo.append("[").append(pathCoordinates.get(i)[0]).append(",").append(pathCoordinates.get(i)[1]).append("] ");
        }
        if (pathCoordinates.size() > 5) {
            pathInfo.append("...");
        }
        debugLabel.setText(pathInfo.toString());
    }

    /**
     * Create the game grid
     */
    private GridPane createGameGrid() {
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

        for (int[] coord : pathCoordinates) {
            if (coord[0] >= 0 && coord[0] < GRID_SIZE && coord[1] >= 0 && coord[1] < GRID_SIZE) {
                isPath[coord[0]][coord[1]] = true;
            } else {
                System.err.println("Warning: Invalid coordinate in path data: [" + coord[0] + "," + coord[1] + "]");
            }
        }

        // Create and animate grid tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
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

        return grid;
    }

    /**
     * Create the HUD panel with game stats and tower options
     */
    private VBox createHudPanel() {
        VBox hud = new VBox(10);
        hud.setStyle("-fx-background-color: #FFF6DA; -fx-padding: 3px;");
        hud.setPrefWidth(240);
        hud.setAlignment(Pos.CENTER);

        // Tower selection buttons
        Image singleShotImage = new Image("/assets/towers/singleshottower.png");
        ImageView image=new ImageView(singleShotImage);
        image.setFitWidth(30);
		image.setFitHeight(30);
        VBox vbox1 = new VBox();
        vbox1.setAlignment(Pos.CENTER);
        Label label1 = new Label("Single Shot Tower");
        Label label2 = new Label("50$");
        vbox1.getChildren().addAll(image,label1, label2);
        Button singleShot = new Button();
        singleShot.setGraphic(vbox1); 
        
        
        Image laserImage = new Image("/assets/towers/lasertower.png");
        ImageView image2=new ImageView(laserImage);
        image2.setFitWidth(30);
		image2.setFitHeight(30);
        VBox vbox2= new VBox(5);
        vbox2.setAlignment(Pos.CENTER);
        Label label3 = new Label("Laser Tower");
        Label label4 = new Label("120$");
        vbox2.getChildren().addAll(image2,label3, label4);
        Button laser= new Button();
        laser.setGraphic(vbox2);
        
        
        Image TripleShotImage = new Image("/assets/towers/tripleshottower.png");
        ImageView image3=new ImageView(TripleShotImage);
        
        image3.setFitWidth(30);
		image3.setFitHeight(30);
        VBox vbox3 = new VBox();
        vbox3.setAlignment(Pos.CENTER);
        Label label5 = new Label("Triple Shot Tower");
        Label label6 = new Label("150$");
        vbox3.getChildren().addAll(image3,label5, label6);
        Button tripleShot = new Button();
        tripleShot.setGraphic(vbox3);

        Image MissileTowerImage = new Image("/assets/towers/missilelaunchtower.png");
        ImageView image4=new ImageView(MissileTowerImage);
        VBox vbox4= new VBox();
        image4.setFitWidth(30);
		image4.setFitHeight(30);
        vbox4.setAlignment(Pos.CENTER);
        Label label7 = new Label("Missile Launcher Tower");
        Label label8 = new Label("200$");
        vbox4.getChildren().addAll(image4,label7, label8);
        Button missile= new Button();
        missile.setGraphic(vbox4);
        
        

        singleShot.setOnAction(e -> selectedTowerType = 1);
        laser.setOnAction(e -> selectedTowerType = 2);
        tripleShot.setOnAction(e -> selectedTowerType = 3);
        missile.setOnAction(e -> selectedTowerType = 4);

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

        // Style labels
        livesLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        moneyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        debugLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        hud.getChildren().addAll(livesLabel, moneyLabel, debugLabel, singleShot, laser, tripleShot, missile);
        return hud;
    }

    /**
     * Setup tower placement and dragging functionality
     */
    private void setupTowerPlacement() {
        gameOverlay.setOnMouseClicked(e -> {
            double clickX = e.getX();
            double clickY = e.getY();

            // Prevent tower placement in HUD area
            if (clickX >= 1520)
                return;

            // Prevent tower placement outside grid
            if (clickX < offsetX || clickY < offsetY)
                return;

            // Convert click coordinates to grid position
            int col = (int)((clickX - offsetX) / gridUnit);
            int row = (int)((clickY - offsetY) / gridUnit);

            // Check if grid position is valid
            if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
                return;
            }

            // Check if path tile
            for (int[] coord : pathCoordinates) {
                if (coord[0] == row && coord[1] == col) {
                    return;
                }
            }

            // Check if tower already placed
            for (int[] placed : placedTowerCells) {
                if (placed[0] == row && placed[1] == col) {
                    return;
                }
            }

            // Calculate center of grid cell for tower placement
            double cellCenterX = offsetX + col * gridUnit + TILE_SIZE / 2;
            double cellCenterY = offsetY + row * gridUnit + TILE_SIZE / 2;

            // Create tower based on selected type
            Tower tower = switch (selectedTowerType) {
                case 1 -> new SingleShotTower(cellCenterX, cellCenterY);
                case 2 -> new LaserTower(cellCenterX, cellCenterY);
                case 3 -> new TripleShotTower(cellCenterX, cellCenterY);
                case 4 -> new MissileLauncherTower(cellCenterX, cellCenterY);
                default -> null;
            };

            if (tower != null) {
                // Check if player has enough money
                if (money < tower.getPrice()) {
                    return;
                }

                // Deduct tower cost
                decreaseMoney(tower.getPrice());

                // Add tower to game
                gameOverlay.getChildren().add(tower.getRangeCircle());
                gameOverlay.getChildren().add(tower.getNode());
                Game.addTower(tower);

                // Set tower grid position
                tower.setGridPosition(row, col);
                placedTowerCells.add(new int[]{row, col});

                // Handle tower selection and right-click to sell
                tower.getNode().setOnMousePressed(ev -> {
                    if (ev.isSecondaryButtonDown()) {
                        // Sell tower on right-click
                        increaseMoney(tower.getPrice());

                        // Remove tower from display
                        gameOverlay.getChildren().removeAll(tower.getNode(), tower.getRangeCircle());

                        // Remove tower from grid tracking
                        int[] gridPos = tower.getGridPosition();
                        placedTowerCells.removeIf(p -> p[0] == gridPos[0] && p[1] == gridPos[1]);

                        // Remove tower from game logic
                        Game.removeTower(tower);

                        return;
                    }

                    // Select tower and show range
                    selectedTower = tower;
                    dragging = true;
                    tower.getRangeCircle().setVisible(true);
                });

                // Handle tower dragging
                tower.getNode().setOnMouseDragged(ev -> {
                    if (dragging) {
                        tower.setPosition(ev.getX(), ev.getY());
                    }
                });

                // Handle tower placement after drag
                tower.getNode().setOnMouseReleased(ev -> {
                    dragging = false;
                    selectedTower = null;
                    tower.getRangeCircle().setVisible(false);

                    double mouseX = ev.getX();
                    double mouseY = ev.getY();

                    int col1 = (int)((mouseX - offsetX) / gridUnit);
                    int row1 = (int)((mouseY - offsetY) / gridUnit);

                    // Check if new position is on path
                    for (int[] coord : pathCoordinates) {
                        if (coord[0] == row1 && coord[1] == col1) {
                            return;
                        }
                    }

                    // Remove old position from tracking
                    placedTowerCells.removeIf(p -> p[0] == tower.getGridPosition()[0] && p[1] == tower.getGridPosition()[1]);

                    // Apply new position
                    double centerX = offsetX + col1 * gridUnit + TILE_SIZE / 2;
                    double centerY = offsetY + row1 * gridUnit + TILE_SIZE / 2;

                    tower.setPosition(centerX, centerY);
                    tower.setGridPosition(row1, col1);
                    placedTowerCells.add(new int[]{row1, col1});
                });
            }
        });
    }

    /**
     * Visualize path points for debugging
     */
    private void visualizePathPoints() throws Exception {
        if (pathCoordinates == null || pathCoordinates.isEmpty()) {
            throw new Exception("Got empty coordinates!");
        }

        // Calculate grid dimensions
        double gridWidth = (TILE_SIZE + SPACING) * GRID_SIZE - SPACING;
        double gridHeight = (TILE_SIZE + SPACING) * GRID_SIZE - SPACING;

        // Create a circle at each path point
        for (int i = 0; i < pathCoordinates.size(); i++) {
            int[] point = pathCoordinates.get(i);
            double x = offsetX + point[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
            double y = offsetY + point[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;

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
        currentEnemy = new Enemy(30, gameOverlay); // 100:health
        enemies.add(currentEnemy);
        Game.enemies.add(currentEnemy);

        // Start enemy movement along the path
        currentEnemy.moveAlongPath(pathCoordinates);
    }

    /**
     * Damage the current enemy for testing
     */
    private void damageEnemy() {
        if (currentEnemy != null && currentEnemy.isAlive()) {
            currentEnemy.damage(10);
        }
    }

    /**
     * Static method to decrease player lives
     */
    public static void decreaseLives() {
        lives--;
        livesLabel.setText("Lives: " + lives);

        // Game over condition
        if (lives <= 0) {
        	goEndScene();
        }
    }
   

    /**
     * Static method to increase player money
     */
    public static void increaseMoney(double amount) {
        money += amount;
        moneyLabel.setText("Money: $" + money);
    }

    /**
     * Static method to decrease player money
     */
    public static void decreaseMoney(double amount) {
        money -= amount;
        moneyLabel.setText("Money: $" + money);
    }

    /**
     * Create a styled start button
     */
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
    private static Button getloseButton() {
    	if(loseButton==null) {
        loseButton = new Button("Back to Main Menu");
    	}
        loseButton.setPrefWidth(400);
        loseButton.setPrefHeight(150);
        loseButton.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-background-color: #c29b57;" +
                        "-fx-text-fill: black;" +
                        "-fx-background-radius: 40;" +
                        "-fx-border-radius: 40;"
        );
        return loseButton;
    }
}