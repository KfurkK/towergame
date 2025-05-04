import java.io.FileNotFoundException;
import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
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
	private double offsetX;
	private double offsetY;
	private final double gridUnit = TILE_SIZE + SPACING;
	private final int WIDTH = 1920;
    private final int HEIGHT = 1080;
    private static final int GRID_SIZE = 10;
    private static final int TILE_SIZE = 45;
    private static final double SPACING = 2.5; // Grid spacing
    private static final Color[] YELLOW_TONES = {
            Color.web("FFCF50"), Color.web("FBC518")
    };
    private final ArrayList<int[]> placedTowerCells = new ArrayList<>();
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

    public Enemy currentEnemy = null;
    

    // Tower selection and management
    public int selectedTowerType = 1; // 1: Single, 2: Laser, 3: Triple, 4: Missile
    public Tower selectedTower = null;
    public boolean dragging = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
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
            addGameButtons();
        });
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
        // Load path coordinates
        pathCoordinates = tools.readCoordinates("C:\\Users\\asara\\OneDrive\\Masaüstü\\TermProject\\levels\\level2.txt");
        
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

        // Setup tower placement on click
        setupTowerPlacement();
        
        double gridWidth = gridUnit * GRID_SIZE - SPACING;
        double gridHeight = gridUnit * GRID_SIZE - SPACING;

        offsetX = (WIDTH - gridWidth) / 2;
        offsetY = (HEIGHT - gridHeight) / 2;

        return gameScene;
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
        Button singleShot = new Button("Single Shot Tower - 50$");
        Button laser = new Button("Laser Tower - 120$");
        Button tripleShot = new Button("Triple Shot Tower - 150$");
        Button missile = new Button("Missile Launcher Tower - 200$");

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
        	    
        	if (clickX >= 1520) 
        		return;
    
        	if (clickX < offsetX || clickY < offsetY) 
            	return;
            
            
            
            int col = (int)((clickX - offsetX) / gridUnit);
            int row = (int)((clickY - offsetY) / gridUnit);
            
            if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
                System.out.println("❌ Harita dışına kule koyamazsın!");
                return;
            }

            for (int[] coord : pathCoordinates) {
                if (coord[0] == row && coord[1] == col) {
                   return;
                }
            }
            
            for (int[] placed : placedTowerCells) {
                if (placed[0] == row && placed[1] == col) {
                    return;
                    
                }
            }
            double cellCenterX = offsetX + col * gridUnit + TILE_SIZE / 2;
            double cellCenterY = offsetY + row * gridUnit + TILE_SIZE / 2;
            
            
        	Tower tower = switch (selectedTowerType) {
                case 1 -> new SingleShotTower(cellCenterX, cellCenterY);
                case 2 -> new LaserTower(cellCenterX, cellCenterY);
                case 3 -> new TripleShotTower(cellCenterX, cellCenterY);
                case 4 -> new MissileLauncherTower(cellCenterX, cellCenterY);
                default -> null;
            };

            if (tower != null) {
            	if (money < tower.getPrice()) {
                   return;
                }
            	
            	gameOverlay.getChildren().add(tower.getRangeCircle());
                gameOverlay.getChildren().add(tower.getNode());
                Game.addTower(tower);
                
                tower.setGridPosition(row, col);
                placedTowerCells.add(new int[]{row, col});

                tower.getNode().setOnMousePressed(ev -> {
                    selectedTower = tower;
                    dragging = true;
                    tower.getRangeCircle().setVisible(true);
                });

                tower.getNode().setOnMouseDragged(ev -> {
                    if (dragging) {
                    	tower.setPosition(ev.getX(), ev.getY());
                    }
                });

                tower.getNode().setOnMouseReleased(ev -> {
                	dragging = false;
                    selectedTower = null;
                    tower.getRangeCircle().setVisible(false); 
                    double mouseX = ev.getX();
                    double mouseY = ev.getY();

                    int col1 = (int)((mouseX - offsetX) / gridUnit);
                    int row1 = (int)((mouseY - offsetY) / gridUnit);

                    
                    for (int[] coord : pathCoordinates) {
                        if (coord[0] == row1 && coord[1] == col1) {
                            System.out.println("❌ Yola kule bırakılamaz!");
                            return;
                        }
                    }

                    // 🔁 Eski pozisyonu sil
                    placedTowerCells.removeIf(p -> p[0] == tower.getGridPosition()[0] && p[1] == tower.getGridPosition()[1]);

                    // ✅ Yeni pozisyonu uygula
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

        // Get center position of the grid in the scene
        double gridCenterX = gameOverlay.getScene().getWidth() / 2;
        double gridCenterY = gameOverlay.getScene().getHeight() / 2;

        // Calculate grid offset (to center it)
        double gridWidth = (TILE_SIZE + SPACING) * GRID_SIZE - SPACING;
        double gridHeight = (TILE_SIZE + SPACING) * GRID_SIZE - SPACING;
        double offsetX = gridCenterX - gridWidth / 2;
        double offsetY = gridCenterY - gridHeight / 2;

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
        currentEnemy = new Enemy(100, gameOverlay);
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
}