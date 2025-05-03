import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public abstract class Tower {
	public double x, y; // Kule konumu
	public double range; // Mesafe
	public double price; // kule fiyatı
   public Enemy target;
   public boolean selected;
   public Rectangle body;
   
   public Tower() {
	   
   }
   
   
   public Tower (double x, double y, double range, double price, Color color) { // Base Constructor
	   this.x =x;
	   this.y = y;
	   this.range = range;
	   this.price = price;
	   
	   body = new Rectangle(20, 20, color);
       body.setLayoutX(x - 10);
       body.setLayoutY(y - 10);
	   }
   
   public Rectangle getNode() {
       return body;
   }
   
   public void setPosition(double newX, double newY) {
       this.x = newX;
       this.y = newY;
       body.setLayoutX(x - 10);
       body.setLayoutY(y - 10);
   }
   
   public boolean contains(double px, double py) {
       return body.getBoundsInParent().contains(px, py);
   }
   
   public boolean isRange(Enemy e) {
	   double dx = e.getX()- x;
	   double dy = e.getY()- y;
	   
	   double enemyRange = Math.sqrt((dy*dy)+(dx*dx));
	   
	   if (enemyRange <= range)
		   return true;
	   else
		   return false;
	   
   }
   
   
   
   public abstract void update(List<Enemy> enemies); // her karede ne yapacak?
   
   
}
