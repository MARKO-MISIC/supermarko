package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.KeyCode; // DIESER IMPORT HAT GEFEHLT
import javafx.scene.paint.Color;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    // WICHTIG: Die Variable muss HIER stehen, damit alle Methoden darauf zugreifen können
    private Entity player;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("HTL Mario Test");
    }

    @Override
    protected void initInput() {
        // Jetzt wird KeyCode erkannt
        onKey(KeyCode.D, () -> {
            if (player != null) player.translateX(5);
        });
        onKey(KeyCode.A, () -> {
            if (player != null) player.translateX(-5);
        });
        onKeyDown(KeyCode.W, () -> {
            if (player != null) player.translateY(-50);
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);

        try {
            // Der Pfad zu deiner Map
            setLevelFromMap("../levels/level1.tmx");

            // Initialisierung der player-Variable
            player = getGameWorld().spawn("player");

            getGameScene().getViewport().setZoom(1.0);
            getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
            getGameScene().getViewport().setBounds(0, 0, 3000, 720);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}