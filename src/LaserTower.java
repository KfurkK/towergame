import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LaserTower extends Tower{
	double damagePerSecond = 10.0;
	Map<Enemy, Double> targetTimers = new HashMap<>();
	 
	public LaserTower(double x, double y) {
		super(x,y,100,120);  // 120$
	}
	
	  @Override
	   public void update(List<Enemy> enemies) {
	        long instanceTime = System.currentTimeMillis();

	        for (Enemy e : enemies) {
	            if (isRange(e) && e.isAlive()) {
	                
	                targetTimers.putIfAbsent(e, instanceTime * 1.0);

	                double lastHitTime = targetTimers.get(e);
	                double elapsedSeconds = (instanceTime - lastHitTime) / 1000.0;

	                // Zaman geçtiyse hasar ver
	                if (elapsedSeconds >= 0.1) {
	                    e.damage(damagePerSecond * elapsedSeconds);
	                    targetTimers.put(e, instanceTime * 1.0);
	                }
	            } else {
	                // Menzil dışına çıktıysa zamanlayıcıyı sıfırla
	                targetTimers.remove(e);
	            }
	        }
	    }

	  @Override
	    public void draw(GraphicsContext gc) {
	        // Kuleyi çiz
	        gc.setFill(Color.RED);
	        gc.fillRect(x - 10, y - 10, 20, 20);

	        // Menzil çizimi (seçiliyse)
	        if (selected) {
	            gc.setStroke(Color.RED);
	            gc.strokeOval(x - range, y - range, range * 2, range * 2);
	        }

	        // Aktif lazer çizgileri
	        gc.setStroke(Color.ORANGERED);
	        for (Enemy e : targetTimers.keySet()) {
	            if (e.isAlive() && isRange(e)) {
	                gc.strokeLine(x, y, e.getX(), e.getY());
	            }
	        }
	    }
}
