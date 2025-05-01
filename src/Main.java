import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;
//50 olan doğru olan
// Color.web("FFCF50"), Color.web("FBC518")
public class Main extends Application {
    private final int WIDTH = 1920;
    private final int HEIGHT = 1080;
    private static final int GRID_SIZE = 10;
    private static final int TILE_SIZE = 45;
    private static final Color[] YELLOW_TONES = {
            Color.web("FFCF50"), Color.web("FBC518")
    };
    private static final Color PATH_COLOR = Color.web("FBEBE0");
    private final ArrayList<Animation> transitions = new ArrayList<>();

    public int money = 100;
    public int lives = 5;
    public int nextWave = 0; // these variables are not at the right place imo.

    private Label livesLabel = new Label("Lives: " + lives);
    private Label moneyLabel = new Label("Money: $" + money);
    private Label waveLabel = new Label("Next Wave: " + nextWave);

    private String pathToLevels = "C:/Users/kamil/IdeaProjects/towergame/src/levels/";

    // need to add dynamic level
    private String filePath = pathToLevels + "level1.txt";


    //F5ECD5 OLABİLİR
//FBE4D6 OLABİLİR
    @Override
    public void start(Stage primaryStage) throws Exception{

        Button startButton = getStartButton();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        StackPane gameRoot = new StackPane();
        Scene gameScene = getGameScene(gameRoot);

        root.setStyle("-fx-background-color: #FFF6DA;");
        root.getChildren().add(startButton);

        primaryStage.setTitle("Tower Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        startButton.setOnAction(e -> { // butona basıldığı zaman bunları yap diyorsun
            primaryStage.setScene(gameScene);
            transitions.forEach(Animation::play);
            //her bir karenin görünme animasyonu(opaklık küçülme) tek tek play ile çağrıldı
        });
    }

    private Scene getGameScene(StackPane gameRoot) {

        Scene gameScene = new Scene(gameRoot, WIDTH, HEIGHT);
        gameRoot.setStyle("-fx-background-color: #FFF6DA;");//FFF6DA DOĞRU OLAN

        GridPane grid = new GridPane();
        grid.setHgap(2.5);
        grid.setVgap(2.5);
        grid.setAlignment(Pos.CENTER);

        // Gri yolun konumu (örnek olarak orta satır)
        boolean[][] isPath = new boolean[GRID_SIZE][GRID_SIZE];

        // get the gray path
        try {
            ArrayList<Integer> grayPath = tools.readCoordinates("");
        }

        /*
        for (int col = 0; col < 10; col++) {
            isPath[2][col] = true;
        }
        for (int row = 3; row < 6; row++) {
            isPath[row][3] = true;
        }
        for (int col = 4; col < GRID_SIZE; col++) {
            isPath[5][col] = true;
        }
        */


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

                tile.setScaleX(0.1);  // Küçük başla
                tile.setScaleY(0.1);      //gereksiz gibi duruyorlar..

                grid.add(tile, col, row);

                // Fade Görünme animasyonu  OPAKLIK AYARI İÇİN
                FadeTransition ft = new FadeTransition(Duration.millis(500), tile);
                ft.setFromValue(0);
                ft.setToValue(1);

                //Scale Görünme animasyonu BÜYÜME AYARI İÇİN
                ScaleTransition st = new ScaleTransition(Duration.millis(500), tile);
                st.setFromX(0.1);
                st.setFromY(0.1);
                st.setToX(1.0);
                st.setToY(1.0);

                // Paralel olarak fade + scale animasyonu birlikte çalışsın
                ParallelTransition pt = new ParallelTransition(ft, st);

                pt.setDelay(Duration.millis((row * GRID_SIZE + col) * 12));
                transitions.add(pt);// animasyonu listeye ekledik böylece direkt run yapınca çalışmamış oldu beklettik yani
            }// her bir karenin görünme animasyonu transitions listesine eklendi
        }

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
        }//burayı değiştiririz belki forlu bu kısım nasıl yapılıyor bilmiyom

        livesLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        moneyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        waveLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");


        hud.getChildren().addAll(livesLabel, moneyLabel, waveLabel, singleShot, laser, tripleShot, missile);

        Pane overlay=new Pane();

        hud.setLayoutX(1520);
        hud.setLayoutY(280);

        overlay.getChildren().addAll(hud);//Tam olarak konumu ayarlamak için Pane ile setLayout kullandım
        gameRoot.getChildren().add(grid);//
        gameRoot.getChildren().add(overlay);//Başta StackPane aldığımız için bu şekilde yaptım
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