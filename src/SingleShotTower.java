import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SingleShotTower extends Tower{
	private long lastShotTime = 0;
    private long shootInterval = 1000;
    
    public SingleShotTower(double x , double y ) {
    	super(x,y,100, 50, Color.BLUE); //100 range , 50$
    	
    }
    public Enemy nearestEnemy(List<Enemy> enemies) {
    	Enemy closest = null;
    	double minDistance = Double.MAX_VALUE;
    	for ( Enemy e : enemies) {
    		if (isRange(e)) {
    			double enemyDistance = Math.sqrt(e.getX()-x*e.getX()-x + e.getY()-y*e.getY()-y);
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
        if (instantTime - lastShotTime >= shootInterval) {
            Enemy closest = nearestEnemy(enemies);
            if (closest != null && isRange(closest)) {
                
                Bullet b = new Bullet(x, y, closest, 0); 
                Game.addBullet(b); 
                lastShotTime = instantTime;
            }
        }
    }
    
	
	

}
