package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import java.util.List;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    public enum EntityType {
        PLAYER, GROUND
    }

    private Entity player;

    // Physik-Werte (langsameres Fallen & Gehen wie gewünscht)
    private double velocityY = 0;
    private final double GRAVITY = 0.1;
    private final double JUMP_FORCE = -4;
    private final int WALK_SPEED = 2;

    // Kamera-Einstellungen (langsameres Scrollen)
    private double cameraX = 0;
    private double scrollSpeed = 0.8;
    private boolean gameStarted = false;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("HTL Mario - Autoscroller & Animation");

        // Fullscreen-Einstellungen
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initInput() {
        // Steuerung für RECHTS (D)
        getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if (player == null) return;
                gameStarted = true;
                player.getComponent(PlayerComponent.class).moveRight();
                player.translateX(WALK_SPEED);
                if (isCollidingWithWall()) player.translateX(-WALK_SPEED);
            }

            @Override
            protected void onActionBegin() {
                // Wird einmal beim Drücken aufgerufen (optional)
            }

            @Override
            protected void onActionEnd() {
                // DAS ERSETZT onKeyUp: Wird beim Loslassen aufgerufen
                if (player != null) player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D);

        // Steuerung für LINKS (A)
        getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                if (player == null) return;
                gameStarted = true;
                player.getComponent(PlayerComponent.class).moveLeft();
                if (player.getX() > getGameScene().getViewport().getX()) {
                    player.translateX(-WALK_SPEED);
                    if (isCollidingWithWall()) player.translateX(WALK_SPEED);
                }
            }

            @Override
            protected void onActionEnd() {
                if (player != null) player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A);

        // Springen (W) kann so bleiben, da es kein onKeyUp nutzt
        onKeyDown(KeyCode.W, () -> {
            if (player != null && isOnGround()) {
                gameStarted = true;
                velocityY = JUMP_FORCE;
            }
        });
    }
    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);

        gameStarted = false;
        cameraX = 0;

        try {
            // Lädt das Level aus Tiled (Pfad muss at.htl Struktur entsprechen)
            setLevelFromMap("../levels/level1.tmx");
            player = getGameWorld().spawn("player");

            // Kamera-Setup (Zoom 2.0 für Retro-Look)
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
            // 1. Konstantes Autoscrolling
            cameraX += scrollSpeed;

            // 2. Kamera zieht mit, wenn Mario schneller als die Kamera ist
            double threshold = getAppWidth() / 4.0;
            if (player.getX() > cameraX + threshold) {
                cameraX = player.getX() - threshold;
            }

            getGameScene().getViewport().setX(cameraX);

            // 3. Game Over: Mario fällt links aus dem Bild
            if (player.getX() < cameraX) {
                gameOver();
            }
        }

        // Einfache Physik: Gravitation anwenden
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