import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Missile {
	public double x, y;
	public Enemy target;
	public double speed = 2.0;
	public int damage = 30;
	public double effectRadius;
	public boolean active = true;
	
	public Missile(double x, double y, Enemy target, int damage, double effectRadius) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        this.effectRadius = effectRadius;
    }
	
	public boolean isActive() {
        return active;
    }
	
	
	public void update(List<Enemy> enemies) {
        if (!active || !target.isAlive()) return;

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double enemyDistance = Math.sqrt((dx * dx) +  (dy * dy));

        // Hedefe ulaştıysa → patla
        if (enemyDistance < 5) {
            explode(enemies);
            active = false;
            return;
        }

        
        x += dx / enemyDistance * speed;
        y += dy / enemyDistance * speed;
    }
	
	private void explode(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                double dist = Math.sqrt((e.getX() - x) * (e.getX() - x) + (e.getY() - y) * (e.getY() - y) );
                if (dist <= effectRadius) {
                    e.damage(damage);
                }
            }
        }

        // Burada animasyon başlatılabilir (20 parçacıklı patlama vs.)
        // Patlama efektini Game sınıfı tetikleyebilir
    }
	  public void draw(GraphicsContext gc) {
        if (!active) return;

        gc.setFill(Color.DARKORANGE);
        gc.fillOval(x - 6, y - 6, 12, 12); // Roket biraz büyük görünür
    } 


	
	
}
