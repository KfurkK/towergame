import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;


public class Game {
	public static List<Tower> towers = new ArrayList<>();
	public static List<Enemy> enemies = new ArrayList<>();
	public static List<Bullet> bullets = new ArrayList<>();
    public static List<Missile> missiles = new ArrayList<>();
    
    private int money = 100;
    private int lives = 5;
    
    public static void update() {
        // Kuleleri güncelle
        for (Tower tower : towers) {
            tower.update(enemies);
        }

        /* // Düşmanları güncelle
        for (Enemy e : enemies) {
            e.update();
        } */

        // Mermileri güncelle
        for (Bullet b : bullets) {
            b.update();
        }

        // Roketleri güncelle
        for (Missile m : missiles) {
            m.update(enemies);
        }

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
    
   /* public static void draw(GraphicsContext gc) {
        for (Tower tower : towers)
            tower.draw(gc);

        for (Enemy e : enemies)
            e.draw(gc);

        for (Bullet b : bullets)
            b.draw(gc);

        for (Missile m : missiles)
            m.draw(gc);
    } */
    
    

}
