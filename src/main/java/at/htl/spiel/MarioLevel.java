package at.htl.spiel;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MarioLevel extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("HTL Mario Test");
    }

    @Override
    protected void initGame() {
        // 1. Hintergrund auf Blau setzen
        getGameScene().setBackgroundColor(javafx.scene.paint.Color.BLUE);

        try {
            // 2. Level laden
            setLevelFromMap("level1.tmx");
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}