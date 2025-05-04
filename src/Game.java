import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;


public class Game {
	public static List<Tower> towers = new ArrayList<>();
	public static List<Enemy> enemies = new ArrayList<>();
	public static List<Bullet> bullets = new ArrayList<>();
    public static List<Missile> missiles = new ArrayList<>();
    public static Pane root = new Pane();;
    
    
    
    private int money = 100;
    private int lives = 5;
    
    
    public static void update() {
        // Kuleleri güncelle
        for (Tower tower : towers) {
            tower.update(Game.enemies);
        }

         // Düşmanları güncelle
         /* for (Enemy e : enemies) {
            e.update();
        } */

        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isActive());

        for (Missile m : missiles) m.update(enemies);
        missiles.removeIf(m -> !m.isActive());

        // Temizlik
        bullets.removeIf(b -> !b.isActive());
        missiles.removeIf(m -> !m.isActive());
        enemies.removeIf(e -> !e.isAlive());
    }
    
    public static void addTower(Tower t) {
        towers.add(t);
    }

    public static void addEnemy(Enemy e) {
        enemies.add(e);
    }

    public static void addBullet(Bullet b) {
        bullets.add(b);
        root.getChildren().add(b.getNode());
    }

    public static void addMissile(Missile m) {
        missiles.add(m);
    }

    


    public void resetGame() {
        towers.clear();
        enemies.clear();
        bullets.clear();
        missiles.clear();
        money = 100;
        lives = 5;
    }
    
    
    
    

}
