package at.htl;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class MarioLevel extends Application {

    private final int TILE_SIZE = 40;
    private final int ROWS = 15;
    private final int COLS = 60;

    // Spieler-Variablen
    private Rectangle player;
    private double playerX = 100;
    private double playerY = 100;

    // Speichert, welche Tasten gerade gedrückt werden
    private Set<KeyCode> pressedKeys = new HashSet<>();

    @Override
    public void start(Stage stage) {
        Pane world = new Pane(); // Die gesamte Welt
        Pane root = new Pane(world); // Der sichtbare Ausschnitt

        // Map bauen (wie vorher)
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (r >= ROWS - 2) { // Boden zeichnen
                    Rectangle tile = new Rectangle(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    tile.setFill(Color.BROWN);
                    world.getChildren().add(tile);
                }
            }
        }

        // Spieler erstellen
        player = new Rectangle(playerX, playerY, 30, 30);
        player.setFill(Color.RED);
        world.getChildren().add(player);

        Scene scene = new Scene(root, 800, ROWS * TILE_SIZE);

        // Tastatur-Eingaben registrieren
        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        // Der Game Loop (läuft ca. 60 mal pro Sekunde)
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                // Kamera folgt dem Spieler
                world.setTranslateX(-playerX + 200);
            }
        };
        timer.start();

        stage.setTitle("Mario Prototyp - Steuerung");
        stage.setScene(scene);
        stage.show();
    }

    private void update() {
        if (pressedKeys.contains(KeyCode.D)) playerX += 5;
        if (pressedKeys.contains(KeyCode.A)) playerX -= 5;
        if (pressedKeys.contains(KeyCode.W)) playerY -= 5;
        if (pressedKeys.contains(KeyCode.S)) playerY += 5;

        // Spieler-Position im Grafik-Objekt aktualisieren
        player.setX(playerX);
        player.setY(playerY);
    }

    public static void main(String[] args) {
        launch(args);
    }
}