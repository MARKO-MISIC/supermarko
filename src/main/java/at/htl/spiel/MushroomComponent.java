package at.htl.spiel;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import java.util.Random;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MushroomComponent extends Component {

    private static final int WALK_SPEED = 1;
    private static final double GRAVITY = 0.3;

    private double velocityY = 0;
    private int direction;
    private double directionTimer = 0;
    private double nextDirectionChange;
    private boolean dead = false;

    private Texture texture;
    private double animTimer = 0;
    private int currentFrame = 0;
    private final int mushroomType;

    // Pixelgenau aus dem Sheet analysiert:
    // Pilze sitzen in Y=90, Höhe=18px, Breite=18px pro Frame
    // Frame 0+1 = rot, Frame 2+3 = grün, Frame 4+5 = lila
    private static final double SHEET_Y = 90;
    private static final double FRAME_W = 18;
    private static final double FRAME_H = 18;
    private static final double[][] FRAME_X = {
            {0,  18},  // rot
            {36, 54},  // grün
            {72, 90},  // lila
    };

    // Spawn-Grenzen: Pilze dürfen nicht über diese X-Werte hinauswandern
    private double minX = 50;
    private double maxX = 2500;

    public MushroomComponent(int mushroomType) {
        this.mushroomType = mushroomType;
        this.direction = new Random().nextBoolean() ? 1 : -1;
        this.nextDirectionChange = 2.0 + new Random().nextDouble() * 2.0;
    }

    /** Optionale Spawn-Grenzen setzen (damit Pilze nicht von der Map laufen) */
    public void setBounds(double minX, double maxX) {
        this.minX = minX;
        this.maxX = maxX;
    }

    @Override
    public void onAdded() {
        texture = texture("../levels/mario_and_items.png");
        texture.setViewport(new Rectangle2D(
                FRAME_X[mushroomType][0], SHEET_Y, FRAME_W, FRAME_H));
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) return;

        // Animation: Frame wechseln alle 0.35 Sekunden
        animTimer += tpf;
        if (animTimer >= 0.35) {
            animTimer = 0;
            currentFrame = 1 - currentFrame;
            texture.setViewport(new Rectangle2D(
                    FRAME_X[mushroomType][currentFrame], SHEET_Y, FRAME_W, FRAME_H));
        }

        // Schwerkraft
        velocityY += GRAVITY;
        entity.translateY(velocityY);

        // Boden-Kollision
        for (var g : getGameWorld().getEntitiesByType(MarioLevel.EntityType.GROUND)) {
            if (entity.isColliding(g)) {
                double pilzUnten = entity.getY() + entity.getHeight();
                double bodenOben = g.getY();
                if (velocityY >= 0 && pilzUnten <= bodenOben + 16) {
                    entity.setY(bodenOben - entity.getHeight());
                    velocityY = 0;
                    break;
                }
            }
        }

        // Horizontale Bewegung
        entity.translateX(direction * WALK_SPEED);

        // Map-Grenzen einhalten → umdrehen statt runterfallen
        if (entity.getX() <= minX) {
            entity.setX(minX);
            direction = 1;
        } else if (entity.getX() >= maxX) {
            entity.setX(maxX);
            direction = -1;
        }

        entity.setScaleX(direction == 1 ? 1 : -1);

        // Wand-Kollision → umdrehen
        for (var g : getGameWorld().getEntitiesByType(MarioLevel.EntityType.GROUND)) {
            if (entity.isColliding(g)) {
                double pilzUnten = entity.getY() + entity.getHeight();
                double bodenOben = g.getY();
                if (pilzUnten > bodenOben + 5 && entity.getY() < g.getY() + g.getHeight() - 5) {
                    direction *= -1;
                    entity.translateX(direction * WALK_SPEED * 4);
                    break;
                }
            }
        }

        // Zufällige Richtungsänderung
        directionTimer += tpf;
        if (directionTimer >= nextDirectionChange) {
            directionTimer = 0;
            nextDirectionChange = 1.5 + new Random().nextDouble() * 2.5;
            if (new Random().nextBoolean()) direction *= -1;
        }

        // Von der Map gefallen → entfernen (nicht Game Over)
        if (entity.getY() > 800) {
            entity.removeFromWorld();
        }
    }

    public void die() {
        if (dead) return;
        dead = true;
        entity.setScaleY(0.25);
        entity.translateY(entity.getHeight() * 0.75);
        runOnce(entity::removeFromWorld, Duration.seconds(0.4));
    }

    public boolean isDead() { return dead; }
}