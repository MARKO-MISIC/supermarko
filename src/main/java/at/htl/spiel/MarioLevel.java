package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import java.util.List;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    public enum EntityType {
        PLAYER, GROUND
    }

    private Entity player;
    private double velocityY = 0;
    private final double GRAVITY = 0.4;
    private final double JUMP_FORCE = -10;
    private final int WALK_SPEED = 5;

    // Kamera-Einstellungen
    private double cameraX = 0;
    private double scrollSpeed = 1.5; // Mindestgeschwindigkeit der Kamera
    private boolean gameStarted = false;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("HTL Mario - Autoscroller Pro");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.D, () -> {
            if (player == null) return;
            gameStarted = true;
            player.translateX(WALK_SPEED);
            if (isCollidingWithWall()) player.translateX(-WALK_SPEED);
        });

        onKey(KeyCode.A, () -> {
            if (player == null) return;
            // Verhindert, dass Mario links aus dem Sichtfeld läuft
            if (player.getX() > getGameScene().getViewport().getX()) {
                gameStarted = true;
                player.translateX(-WALK_SPEED);
                if (isCollidingWithWall()) player.translateX(WALK_SPEED);
            }
        });

        onKeyDown(KeyCode.W, () -> {
            if (player != null && isOnGround()) {
                gameStarted = true;
                velocityY = JUMP_FORCE;
            }
        });

        onKeyDown(KeyCode.F11, () -> {
            var stage = getPrimaryStage();
            stage.setFullScreen(!stage.isFullScreen());
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);

        // Reset der Variablen für Neustart
        gameStarted = false;
        cameraX = 0;

        try {
            setLevelFromMap("../levels/level1.tmx");
            player = getGameWorld().spawn("player");

            getGameScene().getViewport().setZoom(2.0);
            getGameScene().getViewport().setX(0);
            getGameScene().getViewport().setBounds(0, 0, 10000, 720);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player == null) return;

        if (gameStarted) {
            // 1. Kamera bewegt sich mindestens mit scrollSpeed
            cameraX += scrollSpeed;

            // 2. Wenn Mario die Bildschirmmitte überschreitet, zieht er die Kamera mit.
            // (getAppWidth() / 4.0 entspricht der Mitte bei einem Zoom von 2.0)
            double threshold = getAppWidth() / 4.0;
            if (player.getX() > cameraX + threshold) {
                cameraX = player.getX() - threshold;
            }

            getGameScene().getViewport().setX(cameraX);

            // 3. Game Over Prüfung: Linker Bildschirmrand
            if (player.getX() < cameraX) {
                gameOver();
            }
        }

        // Physik-Logik
        velocityY += GRAVITY;
        player.translateY(velocityY);

        if (isOnGround()) {
            velocityY = 0;
        }
    }

    private void gameOver() {
        gameStarted = false;
        showMessage("Du wurdest von der Kamera verschluckt!", () -> {
            getGameController().startNewGame();
        });
    }

    private boolean isCollidingWithWall() {
        List<Entity> grounds = getGameWorld().getEntitiesByType(EntityType.GROUND);
        for (Entity g : grounds) {
            if (player.isColliding(g)) {
                // Kleine Toleranz, damit Mario nicht im Boden stecken bleibt
                if (player.getY() + player.getHeight() > g.getY() + 5) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnGround() {
        List<Entity> grounds = getGameWorld().getEntitiesByType(EntityType.GROUND);
        for (Entity g : grounds) {
            if (player.isColliding(g)) {
                if (player.getY() + player.getHeight() <= g.getY() + 15 && velocityY >= 0) {
                    player.setY(g.getY() - player.getHeight());
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}