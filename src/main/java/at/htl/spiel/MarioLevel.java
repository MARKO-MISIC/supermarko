package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.paint.Color;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("HTL Mario Test");
    }

    @Override
    protected void initGame() {
        // WICHTIG: Erst die Factory registrieren, dann das Level laden
        getGameWorld().addEntityFactory(new MyEntityFactory());
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);

        try {
            // Pfad korrigiert auf deinen Ordner
            setLevelFromMap("../levels/level1.tmx");

            // Falls der Spieler nicht automatisch spawnt, erzwingen wir es hier
            getGameWorld().spawn("player");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}