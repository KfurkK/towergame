import javafx.scene.media.AudioClip;
import javax.sound.sampled.*;

import javafx.scene.Node;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet {
	public double x, y;
	public Enemy target;
    public double speed = 5.0; // To be tuned if needed
	public int damage = 10;
	public boolean active = true;
	public ImageView shape;
	private static final Image BULLET_IMG = new Image(
			Bullet.class.getResource("/assets/bullets/cannonball.png").toExternalForm());
	/*
	 * public static Clip shootClip; static { try (AudioInputStream ais =
	 * AudioSystem.getAudioInputStream(
	 * Bullet.class.getResource("/assets/sounds/Cannonball.wav"))) { shootClip =
	 * AudioSystem.getClip(); shootClip.open(ais); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */

	public Bullet(double x, double y, Enemy target, int damage) {
		this.x = x;
		this.y = y;
		this.target = target;
		this.damage = damage;
		shape = new ImageView(BULLET_IMG);
		double diameter = 20;

		shape.setFitWidth(diameter);
		shape.setFitHeight(diameter);
		shape.setPreserveRatio(true);

		shape.setLayoutX(x - diameter / 2);
		shape.setLayoutY(y - diameter / 2);

	}

	public boolean isActive() {
		return true;
	}

	public void update() {
		if (!active || target == null || !target.isAlive()) {
			shape.setVisible(false);
			return;
		}
		double dx = target.getX() - x;
		double dy = target.getY() - y;
		double enemyDistance = Math.sqrt(dx * dx + dy * dy);

		if (!active || !target.isAlive()) {
			active = false;
			shape.setVisible(false);
			return;
		}

		if (enemyDistance < 5) {

			target.damage(damage);
			active = false;
			shape.setVisible(false);
			Game.gameOverlay.getChildren().remove(shape);
			return;
		}
		x += dx / enemyDistance * speed;
		y += dy / enemyDistance * speed;
		shape.setLayoutX(x);
		shape.setLayoutY(y);
	}

	public Node getNode() {
		return shape;
	}

}