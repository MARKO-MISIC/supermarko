package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
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

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("HTL Mario - F11 for Fullscreen");

        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);

        // Optional: Wenn du das ESC-Menü komplett ausschalten willst,
        // entkommentiere die nächste Zeile:
        // settings.setSceneFactory(new SceneFactory());
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.D, () -> {
            if (player == null) return;
            player.translateX(WALK_SPEED);
            if (isCollidingWithWall()) player.translateX(-WALK_SPEED);
        });

        onKey(KeyCode.A, () -> {
            if (player == null) return;
            player.translateX(-WALK_SPEED);
            if (isCollidingWithWall()) player.translateX(WALK_SPEED);
        });

        onKeyDown(KeyCode.W, () -> {
            if (player != null && isOnGround()) {
                velocityY = JUMP_FORCE;
            }
        });

        // --- ÄNDERUNG: Nutze F11 statt ESC ---
        onKeyDown(KeyCode.F11, () -> {
            var stage = getPrimaryStage();
            stage.setFullScreen(!stage.isFullScreen());
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);

        try {
            setLevelFromMap("../levels/level1.tmx");
            player = getGameWorld().spawn("player");

            getGameScene().getViewport().setZoom(2.0);
            getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
            getGameScene().getViewport().setBounds(0, 0, 5000, 720);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player == null) return;

        velocityY += GRAVITY;
        player.translateY(velocityY);

        if (isOnGround()) {
            velocityY = 0;
        }
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