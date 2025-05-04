

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet {
	public double x,y;
	public Enemy target;
	public double speed = 5.0 ; // Sonra dolduralım.
	public int damage = 10;
	public boolean active = true;
	public Circle shape;

	
	
	public Bullet(double x, double y, Enemy target, int damage) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        shape = new Circle(5, Color.BLACK);
        shape.setLayoutX(x);
        shape.setLayoutY(y);
        
        
    }
	
	public boolean isActive() {
		return true;
	}
	
	public void update() {
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
