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
    private final int WALK_SPEED = 4;

    private double cameraX = 0;
    private final double BASE_SCROLL_SPEED = 2.5;
    private double currentScrollSpeed = BASE_SCROLL_SPEED;
    private boolean gameStarted = false;
    private boolean isGameOver = false;

    private double elapsedSeconds = 0;
    private boolean timerRunning = false;
    private Text timerText;
    private MediaPlayer backgroundMusic;

    // Pilz-Spawns für Level 1
    private static final int[][] MUSHROOM_SPAWNS_L1 = {
            {300,  190, 0,  50,  1630},
            {600,  190, 0,  50,  1630},
            {1120, 190, 1,  50,  1630},
            {1250, 190, 2,  50,  1630},
            {1500, 190, 0,  50,  1630},
            {1800, 190, 1, 1632, 2557},
            {2100, 190, 2, 1632, 2557},
            {2350, 190, 0, 1632, 2557}
    };

    // Pilz-Spawns für Level 2 (wieder hinzugefügt)
    private static final int[][] MUSHROOM_SPAWNS_L2 = {
            {250,  173, 1,  10, 1600},
            {610,  173, 0,  10, 1600},
            {750,  100, 2,  10, 1600},
            {1080, 100, 1,  10, 1600},
            {1250, 173, 0,  10, 1600},
            {1450, 173, 2,  10, 1600}
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
                startTimerIfNeeded();
                PlayerComponent pc = player.getComponent(PlayerComponent.class);
                pc.moveRight();
                double speed = pc.isPoweredUp() ? WALK_SPEED * 1.5 : WALK_SPEED;
                player.translateX(speed);
                if (isCollidingWithWall()) player.translateX(-speed);
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
                startTimerIfNeeded();
                PlayerComponent pc = player.getComponent(PlayerComponent.class);
                pc.moveLeft();
                double speed = pc.isPoweredUp() ? WALK_SPEED * 1.5 : WALK_SPEED;
                if (player.getX() > getGameScene().getViewport().getX()) {
                    player.translateX(-speed);
                    if (isCollidingWithWall()) player.translateX(speed);
                }
            }
            @Override
            protected void onActionEnd() {
                if (player != null) player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A);

        onKeyDown(KeyCode.W, () -> {
            if (player != null && isOnGround()) {
                startTimerIfNeeded();
                boolean isBoosted = player.getComponent(PlayerComponent.class).isPoweredUp();
                velocityY = isBoosted ? JUMP_FORCE * 1.3 : JUMP_FORCE;
            }
        });

        onKeyDown(KeyCode.S, () -> {
            if (player != null && isCollidingWithPipe()) handleLevelTransition();
        });
    }

    private void startTimerIfNeeded() {
        if (!gameStarted) {
            gameStarted = true;
            timerRunning = true;
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player == null || isGameOver) return;

        if (timerRunning) {
            elapsedSeconds += tpf;
            if (timerText != null) timerText.setText(formatTime(elapsedSeconds));
        }

        if (gameStarted) {
            cameraX += currentScrollSpeed;
            double threshold = getAppWidth() / 4.0;
            if (player.getX() > cameraX + threshold) cameraX = player.getX() - threshold;
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
        if (player.getY() > 720) gameOver();
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

                PlayerComponent pc = player.getComponent(PlayerComponent.class);
                pc.setPoweredUp(true);

                getGameTimer().runOnceAfter(() -> {
                    if (player != null && player.isActive()) {
                        player.getComponent(PlayerComponent.class).setPoweredUp(false);
                    }
                }, Duration.seconds(2));

            } else {
                gameOver();
                break;
            }
        }
    }

    private void handleLevelTransition() {
        if (currentLevel == 1) loadNextLevel(2, "Betrete Level 2...");
        else if (currentLevel == 2) loadNextLevel(3, "Level 3: Düster & Schnell!");
        else if (currentLevel == 3) {
            gameStarted = false; timerRunning = false; stopMusic();
            showMessage("Sieg! Zeit: " + formatTime(elapsedSeconds), () -> getGameController().startNewGame());
        }
    }

    private void loadNextLevel(int levelNum, String message) {
        gameStarted = false; timerRunning = false; isGameOver = false; velocityY = 0;
        currentLevel = levelNum;
        currentScrollSpeed = (levelNum == 3) ? BASE_SCROLL_SPEED + 0.6 : BASE_SCROLL_SPEED;

        showMessage(message, () -> {
            getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
            setLevelFromMap("../levels/level" + levelNum + ".tmx");
            player = getGameWorld().spawn("player");

            updateUIForLevel(levelNum);

            // Pilze für das neue Level spawnen
            if (levelNum == 1) spawnMushrooms(MUSHROOM_SPAWNS_L1);
            else if (levelNum == 2) spawnMushrooms(MUSHROOM_SPAWNS_L2);
            // Hier könnten noch Spawns für Level 3 hin

            if (levelNum >= 2) player.getComponent(PlayerComponent.class).changeDimension();
            cameraX = 0;
            getGameScene().getViewport().setX(0);
        });
    }

    private void updateUIForLevel(int level) {
        if (level == 3) {
            getGameScene().setBackgroundColor(Color.rgb(20, 20, 40));
            timerText.setFill(Color.WHITE); timerText.setStrokeWidth(0);
        } else {
            getGameScene().setBackgroundColor(Color.LIGHTBLUE);
            timerText.setFill(Color.BLACK); timerText.setStroke(Color.WHITE); timerText.setStrokeWidth(2);
        }
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());
        currentLevel = 1; cameraX = 0; currentScrollSpeed = BASE_SCROLL_SPEED;
        velocityY = 0; gameStarted = false; isGameOver = false; elapsedSeconds = 0; timerRunning = false;
        playMusic();
        getGameScene().getViewport().setZoom(2.0);
        getGameScene().getViewport().setBounds(0, 0, 10000, 720);
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
        setLevelFromMap("../levels/level1.tmx");
        player = getGameWorld().spawn("player");
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        spawnMushrooms(MUSHROOM_SPAWNS_L1);
    }

    private void spawnMushrooms(int[][] spawns) {
        for (int[] sp : spawns) {
            getGameWorld().spawn("mushroom", new SpawnData(sp[0], sp[1] - 40).put("mushroomType", sp[2]))
                    .getComponent(MushroomComponent.class).setBounds(sp[3], sp[4]);
        }
    }

    private String formatTime(double seconds) {
        int mins = (int)(seconds / 60); int secs = (int)(seconds % 60); int tenths = (int)((seconds * 10) % 10);
        return String.format("%02d:%02d.%d", mins, secs, tenths);
    }

    private void checkYCollisions() {
        if (player == null) return;
        for (Entity g : getGameWorld().getEntitiesByType(EntityType.GROUND)) {
            if (player.isColliding(g)) {
                if (velocityY < 0 && player.getY() < g.getY() + g.getHeight() && player.getY() > g.getY()) {
                    player.setY(g.getY() + g.getHeight());
                    velocityY = 0;
                } else if (velocityY >= 0 && player.getY() + player.getHeight() <= g.getY() + 15) {
                    player.setY(g.getY() - player.getHeight());
                    velocityY = 0;
                }
            }
        }
    }

    private boolean isCollidingWithPipe() { return getGameWorld().getEntitiesByType(EntityType.PIPE).stream().anyMatch(p -> player.isColliding(p)); }
    private boolean isCollidingWithWall() {
        for (Entity g : getGameWorld().getEntitiesByType(EntityType.GROUND)) {
            if (player.isColliding(g) && player.getY() + player.getHeight() > g.getY() + 5 && player.getY() < g.getY() + g.getHeight() - 5) return true;
        }
        return false;
    }
    private boolean isOnGround() { return getGameWorld().getEntitiesByType(EntityType.GROUND).stream().anyMatch(g -> player.isColliding(g) && Math.abs((player.getY() + player.getHeight()) - g.getY()) < 2); }

    private void gameOver() {
        if (isGameOver) return;
        isGameOver = true; gameStarted = false; timerRunning = false;
        stopMusic();
        showMessage("Du bist gestorben", () -> getGameController().startNewGame());
        player = null;
    }

    private void playMusic() {
        try {
            if (backgroundMusic == null) {
                backgroundMusic = new MediaPlayer(new Media(getClass().getResource("/assets/levels/MarkoMusic.mp3").toExternalForm()));
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); backgroundMusic.setVolume(0.5);
            }
            backgroundMusic.seek(Duration.ZERO); backgroundMusic.play();
        } catch (Exception e) { System.err.println("Musikfehler"); }
    }
    private void stopMusic() { if (backgroundMusic != null) backgroundMusic.stop(); }

    public static void main(String[] args) { launch(args); }
}