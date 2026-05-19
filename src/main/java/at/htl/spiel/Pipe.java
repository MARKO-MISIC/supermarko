package at.htl.spiel;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Pipe extends ImageView {

    public Pipe(double x, double y) {
        // Lade dein rotes Rohr-Bild (Pfade anpassen)
        setImage(new Image(getClass().getResourceAsStream("/images/red_pipe.png")));
        setX(x);
        setY(y);
        setFitWidth(64);  // Beispielmaße
        setFitHeight(64);
    }

    public Bounds getHitbox() {
        return getBoundsInParent();
    }
}