import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
	private final ImageView enemyView;
	private final Rectangle healthBar;
	private final Pane gamePane;
	private PathTransition currentTransition;

	// Constants
	private static final int TILE_SIZE = 45;
	private static final double SPACING = 2.5;

	/**
	 * Create a new enemy
	 * 
	 * @param health   The enemy's health
	 * @param gamePane The pane to add the enemy to
	 */
	public Enemy(int health, Pane gamePane) {
		this.health = health;
		this.maxHealth = health;
		this.gamePane = gamePane;

		Image img = new Image(getClass().getResource("/assets/soldier.png").toExternalForm());
		this.enemyView = new ImageView(img);
		enemyView.setFitWidth(TILE_SIZE * 0.8);
		enemyView.setFitHeight(TILE_SIZE * 0.8);

		this.healthBar = new Rectangle(TILE_SIZE, 5);
		healthBar.setFill(Color.GREEN);

		healthBar.translateXProperty()
				.bind(enemyView.translateXProperty().subtract((enemyView.getFitWidth() / 2.0) - 15));
		healthBar.translateYProperty().bind(enemyView.translateYProperty().subtract(enemyView.getFitHeight() / 2.0)
				.subtract(healthBar.getHeight() + 1) // lift +5px
		);

		// Add both to the pane
		gamePane.getChildren().addAll(enemyView, healthBar);
	}

	public void moveAlongPath(ArrayList<int[]> path) {
		Path movementPath = new Path();

		// Calculate grid positioning
		double gridCenterX = gamePane.getScene().getWidth() / 2;
		double gridCenterY = gamePane.getScene().getHeight() / 2;
		double gridWidth = (TILE_SIZE + SPACING) * 10 - SPACING;
		double gridHeight = (TILE_SIZE + SPACING) * 10 - SPACING;
		double offsetX = gridCenterX - gridWidth / 2;
		double offsetY = gridCenterY - gridHeight / 2;

		// Initial position
		int[] firstPoint = path.get(0);
		double startX = offsetX + firstPoint[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
		double startY = offsetY + firstPoint[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
		enemyView.setTranslateX(startX);
		enemyView.setTranslateY(startY);

		// Build the Path object
		movementPath.getElements().add(new MoveTo(startX, startY));
		for (int i = 1; i < path.size(); i++) {
			int[] pt = path.get(i);
			double x = offsetX + pt[1] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
			double y = offsetY + pt[0] * (TILE_SIZE + SPACING) + TILE_SIZE / 2;
			movementPath.getElements().add(new LineTo(x, y));
		}

		// 1) Use a PathTransition with a LINEAR interpolator
		PathTransition move = new PathTransition();
		move.setNode(enemyView);
		move.setPath(movementPath);
		move.setDuration(Duration.seconds(10)); // total travel time
		move.setInterpolator(Interpolator.LINEAR); // constant speed
		move.setCycleCount(1);
		move.setOnFinished(e -> {
			if (health > 0) {
				removeFromGame();
				Main.decreaseLives();
			}
		});
		this.currentTransition = move;
		move.play();
	}

	public void stop() {
		if (currentTransition != null) {
			currentTransition.stop();
		}
	}

	/**
	 * Damage the enemy and update health bar
	 */
	public void damage(double amount) {
		health -= amount;
		double percent = (double) health / maxHealth;
		healthBar.setWidth(TILE_SIZE * percent);

		if (percent < 0.3) {
			healthBar.setFill(Color.RED);
		} else if (percent < 0.6) {
			healthBar.setFill(Color.ORANGE);
		}

		if (health <= 0) {
			die();
		}
	}

	/**
	 * Play death animation and remove
	 */
	private void die() {
		FadeTransition fadeSprite = new FadeTransition(Duration.millis(300), enemyView);
		fadeSprite.setFromValue(1.0);
		fadeSprite.setToValue(0.0);

		FadeTransition fadeBar = new FadeTransition(Duration.millis(300), healthBar);
		fadeBar.setFromValue(1.0);
		fadeBar.setToValue(0.0);

		createExplosionEffect();
		Main.increaseMoney(10);

		ParallelTransition deathAnim = new ParallelTransition(fadeSprite, fadeBar);
		deathAnim.setOnFinished(e -> removeFromGame());
		deathAnim.play();
	}

	/**
	 * Simple particle explosion
	 */
	private void createExplosionEffect() {
		for (int i = 0; i < 20; i++) {
			javafx.scene.shape.Circle p = new javafx.scene.shape.Circle(4, Color.RED);
			p.setTranslateX(enemyView.getTranslateX());
			p.setTranslateY(enemyView.getTranslateY());
			gamePane.getChildren().add(p);

			FadeTransition fade = new FadeTransition(Duration.millis(500), p);
			fade.setFromValue(1.0);
			fade.setToValue(0.0);

			TranslateTransition tr = new TranslateTransition(Duration.millis(500), p);
			double angle = Math.random() * 2 * Math.PI;
			double distance = Math.random() * TILE_SIZE;
			tr.setByX(Math.cos(angle) * distance);
			tr.setByY(Math.sin(angle) * distance);

			ParallelTransition pt = new ParallelTransition(fade, tr);
			pt.setOnFinished(e -> gamePane.getChildren().remove(p));
			pt.play();
		}
	}

	/**
	 * Remove enemy from the game
	 */
	private void removeFromGame() {
		gamePane.getChildren().removeAll(enemyView, healthBar);
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
	public ImageView getView() {
		return enemyView;
	}

	// Position getters
	public double getX() {
		return enemyView.getTranslateX();
	}

	public double getY() {
		return enemyView.getTranslateY();
	}

	public Rectangle getHealthBar() {
		return healthBar;
	}

}