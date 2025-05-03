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
 * A simple enemy that follows a path
 */
public class Enemy {
    public int health;
    private final int maxHealth;
    private final Circle enemyCircle;
    private final Rectangle healthBar;
    private final Pane gamePane;

    // Constants
    private static final int TILE_SIZE = 45;
    private static final double SPACING = 2.5; // The grid's horizontal and vertical gap

    /**
     * Create a new enemy
     * @param health The enemy's health
     * @param gamePane The pane to add the enemy to
     */
    public Enemy(int health, Pane gamePane) {
        this.health = health;
        this.maxHealth = health;
        this.gamePane = gamePane;

        // Create the enemy circle
        this.enemyCircle = new Circle(TILE_SIZE / 5);
        this.enemyCircle.setFill(Color.RED);

        // Create health bar above enemy
        this.healthBar = new Rectangle(TILE_SIZE, 5);
        this.healthBar.setFill(Color.GREEN);

        // Position health bar above enemy
        this.healthBar.setTranslateY(-TILE_SIZE / 2);
        this.healthBar.setTranslateX(-TILE_SIZE / 2);

        // Add enemy and health bar to the game pane
        gamePane.getChildren().addAll(enemyCircle, healthBar);
    }

    /**
     * Move the enemy along a path
     * @param path List of coordinates [row, col] representing the path
     */
    public void moveAlongPath(ArrayList<int[]> path) {
        // Create a path for the enemy to follow
        Path movementPath = new Path();

        // Get center position of the grid in the scene
        double gridCenterX = gamePane.getScene().getWidth() / 2;
        double gridCenterY = gamePane.getScene().getHeight() / 2;

        // Calculate grid offset (to center it)
        double gridWidth = (TILE_SIZE + SPACING) * 10 - SPACING;
        double gridHeight = (TILE_SIZE + SPACING) * 10 - SPACING;
        double offsetX = gridCenterX - gridWidth / 2;
        double offsetY = gridCenterY - gridHeight / 2;

        // Convert the first coordinate to pixel position
        int[] firstPoint = path.get(0);
        double startX = offsetX + firstPoint[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
        double startY = offsetY + firstPoint[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;

        // Set initial positions
        enemyCircle.setTranslateX(startX);
        enemyCircle.setTranslateY(startY);
        healthBar.setTranslateX(startX - TILE_SIZE / 2);
        healthBar.setTranslateY(startY - TILE_SIZE / 2);

        // Set the starting point of the path
        movementPath.getElements().add(new MoveTo(startX, startY));

        // Add all points to the path
        for (int i = 1; i < path.size(); i++) {
            int[] point = path.get(i);
            double x = offsetX + point[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
            double y = offsetY + point[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
            movementPath.getElements().add(new LineTo(x, y));
        }

        // Create animation for enemy circle
        PathTransition circleTransition = new PathTransition();
        circleTransition.setNode(enemyCircle);
        circleTransition.setPath(movementPath);
        circleTransition.setDuration(Duration.seconds(10));
        circleTransition.setCycleCount(1);

        // Create identical animation for health bar
        PathTransition healthBarTransition = new PathTransition();
        healthBarTransition.setNode(healthBar);
        healthBarTransition.setPath(movementPath);
        healthBarTransition.setDuration(Duration.seconds(10));
        healthBarTransition.setCycleCount(1);

        // When animation finishes, remove the enemy if it reached the end
        circleTransition.setOnFinished(e -> {
            if (health > 0) {
                // Enemy reached the end - player loses a life
                Main.decreaseLives(1);
                removeFromGame();
            }
        });

        // Start both animations
        circleTransition.play();
        healthBarTransition.play();
    }

    /**
     * Damage the enemy and update health bar
     * @param amount Amount of damage to deal
     */
    public void damage(int amount) {
        this.health -= amount;

        // Calculate health percentage
        double healthPercent = (double) this.health / this.maxHealth;

        // Update health bar width based on remaining health
        healthBar.setWidth(TILE_SIZE * healthPercent);

        // Adjust X position to keep health bar centered
        double widthDifference = TILE_SIZE - (TILE_SIZE * healthPercent);
        double newX = enemyCircle.getTranslateX() - (TILE_SIZE / 2) + (widthDifference / 2);
        healthBar.setTranslateX(newX);


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
//
        FadeTransition fadeHealth = new FadeTransition(Duration.millis(300), healthBar);
        fadeHealth.setFromValue(1.0);
        fadeHealth.setToValue(0.0);
//
        //// Create explosion effect with particles
        createExplosionEffect();
//
        // Add player money reward
        Main.increaseMoney(10);
//
        // Play animations
        ParallelTransition parallel = new ParallelTransition(fadeCircle, fadeHealth);
        parallel.setOnFinished(e -> removeFromGame());
        parallel.play();
        removeFromGame();

    }

    /**
     * Create a simple explosion effect with particles
     */
    private void createExplosionEffect() {
        for (int i = 0; i < 20; i++) {
            // Create a small particle circle
            Circle particle = new Circle(3, Color.ORANGE);
            particle.setTranslateX(enemyCircle.getTranslateX());
            particle.setTranslateY(enemyCircle.getTranslateY());
            gamePane.getChildren().add(particle);

            // Random angle and distance
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * TILE_SIZE; // always inside the pixel (max:45px)

            // Create fade animation
            FadeTransition fade = new FadeTransition(Duration.millis(500), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            // Create movement animation
            javafx.animation.TranslateTransition translate = new javafx.animation.TranslateTransition(Duration.millis(500), particle);

            translate.setByX(Math.cos(angle) * distance);
            translate.setByY(Math.sin(angle) * distance);

            // Play animations
            ParallelTransition pt = new ParallelTransition(fade, translate);
            pt.setOnFinished(e -> gamePane.getChildren().remove(particle));
            pt.play();
        }
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
}
