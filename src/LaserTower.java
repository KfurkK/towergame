import java.util.ArrayList;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.HashMap;
import javafx.scene.shape.Line;
import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class LaserTower extends Tower {
	double damagePerSecond = 1.0;
	Map<Enemy, Double> targetTimers = new HashMap<>();
	public Line laserBeam = new Line();
	private List<Line> laserBeams = new ArrayList<>();
	
	public int towerHealth = 30; // when enemy attacks to tower's health
	public static int maxTowerHealth = 30; // when enemy attacks to tower's health

	private Pane overlay;

	public final Rectangle healthBar;
	public ImageView imageView;



	public LaserTower(double x, double y, Pane gameOverlay) {
		super(x, y, 100, 120, Color.ORANGERED); // 120$
		this.overlay = gameOverlay;

	

		Image img = new Image("assets/towers/lasertower.png");
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

	@Override
	public void update(List<Enemy> enemies) {
		long instanceTime = System.currentTimeMillis();

		for (Line beam : laserBeams) {

			Game.gameOverlay.getChildren().remove(beam);
		}
		laserBeams.clear();

		for (Enemy e : enemies) {
			if (isRange(e) && e.isAlive()) {

				targetTimers.putIfAbsent(e, instanceTime * 1.0);

				double lastHitTime = targetTimers.get(e);
				double elapsedSeconds = (instanceTime - lastHitTime) / 1000.0;

				// Zaman geçtiyse hasar ver
				if (elapsedSeconds >= 0.1) {
					e.damage(damagePerSecond * elapsedSeconds);
					targetTimers.put(e, instanceTime * 1.0);
				}
				Line beam = new Line(x, y, e.getX(), e.getY());
				beam.setStroke(Color.ORANGE);
				beam.setStrokeWidth(2);
				beam.setOpacity(0.7);

				Game.gameOverlay.getChildren().add(beam);
				laserBeams.add(beam);

			} else {
				laserBeam.setVisible(false);
				targetTimers.remove(e);
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
	
	

	@Override
	public void remove() {
		super.remove();

		for (Line beam : laserBeams) {
			Game.gameOverlay.getChildren().remove(beam);
		}
		laserBeams.clear();
		Game.forceClearAllLaserBeams();

		targetTimers.clear();
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
	}

	/**
	 * Simple particle explosion
	 */
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

	public Node getHealthBar() {
		return healthBar;
	}

}