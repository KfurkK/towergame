import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Missile {
	public double x, y;
	public Enemy target;
	public double speed = 2.0;
	public int damage = 30;
	public double effectRadius;
	public boolean active = true;
	public ImageView shape;
	private static final Image MISSILE_IMG = new Image(
			Missile.class.getResource("/assets/bullets/missile.png").toExternalForm());

	public Missile(double x, double y, Enemy target, int damage, double effectRadius) {
		this.x = x;
		this.y = y;
		this.target = target;
		this.damage = damage;
		this.effectRadius = effectRadius;
		shape = new ImageView(MISSILE_IMG);
		double size = 20;
		shape.setFitWidth(size);
		shape.setFitHeight(size);
		shape.setPreserveRatio(true);
		shape.setLayoutX(x - size / 2);
		shape.setLayoutY(y - size / 2);

	}

	public boolean isActive() {
		return active;
	}

	public void update(List<Enemy> enemies) {

		if (!active)
			return;

		if (target == null || !target.isAlive()) {
			active = false;
			shape.setVisible(false);

			Game.gameOverlay.getChildren().remove(shape);
			return;
		}

		double dx = target.getX() - x;
		double dy = target.getY() - y;
		double enemyDistance = Math.sqrt((dx * dx) + (dy * dy));

		double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
		shape.setRotate(angleDeg);

		// Hedefe ulaştıysa → patla
		if (enemyDistance < 5) {
			explode(enemies);
			active = false;
			shape.setVisible(false);
			Game.gameOverlay.getChildren().remove(shape);

			return;
		}

		x += dx / enemyDistance * speed;
		y += dy / enemyDistance * speed;
		double w = shape.getFitWidth();
		double h = shape.getFitHeight();
		shape.setLayoutX(x - w / 2);
		shape.setLayoutY(y - h / 2);
	}

	private void explode(List<Enemy> enemies) {
		List<Enemy> toDamage = new ArrayList<>();
		for (Enemy e : enemies) {
			if (e.isAlive()) {
				double dist = Math.sqrt((e.getX() - x) * (e.getX() - x) + (e.getY() - y) * (e.getY() - y));
				if (dist <= effectRadius) {
					toDamage.add(e);
				}
			}
		}
		for (Enemy e : toDamage) {
			e.damage(damage);
		}


	}

	public Node getNode() {
		return shape;
	}

}