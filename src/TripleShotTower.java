import java.util.ArrayList;
import java.util.List;

public class TripleShotTower extends Tower{
	public long lastShotTime = 0;
	public long shootInterval = 1200; 
	public int damage = 10;
	
	public TripleShotTower(double x , double y) {
		super(x,y,100,150); // 150$
	}
	
	 @Override
	    public void update(List<Enemy> enemies) {
	        long instanceTime = System.currentTimeMillis();
	        if (instanceTime - lastShotTime >= shootInterval) {
	            List<Enemy> targets = nearestEnemies(enemies, 3);
	            for (Enemy e : targets) {
	                if (e.isAlive() && isRange(e)) {
	                    Bullet b = new Bullet(x, y, e, damage); // Bullet sınıfı hasarı da almalı
	                    Game.addBullet(b);
	                }
	            }
	            lastShotTime = instanceTime;
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
		        double d1 = Math.sqrt((e1.getX() -x) * (e1.getX() -x) + (e1.getY()- y) * (e1.getY() - y ));
		        double d2 = Math.sqrt((e2.getX() -x) * (e2.getX() -x) + (e2.getY()- y) * (e2.getY() - y ));
		        return Double.compare(d1, d2);
		    });

		    // 3. İlk 'count' tanesini al (mesela 3)
		    List<Enemy> result = new ArrayList<>();
		    for (int i = 0; i < count && i < inRangeEnemies.size(); i++) {
		        result.add(inRangeEnemies.get(i));
		    }

		    return result;
		}
	    
	  /*  @Override
	    public void draw(GraphicsContext gc) {
	        gc.setFill(Color.DARKVIOLET);
	        gc.fillRect(x - 10, y - 10, 20, 20);
	        if (selected) {
	            gc.setStroke(Color.RED);
	            gc.strokeOval(x - range, y - range, range * 2, range * 2);
	        }
	    } */

}
