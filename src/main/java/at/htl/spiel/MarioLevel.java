package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.List;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    public enum EntityType {
        PLAYER, GROUND, PIPE
    }

    private Entity player;
    private int currentLevel = 1;

    // Physik-Werte
    private double velocityY = 0;
    private final double GRAVITY = 0.1;
    private final double JUMP_FORCE = -4;
    private final int WALK_SPEED = 2;

    // Kamera-Einstellungen
    private double cameraX = 0;
    private final double scrollSpeed = 0.8;
    private boolean gameStarted = false;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Super Marko");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initInput() {
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
            protected void onActionEnd() {
                if (player != null) player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D);

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

        onKeyDown(KeyCode.W, () -> {
            if (player != null && isOnGround()) {
                gameStarted = true;
                velocityY = JUMP_FORCE;
            }
        });

        // Universeller Level-Wechsel mit S
        onKeyDown(KeyCode.S, () -> {
            if (player != null && isCollidingWithPipe()) {
                handleLevelTransition();
            }
        });
    }

    private void handleLevelTransition() {
        if (currentLevel == 1) {
            loadNextLevel(2, "Betrete Level 2...");
        } else if (currentLevel == 2) {
            loadNextLevel(3, "Vorsicht... es wird dunkel in Level 3!");
        } else if (currentLevel == 3) {
            gameStarted = false;
            showMessage("Herzlichen Glückwunsch! Du hast das Spiel gewonnen!", () -> {
                getGameController().startNewGame();
            });
        }
    }

    private void loadNextLevel(int levelNum, String message) {
        gameStarted = false;
        currentLevel = levelNum;

        showMessage(message, () -> {
            try {
                getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

                // Pfad zu den Maps
                setLevelFromMap("../levels/level" + levelNum + ".tmx");

                player = getGameWorld().spawn("player");

                // Hintergrund für Level 3 gruselig dunkel setzen
                if (levelNum == 3) {
                    getGameScene().setBackgroundColor(Color.rgb(20, 20, 40));
                } else {
                    getGameScene().setBackgroundColor(Color.LIGHTBLUE);
                }

                // In Level 2 & 3 wechselt Mario das Aussehen
                if (levelNum >= 2) {
                    player.getComponent(PlayerComponent.class).changeDimension();
                }

                // WICHTIG: Kamera beim Levelwechsel exakt nullen
                cameraX = 0;
                getGameScene().getViewport().setX(0);

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Fehler beim Laden von Level " + levelNum);
            }
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());

        // CRITICAL FIX: Alle Variablen komplett zurücksetzen bei neuem Spielstart!
        currentLevel = 1;
        cameraX = 0;
        velocityY = 0;
        gameStarted = false;

        // Viewport komplett zurücksetzen
        getGameScene().getViewport().setZoom(2.0);
        getGameScene().getViewport().setX(0);
        getGameScene().getViewport().setY(0);
        getGameScene().getViewport().setBounds(0, 0, 10000, 720);

        try {
            getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
            setLevelFromMap("../levels/level1.tmx");
            player = getGameWorld().spawn("player");
            getGameScene().setBackgroundColor(Color.LIGHTBLUE);

            // Startnachricht asynchron anzeigen
            runOnce(() -> {
                showMessage("Willkommen bei Super Marko!");
            }, Duration.seconds(0.1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player == null) return;

        if (gameStarted) {
            // Kamera-Scrolling erhöhen
            cameraX += scrollSpeed;
            double threshold = getAppWidth() / 4.0;
            if (player.getX() > cameraX + threshold) {
                cameraX = player.getX() - threshold;
            }
            getGameScene().getViewport().setX(cameraX);

            // Links aus dem Bildschirm fallen
            if (player.getX() < cameraX) {
                gameOver();
                return; // Schleife sofort abbrechen
            }
        }

        velocityY += GRAVITY;
        player.translateY(velocityY);
        checkYCollisions();

        // Nach unten aus dem Bildschirm fallen
        if (player.getY() > 720) {
            gameOver();
        }
    }

    private void checkYCollisions() {
        List<Entity> grounds = getGameWorld().getEntitiesByType(EntityType.GROUND);
        for (Entity g : grounds) {
            if (player.isColliding(g)) {
                if (velocityY < 0) {
                    if (player.getY() < g.getY() + g.getHeight() && player.getY() > g.getY()) {
                        player.setY(g.getY() + g.getHeight());
                        velocityY = 0;
                        return;
                    }
                } else if (velocityY >= 0) {
                    if (player.getY() + player.getHeight() <= g.getY() + 15) {
                        player.setY(g.getY() - player.getHeight());
                        velocityY = 0;
                        return;
                    }
                }
            }
        }
    }

    private boolean isCollidingWithPipe() {
        return getGameWorld().getEntitiesByType(EntityType.PIPE).stream().anyMatch(p -> player.isColliding(p));
    }

    private boolean isCollidingWithWall() {
        List<Entity> grounds = getGameWorld().getEntitiesByType(EntityType.GROUND);
        for (Entity g : grounds) {
            if (player.isColliding(g)) {
                if (player.getY() + player.getHeight() > g.getY() + 5 && player.getY() < g.getY() + g.getHeight() - 5) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnGround() {
        return getGameWorld().getEntitiesByType(EntityType.GROUND).stream()
                .anyMatch(g -> player.isColliding(g) && Math.abs((player.getY() + player.getHeight()) - g.getY()) < 2);
    }

    private void gameOver() {
        if (!gameStarted && player == null) return; // Verhindert doppelten Aufruf

        gameStarted = false;
        player = null; // Entfernt die Referenz, damit onUpdate stoppt

        showMessage("Du bist gestorben", () -> {
            getGameController().startNewGame();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}