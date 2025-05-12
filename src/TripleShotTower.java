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

public class TripleShotTower extends Tower {
	public long lastShotTime = 0;
	public long shootInterval = 300;
	public int damage = 10;
	public ImageView imageView;

	public int towerHealth = 30; // when enemy attacks to tower's health
	public static int maxTowerHealth = 30; // when enemy attacks to tower's health

	private Pane overlay;

	public final Rectangle healthBar;



	public TripleShotTower(double x, double y, Pane gameOverlay) {
		super(x, y, 100, 150, Color.DEEPSKYBLUE);// 150$
		this.overlay = gameOverlay;
	

		Image img = new Image("assets/towers/tripleshottower.png");
		imageView = new ImageView(img);
		imageView.setFitWidth(40);
		imageView.setFitHeight(40);
		imageView.setLayoutX(x - 20);
		imageView.setLayoutY(y - 20);
		imageView.setPickOnBounds(true);

		this.body = imageView;
		healthBar = new Rectangle(Enemy.CELL_SIZE, 5);
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
		if (instanceTime - lastShotTime >= shootInterval) {
			List<Enemy> targets = nearestEnemies(enemies, 3);
			for (Enemy e : targets) {
				if (e != null && e.isAlive() && isRange(e)) {

					Bullet b = new Bullet(x, y, e, damage);

					Game.addBullet(b);
				}
			}
			lastShotTime = instanceTime;
		}
	}
	
	@Override
	public void damage(int damageValue) {
		// decrease the healthbar displayd of the tower
		this.towerHealth -= damageValue;
		double percent = (double) this.towerHealth / maxTowerHealth;
		healthBar.setWidth(Enemy.CELL_SIZE * percent);

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

	public List<Enemy> nearestEnemies(List<Enemy> enemies, int count) {
		List<Enemy> inRangeEnemies = new ArrayList<>();

		// 1. Menzildeki ve canlı düşmanları bul
		for (Enemy e : enemies) {
			if (isRange(e) && e.isAlive()) {
				inRangeEnemies.add(e);
			}
		}

		// 2. Yakınlığa göre sırala (merkezden olan uzaklığa göre)
		inRangeEnemies.sort((e1, e2) -> {
			double d1 = Math.sqrt((e1.getX() - x) * (e1.getX() - x) + (e1.getY() - y) * (e1.getY() - y));
			double d2 = Math.sqrt((e2.getX() - x) * (e2.getX() - x) + (e2.getY() - y) * (e2.getY() - y));
			return Double.compare(d1, d2);
		});

		// 3. İlk 'count' tanesini al (mesela 3)
		List<Enemy> result = new ArrayList<>();
		for (int i = 0; i < count && i < inRangeEnemies.size(); i++) {
			result.add(inRangeEnemies.get(i));

		}

		return result;
	}
	
	public Node getHealthBar() {
		return healthBar;
	}

}