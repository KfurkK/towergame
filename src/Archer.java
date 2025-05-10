import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.List; // Required for List
import java.util.Comparator; // Required for sorting

/**
 * An Archer enemy that shoots arrows at nearby towers
 */
public class Archer extends Enemy {
    private static final int ATTACK_RANGE = 90; // Pixels note that one square is 45 pxs wide
    private static final int ATTACK_DAMAGE = 5;
    private static final int ATTACK_COOLDOWN = 2000; // Milliseconds

    private final Pane gamePane;
    private long lastAttackTime = 0;
    private Timeline attackTimer;

    public static int damageValue = ATTACK_DAMAGE; // Using ATTACK_DAMAGE for consistency

    /**
     * Create a new Archer enemy
     *
     * @param health   The enemy's health
     * @param gamePane The pane to add the enemy to
     */
    public Archer(int health, Pane gamePane) {
        super(health, gamePane);
        this.gamePane = gamePane;

        // Load archer-specific image
        this.img = new Image(getClass().getResource("/assets/archer.png").toExternalForm()); //
        this.getView().setImage(this.img); //

        // Start attacking towers periodically
        startAttackLoop(); //
    }

    /**
     * Start a loop to check for towers in range and attack them
     */
    private void startAttackLoop() {
        attackTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> checkAndAttackTowers())); //
        attackTimer.setCycleCount(Timeline.INDEFINITE); //
        attackTimer.play(); //
    }

    /**
     * Check for towers in range and attack if possible
     */
    private void checkAndAttackTowers() {
        if (!isAlive()) { //
            if (attackTimer != null) { //
                attackTimer.stop(); //
            }
            return; //
        }

        // Check cooldown
        long currentTime = System.currentTimeMillis(); //
        if (currentTime - lastAttackTime < ATTACK_COOLDOWN) { //
            return; //
        }

        // Get the nearest tower in range
        Tower nearestTower = findNearestTowerInRange(); //
        if (nearestTower != null) { //
            attackTower(nearestTower); //
            lastAttackTime = currentTime; //
        }
    }

    /**
     * Find the nearest tower within attack range
     *
     * @return The nearest Tower object, or null if none in range
     */
    private Tower findNearestTowerInRange() {
        // Get towers from the Game class
        List<Tower> towers = Game.getTowers(); //
        if (towers == null || towers.isEmpty()) {
            return null;
        }

        Tower closestTower = null;
        double minDistanceSq = ATTACK_RANGE * ATTACK_RANGE; // Use squared distance to avoid sqrt

        for (Tower tower : towers) {
            // Calculate distance from archer to tower
            // Note: Tower x, y are grid positions, convert to screen coordinates if necessary
            // Assuming Tower.getX() and Tower.getY() in Tower.java return screen coordinates or are compatible.
            // If Tower.getX()/getY() return grid indices, you need to convert them like in Main.java:
            // double towerScreenX = offsetX + tower.getGridPosition()[1] * gridUnit + TILE_SIZE / 2;
            // double towerScreenY = offsetY + tower.getGridPosition()[0] * gridUnit + TILE_SIZE / 2;
            // For simplicity, I'll assume tower.getX() and tower.getY() are screen coordinates.
            // If they are grid coordinates, they would need to be public fields or have public getters
            // for their screen coordinates or grid positions to perform the conversion.
            // The Tower class has public double x, y fields which seem to be screen coordinates.

            double dx = tower.x - this.getX(); //
            double dy = tower.y - this.getY(); //
            double distanceSq = dx * dx + dy * dy;

            if (distanceSq <= minDistanceSq) {
                minDistanceSq = distanceSq;
                closestTower = tower;
            }
        }
        // To ensure we are strictly within ATTACK_RANGE (not just <= ATTACK_RANGE)
        // and to pick the absolute closest if multiple are at the exact ATTACK_RANGE boundary,
        // an alternative approach is to filter first, then find the minimum.
        // However, the current approach is generally fine.

        // If you need the exact closest and then check range:
        /*
        return towers.stream()
                .filter(tower -> {
                    double dx = tower.x - this.getX();
                    double dy = tower.y - this.getY();
                    return (dx * dx + dy * dy) <= (ATTACK_RANGE * ATTACK_RANGE);
                })
                .min(Comparator.comparingDouble(tower -> {
                    double dx = tower.x - this.getX();
                    double dy = tower.y - this.getY();
                    return dx * dx + dy * dy;
                }))
                .orElse(null);
        */
        return closestTower; // Return the closest tower found within range
    }

    /**
     * Attack a tower with an arrow
     *
     * @param tower The tower to attack
     */
    private void attackTower(Tower tower) {
        // Calculate direction to tower
        double targetX = tower.x; // Assuming tower.x is the screen coordinate //
        double targetY = tower.y; // Assuming tower.y is the screen coordinate //
        double startX = this.getX(); //
        double startY = this.getY(); //

        // Create arrow
        Line arrow = new Line(startX, startY, startX, startY); //
        arrow.setStrokeWidth(2); //
        arrow.setStroke(Color.BROWN); //
        gamePane.getChildren().add(arrow); //

        // Create arrowhead
        Circle arrowHead = new Circle(3, Color.DARKGRAY); //
        arrowHead.setCenterX(startX); //
        arrowHead.setCenterY(startY); //
        gamePane.getChildren().add(arrowHead); //

        // Animate arrow
        Timeline arrowAnimation = new Timeline(); //
        double animationDuration = 500; // ms //

        for (int i = 0; i <= 10; i++) { //
            double t = i / 10.0; //
            double x = startX + (targetX - startX) * t; //
            double y = startY + (targetY - startY) * t; //

            KeyFrame frame = new KeyFrame(Duration.millis(animationDuration * t), //
                    e -> {
                        arrow.setEndX(x); //
                        arrow.setEndY(y); //
                        arrowHead.setCenterX(x); //
                        arrowHead.setCenterY(y); //
                    });
            arrowAnimation.getKeyFrames().add(frame); //
        }

        arrowAnimation.setOnFinished(e -> { //
            // Clean up arrow
            gamePane.getChildren().removeAll(arrow, arrowHead); //

            // Deal damage to tower
            damageTower(tower, ATTACK_DAMAGE); //

            // Create hit effect
            createHitEffect(targetX, targetY); //
        });

        arrowAnimation.play(); //
    }

    /**
     * Damage a tower
     *
     * @param tower The tower to damage
     * @param damage The amount of damage to deal
     */
    protected void damageTower(Tower tower, int damage) {
        if (tower != null) {
            // The Tower class has an abstract damage() method.
            // Subclasses like SingleShotTower implement it.
            // We need to ensure Archer.damageValue is correctly used or pass ATTACK_DAMAGE
            Archer.damageValue = damage; // Set the static damageValue before calling damage
            // This is how SingleShotTower expects to receive damage from an Archer
            tower.damage(damageValue); // Call the tower's own damage method //
        }
    }

    /**
     * Create an effect when arrow hits target
     *
     * @param x X coordinate of hit
     * @param y Y coordinate of hit
     */
    private void createHitEffect(double x, double y) { //
        // Create small explosion effect
        for (int i = 0; i < 8; i++) { //
            Circle particle = new Circle(2, Color.ORANGE); //
            particle.setCenterX(x); //
            particle.setCenterY(y); //
            gamePane.getChildren().add(particle); //

            // Random direction
            double angle = Math.random() * 2 * Math.PI; //
            double distance = Math.random() * 15; //

            TranslateTransition move = new TranslateTransition(Duration.millis(300), particle); //
            move.setByX(Math.cos(angle) * distance); //
            move.setByY(Math.sin(angle) * distance); //

            FadeTransition fade = new FadeTransition(Duration.millis(300), particle); //
            fade.setFromValue(1.0); //
            fade.setToValue(0.0); //

            fade.setOnFinished(e -> gamePane.getChildren().remove(particle)); //

            move.play(); //
            fade.play(); //
        }
    }

    /**
     * Clean up resources when archer dies or is removed
     */
    @Override
    public void die() { //
        if (attackTimer != null) { //
            attackTimer.stop(); //
        }
        super.die(); //
    }

    /**
     * Override stop method to also stop the attack timer
     */
    @Override
    public void stop() { //
        if (attackTimer != null) { //
            attackTimer.stop(); //
        }
        super.stop(); //
    }


    @Override
    protected void removeFromGame() {
        // 1. Stop Archer-specific activities
        if (attackTimer != null) {
            attackTimer.stop(); //
        }

        super.removeFromGame(); //
    }

    // Removed the abstract findNearestTower() as it's now implemented by findNearestTowerInRange()
}