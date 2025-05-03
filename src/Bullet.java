

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet {
	public double x,y;
	public Enemy target;
	public double speed = 5.0 ; // Sonra dolduralım.
	public int damage = 10;
	public boolean active = true;

	
	
	public Bullet(double x, double y, Enemy target, int damage) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
    }
	
	public boolean isActive() {
		return true;
	}
	
	public void update() {
		double dx = target.getX() - x;
        double dy = target.getY() - y;
        double enemyDistance = Math.sqrt(dx * dx + dy * dy);
		
        if (!active || target.isAlive()) {
			return;
		}
        
        if (enemyDistance < 5) {
            target.damage(damage);
            active = false;
            return;
        }
        x += dx / enemyDistance * speed;
        y += dy / enemyDistance * speed;
        }
	
	  public void draw(GraphicsContext gc) {
	        if (!active)
	        return;

	        gc.setFill(Color.BLACK);
	        gc.fillOval(x - 3, y - 3, 6, 6); // Küçük bir daire olarak çizilir.
	    }  
}
