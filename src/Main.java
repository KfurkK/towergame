//restart yapınca animasyon bozuk
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.GaussianBlur;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import javax.sound.sampled.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;
import java.util.Random;



/**
 * Main game application class
 */
public class Main extends Application {
	public double maxDelay=1188;
    private ImagePattern roadPattern;
    private ImagePattern alternateRoadPattern;
    private Pane initialGameOverlay;
    private Timeline countdownTimer;
    private Timeline waveTimeLine;
    private Label waveCountdownLabel;
    private static Stage mainStage;
    private static Button continueButton;
    private static Button loseButton;
    private Button wonButton;
    private static AnimationTimer gameLoop;
    public boolean draggingTower = false;
    private static boolean gameOverTriggered = false;
    private MediaPlayer mediaPlayer;
    private final static int WIDTH = 1920;
    private final static int HEIGHT = 1080;
    private int gridSize=10;
    private static final int TILE_SIZE = 45;
    private static final double SPACING = 2.5; // Grid spacing
    private static final Color[] YELLOW_TONES = {
            Color.web("FFCF50"), Color.web("FBC518")
    };
    private static final Color PATH_COLOR = Color.web("FBEBE0");
    private final static ArrayList<Animation> transitions = new ArrayList<>();
    private final ArrayList<int[]> placedTowerCells = new ArrayList<>();

    // Grid positioning variables
    private double offsetX;
    private double offsetY;
    private final double gridUnit = TILE_SIZE + SPACING;
    
    private StackPane gameRoot;
    private Scene gameScene;

    // Game state variables
    private static int money = 100;
    private static int lives = 5;
    private static int score = 0;
    
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<int[]> pathCoordinates;
    private Pane gameOverlay;

    // UI elements
    private static Label livesLabel = new Label("Lives: " + lives);
    private static Label moneyLabel = new Label("Money: $" + money);
    private static Label debugLabel = new Label("Debug: No path loaded");
    private static Label scoreLabel = new Label("Score: 0");
    

    public Enemy currentEnemy = null;

    // Tower selection and management
    public int selectedTowerType = 1; // 1: Single, 2: Laser, 3: Triple, 4: Missile
    public Tower selectedTower = null;
    public boolean dragging = false;
    private int currentLevel=1;
    private int finishedWaveCount=0;

    @Override
    public void start(Stage primaryStage) throws Exception {


        Image alternateRoadImg = new Image(getClass().getResourceAsStream("/assets/main_road.png"));
        alternateRoadPattern = new ImagePattern(alternateRoadImg);

        Image bgImage = new Image(getClass().getResource("/assets/background.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);


        AudioPlayer bgMusic = new AudioPlayer();
        bgMusic.playLoop("src/assets/sounds/Muzik1.wav");

        mainStage=primaryStage;
        Button startButton = getStartButton();
        Game.root = new Pane();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        gameRoot = new StackPane();
        gameScene = getGameScene(gameRoot);
        initialGameOverlay = gameOverlay;
        
        Button exitButton = getExitButton();
        
        
        VBox gameNameBox = new VBox(50);
        gameNameBox.setAlignment(Pos.TOP_CENTER);
        gameNameBox.setTranslateY(300);
        
        Label gameName = new Label("Tower Defence Game");
        gameName.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 70));
        gameName.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 4, 0.3, 0, 2);");
        gameNameBox.getChildren().addAll(gameName);
        
        
        VBox topScoreBox = new VBox(5);
        topScoreBox.setStyle("-fx-background-color: rgba(0,0,0,0.0); -fx-background-radius: 10;");
        topScoreBox.setAlignment(Pos.TOP_LEFT);
        topScoreBox.setMaxWidth(300);
        topScoreBox.setTranslateX(700); // sağa
        topScoreBox.setTranslateY(50);  // yukarı
        
        Label title = new Label("Top Scores:");
        title.setStyle("-fx-text-fill: #FFE09A; -fx-font-size: 18px;");
        title.setTranslateX(0);
        topScoreBox.getChildren().add(title);

        List<ScoreManager.ScoreEntry> entries = ScoreManager.loadEntries();
        for (int i = 0; i < entries.size(); i++) {
            ScoreManager.ScoreEntry entry = entries.get(i);
            Label scoreLabel = new Label((i + 1) + ". " + entry.username + " " + entry.score + " pts (" + entry.timestamp + ")");
            scoreLabel.setStyle("-fx-text-fill: #FFE09A; -fx-font-size: 12px; -fx-alignment: CENTER_LEFT;");
            topScoreBox.getChildren().add(scoreLabel);
        }

        root.getChildren().addAll(bgView, gameNameBox, topScoreBox, startButton, exitButton);

        //root.setStyle("-fx-background-color: #FFF6DA;");
        //root.getChildren().add(startButton);

        primaryStage.setTitle("Tower Defense Game");
        primaryStage.setScene(scene);
        primaryStage.show();


        // Set up game loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Game.update();

                if (lives <= 0) {
                    this.stop(); // timer durdurulmalı
                    goEndScene(); // sahneye geçilmeli
                    return;
                }
                if (tools.getWaveData(currentLevel).length == finishedWaveCount
                        && Game.enemies.isEmpty()
                        && lives > 0) {
                    System.out.println("🎉 Tüm wave'ler tamamlandı! Level: " + currentLevel);
                    if (currentLevel == 5) {
                        this.stop();// 🎉 5. level bitti, oyun kazanıldı
                        goWonScene();
                    } else {
                        // ⏭ Diğer levellere geçiş
                        waveCountdownLabel.setText("Next wave: 0s");
                        System.out.println("✔ Level tamamlandı. Yeni levele geçiliyor...");

                        finishedWaveCount = 0;
                        currentLevel++;
                        System.out.println("BURDA");
                        OtherResetGame();
                        System.out.println("BURDA2");
                        goContinueScene();
                        System.out.println("BURDA3");
                        System.out.println(currentLevel);
                    }




                }
            }

        };
        gameLoop.start();

        startButton.setOnAction(e -> {
            resetGame();
            
            gameRoot = new StackPane();

            try {
                gameScene = getGameScene(gameRoot); // yeniden yarat!
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                return;
            }
            primaryStage.setScene(gameScene); // yeni sahneyi göster
            transitions.forEach(Animation::play);

            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(maxDelay), ev -> {
                addGameButtons();
                setupTowerPlacement();
                scheduleWaves(currentLevel);
            }));
            
            
            delayTimeline.play();
        });
        
        exitButton.setOnAction(e -> {
        	Platform.exit();
            });

        getloseButton().setOnAction(we ->{
            currentLevel=1;
            finishedWaveCount=0;
            resetGame();



            if (waveTimeLine != null)
                waveTimeLine.stop();
            if (countdownTimer != null)
                countdownTimer.stop();
            
            primaryStage.setScene(scene);

            

        });



    }

    /**
     * Add game control buttons to the overlay
     */
    private void addGameButtons() {
        // Spawn Enemy button
        /*Button spawnEnemyButton = createGameButton("Spawn Enemy", 520, 500);
        spawnEnemyButton.setOnAction(event -> spawnEnemyArcher());
        gameOverlay.getChildren().add(spawnEnemyButton); */

        // Debug Path button
        /*Button debugButton = createGameButton("Debug Path", 520, 550);
        debugButton.setOnAction(we -> {
            try {
                visualizePathPoints();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        gameOverlay.getChildren().add(debugButton);

        // Damage Enemy button */

    }

    /**
     * Schedule enemy waves to spawn at specific intervals
     */
    private void scheduleWaves(int level) {
        if (waveTimeLine != null) {
            waveTimeLine.stop();
        }

        double[][] waveData = tools.getWaveData(level);
        waveTimeLine = new Timeline();

        // İlk wave'in buffer'ına göre ilk countdown
        if (waveData.length > 0) {
            int firstBuffer = (int) waveData[0][2];
            startWaveCountdown(firstBuffer);
        }

        double delay = waveData[0][2];  // ilk wave başlamadan önceki bekleme

        for (int i = 0; i < waveData.length; i++) {
            int count = (int) waveData[i][0];
            double rate = waveData[i][1];

            // Yeni wave başlamadan 5 saniye önce geri sayımı başlat
            if (delay >= 5) {
                int seconds = 5;
                waveTimeLine.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(delay - seconds),
                                e -> startWaveCountdown(seconds)
                        )
                );
            }

            // Asıl wave başlatma
            waveTimeLine.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(delay),
                            e -> spawnWave(count, rate)
                    )
            );

            double end = delay + (count - 1) * rate;
            double buffer = (i + 1 < waveData.length) ? waveData[i + 1][2] : 0;
            delay = end + buffer;
        }

        waveTimeLine.play();
        System.out.printf("Next wave scheduled at t=%.3fs%n", delay);
    }

    private void startWaveCountdown(int seconds) {
        // Mevcut timer varsa durdur
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        countdownTimer = new Timeline();
        for (int i = 0; i <= seconds; i++) {
            int remaining = seconds - i;
            countdownTimer.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(i),
                            e -> waveCountdownLabel.setText("Next wave: " + remaining + "s")
                    )
            );
        }
        countdownTimer.play();
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
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(finalI * intervalSeconds), e -> {
                
                Random random = new Random();
                double randomValue = random.nextDouble(); // or Math.random()

                // Check if the random value is less than 0.3 (30% chance)
                if (randomValue < 0.3) {
                    spawnEnemyArcher();
                }else {
                	spawnEnemy();
                }
            }));
        }
        timeline.play();

        timeline.setOnFinished(e -> {
            ++finishedWaveCount;
            System.out.println(finishedWaveCount);
        });

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
        pathCoordinates = tools.readCoordinates("src\\levels\\level"+currentLevel+".txt");
        gridSize=tools.getMapSize("src\\levels\\level"+currentLevel+".txt");

        Image bgImage = new Image(getClass()
                .getResource("/assets/background.png")
                .toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(WIDTH);
        bgView.setFitHeight(HEIGHT);
        bgView.setPreserveRatio(false);
        bgView.setEffect(new GaussianBlur(12));  // 8–15 arası radius deneyebilirsiniz
        gameRoot.getChildren().add(0, bgView);

        // Update debug label with path info
        updatePathDebugInfo();

        Scene gameScene = new Scene(gameRoot, WIDTH, HEIGHT);
        //gameRoot.setStyle("-fx-background-color: #FFF6DA;");

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
        double gridWidth = gridUnit * gridSize - SPACING;
        double gridHeight = gridUnit * gridSize - SPACING;

        offsetX = (WIDTH - gridWidth) / 2;
        offsetY = (HEIGHT - gridHeight) / 2;

        // Setup tower placement on click
        setupTowerPlacement();

        return gameScene;
    }

    public void goContinueScene() {
        StackPane nextRoot=new StackPane();
        Scene nextScene=new Scene(nextRoot,WIDTH,HEIGHT);

        Image bgImage = new Image(getClass().getResource("/assets/background.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(WIDTH);
        bgView.setFitHeight(HEIGHT);
        bgView.setPreserveRatio(false);
        bgView.setEffect(new GaussianBlur(12));

        Label nextLabel=new Label("You won!");
        nextLabel.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 48));
        nextLabel.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 4, 0.3, 0, 2);");
        
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        scoreLabel.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0.3, 0, 2);");

        VBox bL=new VBox(20);//ButtonAndLabel
        bL.setStyle("-fx-background-color: rgba(251,209,139,0.0);" + // şeffaf amber tonu
                "-fx-background-radius: 12;" +
                "-fx-padding: 16px;");
        bL.setPrefWidth(240);
        bL.setAlignment(Pos.CENTER);
        bL.getChildren().addAll(nextLabel, scoreLabel, getContinueButton());


        nextRoot.getChildren().addAll(bgView, bL);
        mainStage.setScene(nextScene);


    }

    private static void goEndScene() {

        StackPane endRoot=new StackPane();
        Scene endScene=new Scene(endRoot,WIDTH,HEIGHT);

        Image bgImage = new Image(Main.class.getResource("/assets/background.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(WIDTH);
        bgView.setFitHeight(HEIGHT);
        bgView.setPreserveRatio(false);
        bgView.setEffect(new GaussianBlur(12));

        Label endLabel=new Label("GAME OVER! ");
        endLabel.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 48));
        endLabel.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 4, 0.3, 0, 2);");
        
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        scoreLabel.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0.3, 0, 2);");

        Button restartButton = getloseButton();
        
        VBox scoreList = new VBox(5);
        scoreList.setAlignment(Pos.CENTER);
        Label header = new Label("Top 5 Scores:");
        header.setStyle("-fx-text-fill: #FFE09A; -fx-font-size: 20px;");
        scoreList.getChildren().add(header);

        List<ScoreManager.ScoreEntry> entries = ScoreManager.loadEntries();
        for (int i = 0; i < entries.size(); i++) {
        	ScoreManager.ScoreEntry entry = entries.get(i);
            Label l = new Label((i + 1) + ". " + entry.username + " " + entry.score + " pts (" +entry.timestamp + ")");
            l.setStyle("-fx-text-fill: #FFE09A;");
            scoreList.getChildren().add(l);
        }
        
        
        

        VBox bL=new VBox(20);//ButtonAndLabel
        bL.setStyle("-fx-background-color: rgba(251,209,139,0.0);" +  // Şeffaf amber tonu
                "-fx-background-radius: 12;" +
                "-fx-padding: 16px;");
        bL.setPrefWidth(400);
        bL.setPrefHeight(300);
        bL.setAlignment(Pos.CENTER);
        bL.getChildren().addAll(endLabel, scoreLabel, scoreList, restartButton, getExitButton());


        endRoot.getChildren().addAll(bgView, bL);
        StackPane.setAlignment(bL, Pos.CENTER);
        
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("Oyuncu");
            dialog.setTitle("İsim Gir");
            dialog.setHeaderText("Skorun kaydedilecek. Lütfen ismini yaz:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                ScoreManager.saveScore(name, score);
            });
        });

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
        boolean[][] isPath = new boolean[gridSize][gridSize];

        if (pathCoordinates.isEmpty()) {
            System.err.println("Error: Coordinates could not be loaded.");
            System.exit(1);
        }

        for (int[] coord : pathCoordinates) {
            if (coord[0] >= 0 && coord[0] < gridSize && coord[1] >= 0 && coord[1] < gridSize) {
                isPath[coord[0]][coord[1]] = true;
            } else {
                System.err.println("Warning: Invalid coordinate in path data: [" + coord[0] + "," + coord[1] + "]");
            }
        }
        Image side1 = new Image(getClass().getResourceAsStream("/assets/road5.png"));
        roadPattern = new ImagePattern(side1);

        Image side2 = new Image(getClass().getResourceAsStream("/assets/alternate_road.png"));

        // Create and animate grid tiles
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                if (isPath[row][col]) {
                    tile.setFill(alternateRoadPattern);
                } else {
                    ImagePattern pattern = new ImagePattern(
                            Math.random() < 0.5 ? side1 : side2);
                    tile.setFill(pattern);
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
                pt.setDelay(Duration.millis((row * gridSize + col) * 12));
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
        hud.setStyle("-fx-background-color: rgba(251,209,139,0.0); " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.3, 0, 4);");
        hud.setPrefWidth(240);
        hud.setAlignment(Pos.CENTER);

        Image singleShotImage = new Image("/assets/towers/singleshottower.png");
        ImageView image=new ImageView(singleShotImage);
        image.setFitWidth(30);
        image.setFitHeight(30);
        VBox vbox1 = new VBox();
        vbox1.setAlignment(Pos.CENTER);
        Label label1 = new Label("Single Shot Tower");
        label1.setStyle("-fx-text-fill: #FFE09A");
        Label label2 = new Label("50$");
        label2.setStyle("-fx-text-fill: #FFE09A");
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
        label3.setStyle("-fx-text-fill: #FFE09A");
        Label label4 = new Label("120$");
        label4.setStyle("-fx-text-fill: #FFE09A");
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
        label5.setStyle("-fx-text-fill: #FFE09A");
        Label label6 = new Label("150$");
        label6.setStyle("-fx-text-fill: #FFE09A");
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
        label7.setStyle("-fx-text-fill: #FFE09A");
        Label label8 = new Label("200$");
        label8.setStyle("-fx-text-fill: #FFE09A");
        vbox4.getChildren().addAll(image4,label7, label8);
        Button missile= new Button();
        missile.setGraphic(vbox4);




        singleShot.setOnAction(e -> {
            selectedTowerType = 1;
            selectedTower = new SingleShotTower(0, 0, gameOverlay);
            draggingTower = true;

            Circle circle = selectedTower.getRangeCircle();
            circle.setVisible(true);

            // Menzil ve kule sahneye ekleniyor
            gameOverlay.getChildren().add(selectedTower.getRangeCircle());
            gameOverlay.getChildren().add(selectedTower.getNode());
            gameOverlay.getChildren().add((selectedTower).getHealthBar());

        });
        laser.setOnAction(e -> {
            selectedTowerType = 2;
            selectedTower = new LaserTower(0, 0, gameOverlay);
            draggingTower = true;
            Circle circle = selectedTower.getRangeCircle();
            circle.setVisible(true);

            gameOverlay.getChildren().add(selectedTower.getRangeCircle());
            gameOverlay.getChildren().add(selectedTower.getNode());
            gameOverlay.getChildren().add((selectedTower).getHealthBar());
        });
        tripleShot.setOnAction(e -> {
            selectedTowerType = 3;
            selectedTower = new TripleShotTower(0, 0, gameOverlay);
            draggingTower = true;
            Circle circle = selectedTower.getRangeCircle();
            circle.setVisible(true);

            gameOverlay.getChildren().add(selectedTower.getRangeCircle());
            gameOverlay.getChildren().add(selectedTower.getNode());
            gameOverlay.getChildren().add((selectedTower).getHealthBar());
        });
        missile.setOnAction(e -> {
            selectedTowerType = 4;
            selectedTower = new MissileLauncherTower(0, 0, gameOverlay);
            draggingTower = true;
            Circle circle = selectedTower.getRangeCircle();
            circle.setVisible(true);

            gameOverlay.getChildren().add(selectedTower.getRangeCircle());
            gameOverlay.getChildren().add(selectedTower.getNode());
            gameOverlay.getChildren().add((selectedTower).getHealthBar());
        });

        for (Button b : new Button[]{singleShot, laser, tripleShot, missile}) {
            b.setFocusTraversable(false);
            b.setPrefWidth(150);
            b.setPrefHeight(90);
            b.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-background-color: rgba(251,209,139,0.1);" +  // FBD18B'nin %80 saydam hali
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: #3E2F20;"
            );
            b.setTextFill(Color.web("#3E2F20"));
        }

        // Style labels
        livesLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFE09A;");
        moneyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFE09A;");
        debugLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFE09A;");
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFE09A;");
        waveCountdownLabel = new Label("Next wave: --");
        waveCountdownLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #FFE09A;");
        hud.getChildren().addAll(livesLabel, moneyLabel, waveCountdownLabel, scoreLabel, singleShot, laser, tripleShot, missile);
        return hud;
    }

    /**
     * Setup tower placement and dragging functionality
     */
    public void setupTowerPlacement() {
        gameOverlay.setOnMouseMoved(e -> {
            if (draggingTower && selectedTower != null) {
                // Sahne koordinatlarıyla güncelle
                selectedTower.setPosition(e.getSceneX(), e.getSceneY());


            }
        });
        gameOverlay.setOnMouseClicked(e -> {
            if (draggingTower && selectedTower != null) {

                int price = (int) selectedTower.getPrice();
                if (money < price) {
                    System.out.println("❌ Yetersiz para! Kule elden gitti.");
                    gameOverlay.getChildren().remove(selectedTower.getNode());
                    gameOverlay.getChildren().remove(selectedTower.getRangeCircle());
                    gameOverlay.getChildren().remove(selectedTower.getHealthBar());
                    draggingTower = false;
                    selectedTower = null;
                    selectedTowerType = 0;
                    return;
                }
                // col,row hesaplamanı buraya al
                double clickX = e.getSceneX();
                double clickY = e.getSceneY();
                int col = (int)((clickX - offsetX) / gridUnit);
                int row = (int)((clickY - offsetY) / gridUnit);
                // (istersen path ve placedTowerCells kontrolü yap)

                boolean invalid = false;
                if (clickX < offsetX || clickY < offsetY
                        || col < 0 || col >= gridSize || row < 0 || row >= gridSize) {
                    invalid = true;
                }
                for (int[] p : pathCoordinates) {
                    if (p[0]==row && p[1]==col) { invalid = true; break; }
                }

                for (int[] p : placedTowerCells) {
                    if (p[0]==row && p[1]==col) { invalid = true; break; }
                }
                if (invalid) {
                    // Preview’u sahneden kaldır
                    gameOverlay.getChildren().remove(selectedTower.getRangeCircle());
                    gameOverlay.getChildren().remove(selectedTower.getNode());
                    gameOverlay.getChildren().remove(selectedTower.getHealthBar());
                    // Dragging bitir
                    draggingTower = false;
                    selectedTower = null;
                    selectedTowerType = 0;
                    return;
                }


                if (clickX >= 1520)
                    return;

                // Prevent tower placement outside grid
                if (clickX < offsetX || clickY < offsetY)
                    return;

                if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
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


                decreaseMoney(price);

                // 1) preview zaten sahnede: artık final pozisyona al
                double centerX = offsetX + col * gridUnit + TILE_SIZE / 2;
                double centerY = offsetY + row * gridUnit + TILE_SIZE / 2;
                selectedTower.setPosition(centerX, centerY);
                selectedTower.setGridPosition(row, col);
                placedTowerCells.add(new int[]{row, col});

                // 2) preview node’larını sahneden temizle
                gameOverlay.getChildren().remove(selectedTower.getRangeCircle());
                gameOverlay.getChildren().remove(selectedTower.getNode());

                gameOverlay.getChildren().add(selectedTower.getNode());
                Circle placedCircle = selectedTower.getRangeCircle();
                gameOverlay.getChildren().add(placedCircle);
                placedCircle.setVisible(false);

                // 3) gerçek kuleyi oyuna ekle
                Game.addTower(selectedTower);

                Tower placed = selectedTower;
                // Sağ tık satma ve sol tık sürükleme handler’ları:
                placed.getNode().setOnMousePressed(ev -> {
                    if (ev.isSecondaryButtonDown()) {
                        // sat
                        increaseMoney(placed.getPrice());
                        gameOverlay.getChildren().removeAll(placed.getNode(), placed.getRangeCircle(), placed.getHealthBar());
                        int[] g = placed.getGridPosition();
                        placedTowerCells.removeIf(p -> p[0]==g[0] && p[1]==g[1]);
                        Game.removeTower(placed);
                    } else {
                        // sol tıkla hareket başlat
                        draggingTower = true;
                        selectedTower = placed;
                        placed.getRangeCircle().setVisible(true);
                    }
                });
                placed.getNode().setOnMouseDragged(ev -> {
                    if (draggingTower && selectedTower==placed) {
                        placed.setPosition(ev.getSceneX(), ev.getSceneY());
                    }
                });
                placed.getNode().setOnMouseReleased(ev -> {
                    if (draggingTower && selectedTower==placed) {
                        draggingTower = false;
                        placed.getRangeCircle().setVisible(false);

                        // 2) Sahne koordinatlarını al
                        double mouseX = ev.getSceneX();
                        double mouseY = ev.getSceneY();

                        int col1 = (int)((mouseX - offsetX) / gridUnit);
                        int row1 = (int)((mouseY - offsetY) / gridUnit);

                        // 3) Hücre indeksine dönüştür


                        // 4) Geçersiz mi kontrol et?
                        boolean invalid1 = false;
                        if (row1 < 0 || row1 >= gridSize || col1 < 0 || col1 >= gridSize) {
                            increaseMoney(placed.getPrice());
                            // görselleri sahneden kaldır
                            gameOverlay.getChildren().removeAll(placed.getNode(), placed.getRangeCircle(), selectedTower.getHealthBar());

                            placedTowerCells.removeIf(p ->
                                    p[0] == placed.getGridPosition()[0]
                                            && p[1] == placed.getGridPosition()[1]
                            );
                            Game.removeTower(placed);
                            // seçimi temizle
                            selectedTower = null;
                            selectedTowerType = 0;
                            return;
                        }
                        boolean invalidPlacement = false;

                        for (int[] p : pathCoordinates) {
                            if (p[0] == row1 && p[1] == col1) {
                                invalidPlacement = true;
                                break;
                            }
                        }
                        if (!invalidPlacement) {
                            for (int[] p : placedTowerCells) {
                                if (p[0] == row1 && p[1] == col1
                                        && !(p[0] == placed.getGridPosition()[0]
                                        && p[1] == placed.getGridPosition()[1])) {
                                    invalidPlacement = true;
                                    break;
                                }
                            }
                        }

                        if (invalidPlacement) {
                            // 5a) Hatalıysa orijinal hücresine dön
                            double origX = offsetX
                                    + placed.getGridPosition()[1] * gridUnit
                                    + TILE_SIZE/2;
                            double origY = offsetY
                                    + placed.getGridPosition()[0] * gridUnit
                                    + TILE_SIZE/2;
                            placed.setPosition(origX, origY);
                        } else {
                            // 6) geçerli yeni hücreye taşı
                            // önce eski kaydı sil
                            placedTowerCells.removeIf(p ->
                                    p[0] == placed.getGridPosition()[0]
                                            && p[1] == placed.getGridPosition()[1]
                            );

                            double newX = offsetX + col1 * gridUnit + TILE_SIZE/2;
                            double newY = offsetY + row1 * gridUnit + TILE_SIZE/2;


                            placed.setPosition(newX, newY);
                            placed.setGridPosition(row1, col1);
                            placedTowerCells.add(new int[]{row1, col1});
                        }


                        selectedTower = null;
                        selectedTowerType = 0;
                    }
                });

                // 4) temizle
                draggingTower = false;
                selectedTower = null;
                selectedTowerType = 0;
                return;  // buradan çık, alt kod çalışmasın
            }



            // Convert click coordinates to grid position


            // Check if grid position is valid


            if (selectedTowerType < 1 || selectedTowerType > 4) return;

            double clickX = e.getSceneX();
            double clickY = e.getSceneY();

            int col = (int)((clickX - offsetX) / gridUnit);
            int row = (int)((clickY - offsetY) / gridUnit);

            // Calculate center of grid cell for tower placement
            double cellCenterX = offsetX + col * gridUnit + TILE_SIZE / 2;
            double cellCenterY = offsetY + row * gridUnit + TILE_SIZE / 2;

            // Create tower based on selected type
            Tower tower = switch (selectedTowerType) {
                case 1 -> new SingleShotTower(cellCenterX, cellCenterY, gameOverlay);
                case 2 -> new LaserTower(cellCenterX, cellCenterY, gameOverlay);
                case 3 -> new TripleShotTower(cellCenterX, cellCenterY, gameOverlay);
                case 4 -> new MissileLauncherTower(cellCenterX, cellCenterY, gameOverlay);
                default -> null;
            };
            if (tower != null) {
                // Check if player has enough money
                if (money < tower.getPrice()) {
                    return;
                }

                // Deduct tower cost


                // Add tower to game








                // Set tower grid position
                tower.setGridPosition(row, col);
                placedTowerCells.add(new int[]{row, col});

                // Handle tower selection and right-click to sell
                tower.getNode().setOnMousePressed(ev -> {
                    if (ev.isPrimaryButtonDown()) {
                        draggingTower = true;
                        selectedTower = tower;
                        tower.getRangeCircle().setVisible(true);
                    }

                    if (ev.isSecondaryButtonDown()) {
                        // Sell tower on right-click
                        increaseMoney(tower.getPrice());

                        // Remove tower from display
                        gameOverlay.getChildren().removeAll(tower.getNode(), tower.getRangeCircle(), tower.getHealthBar());

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
                        tower.setPosition(ev.getSceneX(), ev.getSceneY());
                    }
                });

                // Handle tower placement after drag
                tower.getNode().setOnMouseReleased(ev -> {
                    dragging = false;
                    selectedTower = null;
                    tower.getRangeCircle().setVisible(false);

                    double mouseX = ev.getSceneX();
                    double mouseY = ev.getSceneY();

                    int col1 = (int)((mouseX - offsetX) / gridUnit);
                    int row1 = (int)((mouseY - offsetY) / gridUnit);

                    boolean invalid = false;

                    // Check if new position is on path
                    for (int[] coord : pathCoordinates) {
                        if (coord[0] == row1 && coord[1] == col1) {
                            invalid = true;

                        }
                    }

                    for (int[] placed : placedTowerCells) {
                        if (placed[0] == row1 && placed[1] == col1)
                            invalid = true;
                    }

                    if (invalid) {
                        // geri koy
                        double origX = offsetX + tower.getGridPosition()[1] * gridUnit + TILE_SIZE/2;
                        double origY = offsetY + tower.getGridPosition()[0] * gridUnit + TILE_SIZE/2;
                        tower.setPosition(origX, origY);
                        gameOverlay.getChildren().remove((tower).getHealthBar());
                        return;
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
        double gridWidth = (TILE_SIZE + SPACING) * gridSize - SPACING;
        double gridHeight = (TILE_SIZE + SPACING) * gridSize - SPACING;

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
        double randn = Math.random();
        //if (randn < 0.4) {
        //    spawnEnemyArcher();
//
        //}
        enemies.add(currentEnemy);
        Game.enemies.add(currentEnemy);

        // Start enemy movement along the path
        currentEnemy.moveAlongPath(pathCoordinates);
    }

    public void spawnEnemyArcher() {
        currentEnemy = new Archer(30, gameOverlay); // 100:health
        enemies.add(currentEnemy);
        Game.enemies.add(currentEnemy);

        // Start enemy movement along the path
        currentEnemy.moveAlongPath(pathCoordinates);
    }


    /**
     * Static method to decrease player lives
     */
    public static void decreaseLives() {
        lives --;
        livesLabel.setText("Lives: " + lives);

        // Game over condition
        if (lives <= 0 && !gameOverTriggered) {
        	if (!gameOverTriggered) {
                gameOverTriggered = true;
                
            }
            System.out.println("💀 Can 0 oldu, oyun bitti.");
            Game.enemies.clear(); // Düşmanları da sıfırla
            mainStage.getScene().getRoot().setDisable(true);
            
            
            
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
    
    private static Button getExitButton() {
    	Button exitButton = new Button("Exit Game");
    	exitButton.setAlignment(Pos.CENTER);
        exitButton.setTranslateY(150);
    	exitButton.setPrefWidth(200);
    	exitButton.setPrefHeight(50);
    	exitButton.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-background-color: #c29b57;" +
                        "-fx-text-fill: black;" +
                        "-fx-background-radius: 40;" +
                        "-fx-border-radius: 40;"
        );
    	
    	exitButton.setOnAction(e -> {
            Platform.exit();
            
        });
    	
    	return exitButton;
    }
    
    public void resetGame() {
        for (Enemy e : enemies) {
            e.stop();  // Enemy sınıfında stop() metodunu yazmalısın
            gameOverlay.getChildren().remove(e.getView());
            gameOverlay.getChildren().remove(e.getHealthBar());
        }

        Game.enemies.clear();
        enemies.clear();

        // Kuleleri sahneden kaldır
        for (Tower t : Game.getTowers()) {
            gameOverlay.getChildren().remove(t.getNode());
            gameOverlay.getChildren().remove(t.getRangeCircle());
            gameOverlay.getChildren().remove(t.getHealthBar());
        }
        Game.getTowers().clear();

        // Mermileri sahneden kaldır
        for (Bullet b : Game.getBullets()) {
            gameOverlay.getChildren().remove(b.getNode());
        }
        Game.getBullets().clear();

        for (Missile m : Game.getMissiles()) {
            gameOverlay.getChildren().remove(m.getNode());
        }
        Game.getMissiles().clear();

        if (waveTimeLine != null)    waveTimeLine.stop();
        if (countdownTimer != null)  countdownTimer.stop();
        gameOverTriggered = false;

        // Yerleşim yerlerini sıfırla
        placedTowerCells.clear();

        // Oyun değişkenlerini sıfırla
        money = 100;
        lives = 5;
        score = 0;
        if (scoreLabel != null)
            scoreLabel.setText("Score: " + score);

        if (Main.transitions != null) {
            Main.transitions.forEach(Animation::stop);
        }

        if (moneyLabel != null) {
            moneyLabel.setText("Money: $" + money);
        }
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
        }
        gameLoop.start();


    }
    public void OtherResetGame() {
        System.out.println("BURAYA GİRİYO!");
        for (Enemy e : enemies) {
            e.stop();  // Enemy sınıfında stop() metodunu yazmalısın
            gameOverlay.getChildren().remove(e.getView());
            gameOverlay.getChildren().remove(e.getHealthBar());
        }

        Game.enemies.clear();
        enemies.clear();

        // Kuleleri sahneden kaldır
        for (Tower t : Game.getTowers()) {
            gameOverlay.getChildren().remove(t.getNode());
            gameOverlay.getChildren().remove(t.getRangeCircle());
            gameOverlay.getChildren().remove(t.getHealthBar());
        }
        Game.getTowers().clear();

        // Mermileri sahneden kaldır
        for (Bullet b : Game.getBullets()) {
            gameOverlay.getChildren().remove(b.getNode());
        }
        Game.getBullets().clear();

        for (Missile m : Game.getMissiles()) {
            gameOverlay.getChildren().remove(m.getNode());
        }
        Game.getMissiles().clear();

        // Yerleşim yerlerini sıfırla
        placedTowerCells.clear();

        // Oyun değişkenlerini sıfırla
        lives = 5;
        finishedWaveCount=0;
        increaseScore((currentLevel - 1) * 100);

        if (Main.transitions != null) {
            Main.transitions.forEach(Animation::stop);
        }

        if (moneyLabel != null) {
            moneyLabel.setText("Money: $" + money);
        }
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
        }
        gameLoop.start();
   	/* if (waveTimeline != null) {
   		 waveTimeline.stop();
   	 }*/


    }

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }

    private static Button getloseButton() {
        if(loseButton==null) {
            loseButton = new Button("Restart Game");
        }



        loseButton.setPrefWidth(400);
        loseButton.setPrefHeight(150);
        loseButton.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-background-color: #c29b57;" +
                        "-fx-text-fill: black;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 2px;" +
                        "-fx-background-radius: 40;" +
                        "-fx-border-radius: 40;"
        );


        return loseButton;
    }


    private Button getContinueButton() {
        if (continueButton == null) {
            continueButton = new Button("Continue To Next Level");
            continueButton.setPrefWidth(400);
            continueButton.setPrefHeight(150);
            continueButton.setStyle(
                    "-fx-font-size: 32px;" +
                            "-fx-background-color: #c29b57;" +
                            "-fx-text-fill: black;" +
                            "-fx-background-radius: 40;" +
                            "-fx-border-radius: 40;"
            );
            continueButton.setOnAction(e->{ 
        		try { 
        			Scene nextLevelScene = getGameScene(new StackPane()); 
        			mainStage.setScene(nextLevelScene); 
        			transitions.forEach(Animation::play);//Start animation again from scratch
        			
        			if (currentLevel==4 || currentLevel==5) {
                    	maxDelay=2688;
                    }// from scratch start we 

        	            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(maxDelay), ev -> {
        	                addGameButtons();
        	                setupTowerPlacement();
        	                scheduleWaves(currentLevel);
        	            }));
        	            delayTimeline.play();
        			// Animasyonları yeniden başlat transitions.forEach(Animation::play); // Yeni leveli başlat scheduleWaves(currentLevel); } catch(Exception ex) { System.out.println("exception found"); } }); return continueButton;
        		}
        		catch(Exception ex) {
        			System.out.println("continue exeption");
        		}
        		increaseMoney(100);
     	});
        }
        return continueButton;
    }



    public class AudioPlayer {
        private Clip clip;

        public void playLoop(String filepath) {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filepath))) {
                clip = AudioSystem.getClip();
                clip.open(ais);

                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-15.0f); // desibel cinsinden → 0.0f = tam ses, -80f = sessiz

                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            if (clip != null && clip.isRunning()) clip.stop();
        }
    }
    private Button getWonButton() {
        wonButton = new Button("Play Again!");
        Image bg = new Image(getClass().getResource("/assets/background.png").toExternalForm());
        ImageView bgView = new ImageView(bg);
        bgView.setFitWidth(WIDTH);
        bgView.setFitHeight(HEIGHT);
        bgView.setPreserveRatio(false);
        bgView.setEffect(new GaussianBlur(12));
        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        wonButton.setPrefWidth(400);
        wonButton.setPrefHeight(150);
        wonButton.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-background-color: #c29b57;" +
                        "-fx-text-fill: black;" +
                        "-fx-background-radius: 40;" +
                        "-fx-border-radius: 40;"
        );
        wonButton.setOnAction(e -> {
            currentLevel = 1;
            finishedWaveCount = 0;
            resetGame();

            if (waveTimeLine != null) waveTimeLine.stop();
            if (countdownTimer != null) countdownTimer.stop();

            StackPane newGameRoot = new StackPane();
            Scene newGameScene;
            try {
                newGameScene = getGameScene(newGameRoot);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                return;
            }

            mainStage.setScene(newGameScene);
            transitions.forEach(Animation::play);
            addGameButtons();
            setupTowerPlacement();
            scheduleWaves(currentLevel);
        });


        return wonButton;

    }

    private void goWonScene() {
        Label nextLabel=new Label("You Won The Game!");
        nextLabel.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 48));
        nextLabel.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 4, 0.3, 0, 2);");
        
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        scoreLabel.setStyle("-fx-text-fill: #FFE09A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0.3, 0, 2);");

        Image bg = new Image(getClass().getResource("/assets/background.png").toExternalForm());
        ImageView bgView = new ImageView(bg);
        bgView.setFitWidth(WIDTH);
        bgView.setFitHeight(HEIGHT);
        bgView.setPreserveRatio(false);
        bgView.setEffect(new GaussianBlur(12));
        
        VBox scoreList = new VBox(5);
        scoreList.setAlignment(Pos.CENTER);
        Label header = new Label("Top 5 Scores:");
        header.setStyle("-fx-text-fill: #FFE09A; -fx-font-size: 20px;");
        scoreList.getChildren().add(header);

        List<ScoreManager.ScoreEntry> entries = ScoreManager.loadEntries();
        for (int i = 0; i < entries.size(); i++) {
            ScoreManager.ScoreEntry entry = entries.get(i);
            Label l = new Label((i + 1) + ". " + entry.username + " " + entry.score + " pts (" + entry.timestamp + ")");
            l.setStyle("-fx-text-fill: #FFE09A;");
            scoreList.getChildren().add(l);
        }




        VBox won=new VBox(10);
        won.setAlignment(Pos.CENTER);
        won.setPrefWidth(400);
        won.setStyle(
                "-fx-background-color: rgba(251,209,139,0.0);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20px;"
        );
        won.getChildren().addAll(nextLabel, scoreLabel, scoreList, getWonButton(), getExitButton());

        StackPane paneWon=new StackPane();
        paneWon.getChildren().addAll(bgView, won); // önce arka plan, sonra UI
        StackPane.setAlignment(won, Pos.CENTER);

        Scene wonScene=new Scene(paneWon,WIDTH,HEIGHT);
        
        

        mainStage.setScene(wonScene);
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("Oyuncu");
            dialog.setTitle("İsim Gir");
            dialog.setHeaderText("Skorun kaydedilecek. Lütfen ismini yaz:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                ScoreManager.saveScore(name, score);
            });
        });

    }
    
    public static void increaseScore(int amount) {
        score += amount;
        scoreLabel.setText("Score: " + score);
    }



}