package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.ui.FontType;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.List;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    public enum EntityType {
        PLAYER, GROUND, PIPE, MUSHROOM
    }

    private Entity player;
    private int currentLevel = 1;

    private double velocityY = 0;
    private final double GRAVITY = 0.3;
    private final double JUMP_FORCE = -6.5;
    private final int WALK_SPEED = 4; // Bleibt immer konstant bei 4

    private double cameraX = 0;
    private final double BASE_SCROLL_SPEED = 2.5;
    private double currentScrollSpeed = BASE_SCROLL_SPEED; // Dynamische Kamera-Geschwindigkeit
    private boolean gameStarted = false;
    private boolean isGameOver = false;

    // Stoppuhr
    private double elapsedSeconds = 0;
    private boolean timerRunning = false;
    private Text timerText;

    private MediaPlayer backgroundMusic;

    // KORRIGIERT: 3. Pilz von 1020 auf 1120 nach rechts verschoben, damit er komplett frei läuft
    private static final int[][] MUSHROOM_SPAWNS_L1 = {
            {300,  190, 0,  50,  1630},
            {600,  190, 0,  50,  1630},
            {1120, 190, 1,  50,  1630}, // Weiter nach rechts versetzt, weg von den Röhren
            {1250, 190, 2,  50,  1630},
            {1500, 190, 0,  50,  1630},
            {1800, 190, 1, 1632, 2557},
            {2100, 190, 2, 1632, 2557},
            {2350, 190, 0, 1632, 2557},
    };

    private static final int[][] MUSHROOM_SPAWNS_L2 = {
            {250,  173, 1,  10, 1600},
            {610,  173, 0,  10, 1600},
            {750,  100, 2,  10, 1600},
            {1080, 100, 1,  10, 1600},
            {1250, 173, 0,  10, 1600},
            {1450, 173, 2,  10, 1600},
    };

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Super Marko");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initUI() {
        timerText = getUIFactoryService().newText("00:00.0", Color.BLACK, FontType.GAME, 30);
        timerText.setStroke(Color.WHITE);
        timerText.setStrokeWidth(2);

        timerText.setLayoutX(getAppWidth() - 160);
        timerText.setLayoutY(45);

        addUINode(timerText);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if (player == null) return;
                if (!gameStarted) {
                    gameStarted = true;
                    timerRunning = true;
                }
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
                if (!gameStarted) {
                    gameStarted = true;
                    timerRunning = true;
                }
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
                if (!gameStarted) {
                    gameStarted = true;
                    timerRunning = true;
                }
                velocityY = JUMP_FORCE;
            }
        });

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
            loadNextLevel(3, "Vorsicht... es wird dunkel in Level 3 und die Kamera scrollt schneller!");
        } else if (currentLevel == 3) {
            gameStarted = false;
            timerRunning = false;
            stopMusic();
            String finalTime = formatTime(elapsedSeconds);
            showMessage("Herzlichen Glückwunsch! Du hast das Spiel gewonnen!\nZeit: " + finalTime, () -> {
                getGameController().startNewGame();
            });
        }
    }

    private void loadNextLevel(int levelNum, String message) {
        gameStarted = false;
        timerRunning = false;
        isGameOver = false;
        velocityY = 0;
        currentLevel = levelNum;

        // KORRIGIERT: Nur Scroll-Speed erhöht sich, Marko läuft gleich schnell (WALK_SPEED bleibt unberührt)
        if (levelNum == 3) {
            currentScrollSpeed = BASE_SCROLL_SPEED + 0.6;
        } else {
            currentScrollSpeed = BASE_SCROLL_SPEED;
        }

        showMessage(message, () -> {
            try {
                getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
                setLevelFromMap("../levels/level" + levelNum + ".tmx");
                player = getGameWorld().spawn("player");

                // KORRIGIERT: Text-Füllung weiß, Rand-Breite 0 entfernt dunkle Artefakte auf düsterem Grund
                if (levelNum == 3) {
                    getGameScene().setBackgroundColor(Color.rgb(20, 20, 40));
                    timerText.setFill(Color.WHITE);
                    timerText.setStrokeWidth(0);
                } else {
                    getGameScene().setBackgroundColor(Color.LIGHTBLUE);
                    timerText.setFill(Color.BLACK);
                    timerText.setStroke(Color.WHITE);
                    timerText.setStrokeWidth(2);
                }

                if (levelNum >= 2) {
                    player.getComponent(PlayerComponent.class).changeDimension();
                }

                if (levelNum == 1) spawnMushrooms(MUSHROOM_SPAWNS_L1);
                else if (levelNum == 2) spawnMushrooms(MUSHROOM_SPAWNS_L2);

                cameraX = 0;
                getGameScene().getViewport().setX(0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());

        currentLevel = 1;
        cameraX = 0;
        currentScrollSpeed = BASE_SCROLL_SPEED;
        velocityY = 0;
        gameStarted = false;
        isGameOver = false;
        elapsedSeconds = 0;
        timerRunning = false;

        playMusic();

        getGameScene().getViewport().setZoom(2.0);
        getGameScene().getViewport().setX(0);
        getGameScene().getViewport().setY(0);
        getGameScene().getViewport().setBounds(0, 0, 10000, 720);

        try {
            getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
            setLevelFromMap("../levels/level1.tmx");
            player = getGameWorld().spawn("player");
            getGameScene().setBackgroundColor(Color.LIGHTBLUE);

            spawnMushrooms(MUSHROOM_SPAWNS_L1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnMushrooms(int[][] spawns) {
        for (int[] sp : spawns) {
            int safeSpawnY = sp[1] - 40;

            Entity m = getGameWorld().spawn("mushroom",
                    new SpawnData(sp[0], safeSpawnY).put("mushroomType", sp[2]));
            if (sp.length >= 5) {
                m.getComponent(MushroomComponent.class).setBounds(sp[3], sp[4]);
            }
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player == null || isGameOver) return;

        if (timerRunning) {
            elapsedSeconds += tpf;
            if (timerText != null) {
                timerText.setText(formatTime(elapsedSeconds));
            }
        }

        if (gameStarted) {
            cameraX += currentScrollSpeed;
            double threshold = getAppWidth() / 4.0;
            if (player.getX() > cameraX + threshold) {
                cameraX = player.getX() - threshold;
            }
            getGameScene().getViewport().setX(cameraX);

            if (player.getX() < cameraX) {
                gameOver();
                return;
            }
        }

        velocityY += GRAVITY;
        player.translateY(velocityY);
        checkYCollisions();

        if (player == null || isGameOver) return;

        checkMushroomCollisions();

        if (player == null || isGameOver) return;

        if (player.getY() > 720) {
            gameOver();
        }
    }

    private String formatTime(double seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        int tenths = (int)((seconds * 10) % 10);
        return String.format("%02d:%02d.%d", mins, secs, tenths);
    }

    private void checkYCollisions() {
        if (player == null) return;
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

    private void checkMushroomCollisions() {
        if (player == null || isGameOver) return;
        List<Entity> mushrooms = getGameWorld().getEntitiesByType(EntityType.MUSHROOM);
        for (Entity m : mushrooms) {
            MushroomComponent mc = m.getComponent(MushroomComponent.class);
            if (mc.isDead()) continue;
            if (!player.isColliding(m)) continue;

            double marioFuss = player.getY() + player.getHeight();
            double pilzKopf  = m.getY() + m.getHeight() * 0.4;

            if (velocityY > 0 && marioFuss <= pilzKopf + 10) {
                mc.die();
                velocityY = JUMP_FORCE * 0.65;
            } else {
                gameOver();
                return;
            }
        }
    }

    private boolean isCollidingWithPipe() {
        return getGameWorld().getEntitiesByType(EntityType.PIPE).stream()
                .anyMatch(p -> player.isColliding(p));
    }

    private boolean isCollidingWithWall() {
        List<Entity> grounds = getGameWorld().getEntitiesByType(EntityType.GROUND);
        for (Entity g : grounds) {
            if (player.isColliding(g)) {
                if (player.getY() + player.getHeight() > g.getY() + 5 &&
                        player.getY() < g.getY() + g.getHeight() - 5) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnGround() {
        return getGameWorld().getEntitiesByType(EntityType.GROUND).stream()
                .anyMatch(g -> player.isColliding(g) &&
                        Math.abs((player.getY() + player.getHeight()) - g.getY()) < 2);
    }

    private void gameOver() {
        if (isGameOver) return;
        isGameOver = true;
        gameStarted = false;
        timerRunning = false;
        player = null;
        stopMusic();
        showMessage("Du bist gestorben", () -> getGameController().startNewGame());
    }

    private void playMusic() {
        try {
            if (backgroundMusic == null) {
                String musicPath = getClass().getResource("/assets/levels/MarkoMusic.mp3").toExternalForm();
                Media media = new Media(musicPath);
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(0.5);
            }
            backgroundMusic.seek(Duration.ZERO);
            backgroundMusic.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Musikdatei: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (backgroundMusic != null) backgroundMusic.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}