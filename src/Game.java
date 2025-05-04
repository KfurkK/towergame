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
    private static Pane gameOverlay;
    
    public static void setOverlay(Pane overlay) {
        gameOverlay = overlay;
    }
    
    public static Pane getOverlay() {
        return gameOverlay;
    }
    
    
    
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
        
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet b : bullets) {
            b.update();
            if (!b.isActive()) {
                toRemove.add(b);
            }
        }

        for (Bullet b : toRemove) {
            bullets.remove(b);
            gameOverlay.getChildren().remove(b.getNode());
        }


        List<Missile> toRemoveMissiles = new ArrayList<>();
        for (Missile m : missiles) {
            m.update(enemies);
            if (!m.isActive()) {
                toRemoveMissiles.add(m);
            }
        }
        for (Missile m : toRemoveMissiles) {
            missiles.remove(m);
            gameOverlay.getChildren().remove(m.getNode());
        }

      
    }
    
    public static void addTower(Tower t) {
        towers.add(t);
    }
    
    public static void removeTower(Tower t) {
        towers.remove(t);
    }

    public static void addEnemy(Enemy e) {
        enemies.add(e);
    }

    public static void addBullet(Bullet b) {
        bullets.add(b);
        root.getChildren().add(b.getNode());
    }
    
    public static void removeBullet(Bullet b) {
        bullets.remove(b); // logic'ten sil
        root.getChildren().remove(b.getNode()); 
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
