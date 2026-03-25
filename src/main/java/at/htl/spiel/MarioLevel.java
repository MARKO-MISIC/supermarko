package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    private Entity player;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("HTL Mario Test");
        settings.setVersion("1.0");
        // Die rote Zeile mit setAssetRoot einfach LÖSCHEN!
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.D, () -> {
            if (player != null) player.getComponent(PhysicsComponent.class).setVelocityX(200);
        });
        onKey(KeyCode.A, () -> {
            if (player != null) player.getComponent(PhysicsComponent.class).setVelocityX(-200);
        });
        onKeyDown(KeyCode.W, () -> {
            if (player != null) player.getComponent(PhysicsComponent.class).setVelocityY(-350);
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyEntityFactory());
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);

        try {
            // WICHTIG: FXGL sucht standardmäßig in assets/tiled/
            // Mit "../" gehen wir einen Ordner hoch und dann in "levels"
            setLevelFromMap("../levels/level1.tmx");

            player = getGameWorld().spawn("player");

            getGameScene().getViewport().setZoom(1.5);
            getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
            getGameScene().getViewport().setBounds(0, 0, 5000, 720);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}