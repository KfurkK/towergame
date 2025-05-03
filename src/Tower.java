
public  class Tower {
	public double x, y; // Kule konumu
	public double range; // Mesafe
	public double price; // kule fiyatı
   public Enemy target;
   public boolean selected;
   
   public Tower() {
	   
   }
   
   public Tower (double x, double y, double range, double price ) { // Base Constructor
	   this.x =x;
	   this.y = y;
	   this.range = range;
	   this.price = price;
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
   
   //public abstract void update(List<Enemy> enemies); // her karede ne yapacak?
   //public abstract void draw(GraphicsContext gc); //Kule çizimi
   
}
