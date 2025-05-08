import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Missile {
    public double x, y;
    public Enemy target;
    public double speed = 2.0;
    public int damage = 30;
    public double effectRadius;
    public boolean active = true;
    public Circle shape;

    public Missile(double x, double y, Enemy target, int damage, double effectRadius) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        this.effectRadius = effectRadius;
        shape = new Circle(8, Color.ORANGE);
        shape.setStroke(Color.RED);
        shape.setStrokeWidth(2);
        shape.setLayoutX(x);
        shape.setLayoutY(y);


    }

    public boolean isActive() {
        return active;
    }


    public void update(List<Enemy> enemies) {
        if (!active || target == null || !target.isAlive()) return;

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double enemyDistance = Math.sqrt((dx * dx) +  (dy * dy));

        // Hedefe ulaştıysa → patla
        if (enemyDistance < 5) {
            explode(enemies);
            active = false;
            shape.setVisible(false);

            return;
        }


        x += dx / enemyDistance * speed;
        y += dy / enemyDistance * speed;
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }

    private void explode(List<Enemy> enemies) {
        List<Enemy> toDamage = new ArrayList<>();

        // Önce etki alanındaki düşmanları topla
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                double dist = Math.sqrt((e.getX() - x) * (e.getX() - x) + (e.getY() - y) * (e.getY() - y));
                if (dist <= effectRadius) {
                    toDamage.add(e);  // Listeye ekle ama şimdilik hasar verme
                    //ConcurrentModificationException  çözülmesi için
                    
                }
            }
        }

        // Şimdi hasar ver (silme burada olursa sorun çıkmaz)
        for (Enemy e : toDamage) {
            e.damage(damage);
        }
    }


    
    public Node getNode() {
        return shape;
    }




}