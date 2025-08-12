import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class Game {
	public static List<Tower> towers = new ArrayList<>();
	public static List<Enemy> enemies = new ArrayList<>();
	public static List<Bullet> bullets = new ArrayList<>();
	public static List<Missile> missiles = new ArrayList<>();
	public static Pane root = new Pane();;
	public static Pane gameOverlay;

	public static void setOverlay(Pane overlay) {
		gameOverlay = overlay;
	}

	public static Pane getOverlay() {
		return gameOverlay;
	}

	private int money = 100;
	private int lives = 5;

    public static void update() {
        // Update towers
		for (Tower tower : towers) {
			tower.update(new ArrayList<>(Game.enemies));
		}

        // Update enemies (logic handled within Enemy movement/animations)

		List<Bullet> toRemove = new ArrayList<>();
		for (Bullet b : bullets) {
			b.update();
			if (!b.isActive()) {
				toRemove.add(b);
			}
		}

		for (Bullet b : toRemove) {
			bullets.remove(b);
			Game.gameOverlay.getChildren().remove(b.getNode());
		}

		List<Missile> toRemoveMissiles = new ArrayList<>();
		for (Missile m : missiles) {
			m.update(enemies);
			if (!m.isActive()) {
				toRemoveMissiles.add(m);
			}
		}
		for (Missile m : toRemoveMissiles) {
			missiles.remove(m);
			Game.gameOverlay.getChildren().remove(m.getNode());
		}

	}

	public static void forceClearAllLaserBeams() {
		for (Node n : new ArrayList<>(gameOverlay.getChildren())) {
			if (n instanceof Line) {
				gameOverlay.getChildren().remove(n);
			}
		}
	}

	public static void addTower(Tower t) {
		towers.add(t);
	}

	public static void removeTower(Tower t) {
		if (t != null) {
			towers.remove(t);

			Game.forceClearAllLaserBeams();

		}
	}

	public static void addEnemy(Enemy e) {
		enemies.add(e);
	}

	public static void addBullet(Bullet b) {
		bullets.add(b);
		gameOverlay.getChildren().add(b.getNode());
	}

    public static void removeBullet(Bullet b) {
        bullets.remove(b);
        gameOverlay.getChildren().remove(b.getNode());
    }

	public static void addMissile(Missile m) {
		missiles.add(m);
		gameOverlay.getChildren().add(m.getNode());
	}

    public static void removeMissile(Bullet b) {
        bullets.remove(b);
        gameOverlay.getChildren().remove(b.getNode());
    }

	public static List<Tower> getTowers() {
		return towers;
	}

	public static List<Bullet> getBullets() {
		return bullets;
	}

	public static List<Missile> getMissiles() {
		return missiles;
	}

}