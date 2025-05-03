import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;

public class MissileLauncherTower extends Tower{
	    public long lastShotTime = 0;
	    public long shootInterval = 2000; // 2 saniye
	    public int missileDamage = 30;
	    public double effectRadius = 50;
	    
	    public MissileLauncherTower(double x, double y) {
	        super(x, y, 100, 200); // 200$
	    }
	    
	    
	    public void update(List<Enemy> enemies) {
	        long instanceTime = System.currentTimeMillis();
	        if (instanceTime - lastShotTime >= shootInterval) {
	            Enemy closest = nearestEnemy(enemies);
	            if (closest != null && isRange(closest)) {
	                Missile missile = new Missile(x, y, closest, missileDamage, effectRadius);
	                Game.addMissile(missile); 
	                lastShotTime = instanceTime;
	            }
	        }
	    }
	    
	    public Enemy nearestEnemy(List<Enemy> enemies) {
	        Enemy closest = null;
	        double minDistance = Double.MAX_VALUE;
	        for (Enemy e : enemies) {
	            if (isRange(e) && e.isAlive()) {
	                double enemyDistance = Math.sqrt((e.getX() - x ) * (e.getX() - x ) + (e.getY() - y)* (e.getY() - y));
	                if (enemyDistance <= minDistance) {
	                    minDistance = enemyDistance;
	                    closest = e;
	                }
	            }
	        }
	        return closest;
	    }


		@Override
		public void draw(GraphicsContext gc) {
			// TODO Auto-generated method stub
			
		}
	  
	    
	     
	  /*  public void draw(GraphicsContext gc) {
	        gc.setFill(Color.DARKGRAY);
	        gc.fillRect(x - 10, y - 10, 20, 20);
	        if (selected) {
	            gc.setStroke(Color.RED);
	            gc.strokeOval(x - range, y - range, range * 2, range * 2);
	        }
	    }  */

}
