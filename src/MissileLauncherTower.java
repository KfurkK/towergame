import java.util.ArrayList;
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

public class MissileLauncherTower extends Tower {
	public long lastShotTime = 0;
	public long shootInterval = 1500; // 1,5 saniye
	public int missileDamage = 100;
	public double effectRadius = 50;

	public ImageView imageView;

	private Pane overlay;
	public int towerHealth = 30; // when enemy attacks to tower's health
	public static int maxTowerHealth = 30; // when enemy attacks to tower's health

	public final Rectangle healthBar;
	private boolean placed = false;


	public MissileLauncherTower(double x, double y, Pane gameOverlay) {
		super(x, y, 100, 200, Color.ORANGE); // 200$
		this.overlay = gameOverlay;
		Image img = new Image("/assets/towers/missilelaunchtower.png");
		imageView = new ImageView(img);
		imageView.setFitWidth(40);
		imageView.setFitHeight(40);
		imageView.setLayoutX(x - 20);
		imageView.setLayoutY(y - 20);
		imageView.setPickOnBounds(true);

		this.body = imageView;
		
		healthBar = new Rectangle(Enemy.TILE_SIZE, 5);
		healthBar.setFill(Color.GREEN);
		healthBar.layoutXProperty().bind(
				imageView.layoutXProperty().add((imageView.getFitWidth() - healthBar.getWidth()) / 2)
		);
		healthBar.layoutYProperty().bind(
				imageView.layoutYProperty().subtract(healthBar.getHeight() + 2)
		);
	}
	

	public void update(List<Enemy> enemies) {
		long instanceTime = System.currentTimeMillis();
		if (!placed) 
			return;
		if (instanceTime - lastShotTime >= shootInterval) {
			Enemy closest = nearestEnemy(enemies);
			if (closest != null && isRange(closest)) {
				Missile missile = new Missile(x, y, closest, missileDamage, effectRadius);
				Game.addMissile(missile);
				lastShotTime = instanceTime;
			}
		}
	}
	
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
		FadeTransition fadeSprite = new FadeTransition(Duration.millis(300), imageView);
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

		// set damaged tower image
		Image damagedTower = new Image("/assets/towers/missile_damaged.png");
		imageView = new ImageView(damagedTower);
		imageView.setFitWidth(40);
		imageView.setFitHeight(40);
		imageView.setLayoutX(x - imageView.getFitWidth() / 2);
		imageView.setLayoutY(y - imageView.getFitHeight() / 2);
		overlay.getChildren().add(imageView);
	}
	
	private void createExplosionEffect() {
		double cx = imageView.getLayoutX() + imageView.getFitWidth() / 2;
		double cy = imageView.getLayoutY() + imageView.getFitHeight() / 2;

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

	public Enemy nearestEnemy(List<Enemy> enemies) {
		Enemy closest = null;
		double minDistance = Double.MAX_VALUE;
		for (Enemy e : enemies) {
			if (isRange(e) && e.isAlive()) {
				double enemyDistance = Math.sqrt((e.getX() - x) * (e.getX() - x) + (e.getY() - y) * (e.getY() - y));
				if (enemyDistance <= minDistance) {
					minDistance = enemyDistance;
					closest = e;
				}
			}
		}
		return closest;
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