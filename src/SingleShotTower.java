import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SingleShotTower extends Tower {
	private long lastShotTime = 0;
	private long shootInterval = 750;  // 0.75 saniye
	public int damage = 10;
	public ImageView image;

	private Pane overlay;
	public int towerHealth = 30; // when enemy attacks to tower's health
	public static int maxTowerHealth = 30; // when enemy attacks to tower's health
	public final Rectangle healthBar;
	private boolean placed = false;

	
	public SingleShotTower(double x, double y, Pane gameOverlay) {
		super(x, y, 100, 50, Color.BLUE);
		this.damage = 10;
		this.overlay = gameOverlay;
		Image towerImage = new Image("/assets/towers/singleshottower.png");
		image = new ImageView(towerImage);
		image.setFitWidth(40);
		image.setFitHeight(40);
		image.setLayoutX(x - image.getFitWidth() / 2);
		image.setLayoutY(y - image.getFitHeight() / 2);
		image.setPickOnBounds(true);
		this.body = image;
		healthBar = new Rectangle(Enemy.TILE_SIZE, 5);
		healthBar.setFill(Color.GREEN);
		healthBar.layoutXProperty().bind(
				image.layoutXProperty().add((image.getFitWidth() - healthBar.getWidth()) / 2)
		);
		healthBar.layoutYProperty().bind(
				image.layoutYProperty().subtract(healthBar.getHeight() + 2)
		);
	}
		

		

	

	public Enemy nearestEnemy(List<Enemy> enemies) {
		Enemy closest = null;
		double minDistance = Double.MAX_VALUE;
		for (Enemy e : enemies) {
			if (isRange(e)) {

				double enemyDistance = Math.sqrt((e.getX() - x) * (e.getX() - x) + (e.getY() - y) * (e.getY() - y));
				if (enemyDistance <= minDistance) {
					minDistance = enemyDistance;
					closest = e;
				}
			}
		}
		return closest;
	}

	public void update(List<Enemy> enemies) {

		long instantTime = System.currentTimeMillis();
		if (!placed) 
			return;
		if (instantTime - lastShotTime >= shootInterval) {
			Enemy closest = nearestEnemy(enemies);
			if (closest != null && isRange(closest)) {

				Bullet b = new Bullet(x, y, closest, 10);

				Game.addBullet(b);
				/*
				 * if (Bullet.shootClip != null) { Bullet.shootClip.stop();
				 * Bullet.shootClip.setFramePosition(0); Bullet.shootClip.start(); }
				 */
				lastShotTime = instantTime;
			}
		}
	}
	
	@Override
	public void damage(int damageValue) {
		// decrease the healthbar displayd of the tower
		this.towerHealth -= damageValue;
		double percent = (double) this.towerHealth / maxTowerHealth;
		healthBar.setWidth(Enemy.TILE_SIZE * percent);

		if (percent < 0.3) {
			healthBar.setFill(Color.RED);
		} else if (percent < 0.6) {
			healthBar.setFill(Color.ORANGE);
		}

		if (this.towerHealth <= 0) {
			this.die();
		}
	}

	protected void die() {
		FadeTransition fadeSprite = new FadeTransition(Duration.millis(300), image);
		fadeSprite.setFromValue(1.0);
		fadeSprite.setToValue(0.0);

		FadeTransition fadeBar = new FadeTransition(Duration.millis(300), healthBar);
		fadeBar.setFromValue(1.0);
		fadeBar.setToValue(0.0);

		createExplosionEffect();
		Main.increaseMoney(10);

		ParallelTransition deathAnim = new ParallelTransition(fadeSprite, fadeBar);
		deathAnim.setOnFinished(e -> Game.removeTower(this));
		deathAnim.play();
	}


	/**
	 * Simple particle explosion
	 */
	private void createExplosionEffect() {
		double cx = image.getLayoutX() + image.getFitWidth() / 2;
		double cy = image.getLayoutY() + image.getFitHeight() / 2;

		for (int i = 0; i < 20; i++) {
			Circle particle = new Circle(3, Color.ORANGE);
			particle.setCenterX(cx);
			particle.setCenterY(cy);
			particle.setEffect(new BoxBlur(1, 1, 1));

			overlay.getChildren().add(particle);

			// random direction
			double angle = Math.random() * 2 * Math.PI;
			double dist = 30 + Math.random() * 20;
			double tx = cx + Math.cos(angle) * dist;
			double ty = cy + Math.sin(angle) * dist;

			TranslateTransition move = new TranslateTransition(Duration.millis(400), particle);
			move.setToX(tx - cx);
			move.setToY(ty - cy);

			FadeTransition fade = new FadeTransition(Duration.millis(400), particle);
			fade.setFromValue(1.0);
			fade.setToValue(0.0);

			ParallelTransition pt = new ParallelTransition(move, fade);
			pt.setOnFinished(e -> overlay.getChildren().remove(particle));
			pt.play();
		}
	}



	public Node getHealthBar() {
		return healthBar;
	}
	
	public void setPlaced(boolean placed) {
	    this.placed = placed;
	}
	
	public boolean isPlaced() {
	    return placed;
	}


}