import java.util.List;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;

public abstract class Tower {
	public double x, y; // Kule konumu
	public double range; // Mesafe
	public double price; // kule fiyatı
   public Enemy target;
   public boolean selected;
   protected Node body;
   
   public Circle rangeCircle;
   private int currentRow = -1;
   private int currentCol = -1;
   
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
       
       rangeCircle = new Circle(range, Color.rgb(255, 0, 0, 0.2)); // yarı saydam kırmızı
       rangeCircle.setStroke(Color.RED);
       rangeCircle.setFill(Color.rgb(255, 0, 0, 0.1));
       rangeCircle.setLayoutX(x);
       rangeCircle.setLayoutY(y);
       rangeCircle.setVisible(false); 
	   }
   
   public Node getNode() {
       return body;
   }
   
   public double getPrice() {
	   return price;
   }
   
   public void setPosition(double newX, double newY) {
       this.x = newX;
       this.y = newY;
       body.setLayoutX(x - 20 );
       body.setLayoutY(y - 20 );
       
       rangeCircle.setLayoutX(x);
       rangeCircle.setLayoutY(y);
   }
   
   public Circle getRangeCircle() {
       return rangeCircle;
   }
   
   public void remove() {
	    // Varsayılan: sadece sahneden görseli siler
	    Game.gameOverlay.getChildren().remove(getNode());
	    Game.gameOverlay.getChildren().remove(rangeCircle);
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
   
   public void setGridPosition(int row, int col) {
	    this.currentRow = row;
	    this.currentCol = col;
	}

	public int[] getGridPosition() {
	    return new int[]{currentRow, currentCol};
	}
   
   
   
   public abstract void update(List<Enemy> enemies); // her karede ne yapacak?
   
   
}
