import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.ArrayList;

/**
 * A simple enemy that follows a path with a health bar displayed above it
 */
public class Enemy {
    private int health;
    private final int maxHealth;
    private final Circle enemyCircle;
    private final Rectangle healthBar;
    private final Pane gamePane;

    // Position tracking
    private double x = 0;
    private double y = 0;

    // Constants
    private static final int TILE_SIZE = 45;
    private static final double SPACING = 2.5;

    /**
     * Create a new enemy
     * @param health The enemy's health
     * @param gamePane The pane to add the enemy to
     */
    public Enemy(int health, Pane gamePane) {
        this.health = health;
        this.maxHealth = health;
        this.gamePane = gamePane;

        // Create enemy representation
        this.enemyCircle = new Circle(TILE_SIZE / 5);
        this.enemyCircle.setFill(Color.RED);

        // Create health bar above enemy
        this.healthBar = new Rectangle(TILE_SIZE, 5);
        this.healthBar.setFill(Color.GREEN);

        // Position health bar above enemy
        healthBar.translateXProperty().bind(
                enemyCircle.translateXProperty().subtract(TILE_SIZE / 2.0)
        );
        healthBar.translateYProperty().bind(
                enemyCircle.translateYProperty().subtract(enemyCircle.getRadius() + healthBar.getHeight())
        );

        // Add both elements to the game pane
        gamePane.getChildren().addAll(enemyCircle, healthBar);
    }

    /**
     * Move the enemy along a path
     * @param path List of coordinates [row, col] representing the path
     */
    public void moveAlongPath(ArrayList<int[]> path) {
        Path movementPath = new Path();

        // Calculate grid positioning
        double gridCenterX = gamePane.getScene().getWidth() / 2;
        double gridCenterY = gamePane.getScene().getHeight() / 2;
        double gridWidth = (TILE_SIZE + SPACING) * 10 - SPACING;
        double gridHeight = (TILE_SIZE + SPACING) * 10 - SPACING;
        double offsetX = gridCenterX - gridWidth / 2;
        double offsetY = gridCenterY - gridHeight / 2;

        // Set initial position
        int[] firstPoint = path.get(0);
        double startX = offsetX + firstPoint[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
        double startY = offsetY + firstPoint[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;

        this.x = startX;
        this.y = startY;
        enemyCircle.setTranslateX(startX);
        enemyCircle.setTranslateY(startY);

        // Create the path
        movementPath.getElements().add(new MoveTo(startX, startY));
        for (int i = 1; i < path.size(); i++) {
            int[] point = path.get(i);
            double x = offsetX + point[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
            double y = offsetY + point[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
            movementPath.getElements().add(new LineTo(x, y));
        }

        // Create animation for enemy movement
        PathTransition circleTransition = new PathTransition();
        circleTransition.setNode(enemyCircle);
        circleTransition.setPath(movementPath);
        circleTransition.setDuration(Duration.seconds(10));
        circleTransition.setCycleCount(1);

        // Handle end of path
        circleTransition.setOnFinished(e -> {
            if (health > 0) {
                removeFromGame();
            }
        });

        // Start animation
        circleTransition.play();
    }

    /**
     * Damage the enemy and update health bar
     * @param amount Amount of damage to deal
     */
    public void damage(double amount) {
        this.health -= amount;

        // Update health bar
        double healthPercent = (double) this.health / this.maxHealth;
        healthBar.setWidth(TILE_SIZE * healthPercent);

        // Change health bar color based on remaining health
        if (healthPercent < 0.3) {
            healthBar.setFill(Color.RED);
        } else if (healthPercent < 0.6) {
            healthBar.setFill(Color.ORANGE);
        }

        // If health is zero or below, enemy dies
        if (health <= 0) {
            die();
        }
    }

    /**
     * Kill the enemy and play death animation
     */
    private void die() {
        // Simple fade out animation
        FadeTransition fadeCircle = new FadeTransition(Duration.millis(300), enemyCircle);
        fadeCircle.setFromValue(1.0);
        fadeCircle.setToValue(0.0);

        FadeTransition fadeHealth = new FadeTransition(Duration.millis(300), healthBar);
        fadeHealth.setFromValue(1.0);
        fadeHealth.setToValue(0.0);

        // Play animations
        ParallelTransition parallel = new ParallelTransition(fadeCircle, fadeHealth);
        parallel.setOnFinished(e -> removeFromGame());
        parallel.play();
    }

    /**
     * Remove enemy from the game
     */
    private void removeFromGame() {
        gamePane.getChildren().removeAll(enemyCircle, healthBar);
    }

    /**
     * Check if the enemy is still alive
     */
    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Get the enemy circle node
     */
    public Circle getView() {
        return enemyCircle;
    }

    // Position getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}