import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Giant extends Enemy {
    private final Pane gamePane;
    private long lastAttackTime = 0;
    private Timeline attackTimer;
    private Image img;



    public Giant(int health, Pane gamePane) {

        super(health, gamePane);
        this.gamePane = gamePane;

        // Load archer-specific image
        this.img = new Image(getClass().getResource("/assets/giant.png").toExternalForm());
        this.getView().setImage(this.img);

        // Start attacking towers periodically

    }

    @Override
    public void die() {
        super.die();
    }

    @Override
    protected void removeFromGame() {
        super.removeFromGame();
    }

    @Override
    public int getPointValue() {
        return 30;
    }

}
