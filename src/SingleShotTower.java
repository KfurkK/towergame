import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class SingleShotTower extends Tower{
    private long lastShotTime = 0;
    private long shootInterval = 300;
    public int damage = 10;
    public ImageView image;

    public SingleShotTower(double x , double y ) {
        super(x,y,100,50, Color.BLUE);//100 range , 50$
        this.damage = 10;

        Image towerImage = new Image("/assets/towers/singleshottower.png"); //  PNG yolu
        image = new ImageView(towerImage);
        image.setFitWidth(40);
        image.setFitHeight(40);
        image.setLayoutX(x - 20); // merkeze al
        image.setLayoutY(y - 20);

        image.setPickOnBounds(true);

        this.body = image;

    }
    public Enemy nearestEnemy(List<Enemy> enemies) {
        Enemy closest = null;
        double minDistance = Double.MAX_VALUE;
        for ( Enemy e : enemies) {
            if (isRange(e)) {

                double enemyDistance = Math.sqrt((e.getX()-x)*(e.getX()-x) + (e.getY()-y)*(e.getY()-y));
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

                Bullet b = new Bullet(x, y, closest, 10);

                Game.addBullet(b);
                if (Bullet.shootClip != null) {
                   Bullet.shootClip.stop();              
                   Bullet.shootClip.setFramePosition(0); 
                   	Bullet.shootClip.start();             
                } 
                lastShotTime = instantTime;
            }
        }
    }




}