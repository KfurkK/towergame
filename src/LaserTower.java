import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.shape.Line;
import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class LaserTower extends Tower{
	double damagePerSecond = 10.0;
	Map<Enemy, Double> targetTimers = new HashMap<>();
	public Line laserBeam = new Line();
	private List<Line> laserBeams = new ArrayList<>();
	 
	public LaserTower(double x, double y) {
		super(x,y,100,120, Color.RED);  // 120$
		
		Image img = new Image("file:src/assests/towers/lasertower.png");
        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        imageView.setLayoutX(x - 20);
        imageView.setLayoutY(y - 20);
        imageView.setPickOnBounds(true);
        
        

        this.body = imageView;
	}
	
	  @Override
	   public void update(List<Enemy> enemies) {
	        long instanceTime = System.currentTimeMillis();
	        
	        for (Line beam : laserBeams) {
	            beam.setVisible(false);
	            Game.gameOverlay.getChildren().remove(beam);
	        }
	        laserBeams.clear();

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
	                Line beam = new Line(x, y, e.getX(), e.getY());
	                beam.setStroke(Color.RED);
	                beam.setStrokeWidth(2);
	                beam.setOpacity(0.7);

	                Game.gameOverlay.getChildren().add(beam);
	                laserBeams.add(beam);
	                
	            } else {
	            	laserBeam.setVisible(false);
	                targetTimers.remove(e);
	            }
	        }
	    }

	  
}
