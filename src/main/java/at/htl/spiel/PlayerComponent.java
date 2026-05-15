package at.htl.spiel;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import static com.almasb.fxgl.dsl.FXGL.*;

public class PlayerComponent extends Component {

    private final AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk;

    // frameOffset bestimmt, welche Zeile im Spritesheet genutzt wird
    // 0 = Erste Zeile (Normaler Mario)
    // 16 = Zweite Zeile (Andere Dimension / Weißer Mario)
    private int frameOffset = 0;

    public PlayerComponent() {
        // Initialisiere die Animationen beim Start
        updateAnimationChannels();
        texture = new AnimatedTexture(animIdle);
    }

    /**
     * Definiert die Bildbereiche basierend auf dem aktuellen frameOffset.
     * Nutzt das Spritesheet mario_and_items.png mit 16 Spalten.
     */
    private void updateAnimationChannels() {
        // Stehen: Nutzt das erste Bild der jeweiligen Zeile
        animIdle = new AnimationChannel(
                image("../levels/mario_and_items.png"),
                16, 18, 36, Duration.seconds(1),
                frameOffset + 0, frameOffset + 0
        );

        // Laufen: Nutzt Bilder 4 bis 6 der jeweiligen Zeile für die Bewegung
        animWalk = new AnimationChannel(
                image("../levels/mario_and_items.png"),
                16, 18, 36, Duration.seconds(0.5),
                frameOffset + 4, frameOffset + 6
        );
    }

    @Override
    public void onAdded() {
        // Fügt die Textur dem Entity-View hinzu
        entity.getViewComponent().addChild(texture);
    }

    public void moveRight() {
        getEntity().setScaleX(1); // Normal ausrichten
        if (texture.getAnimationChannel() != animWalk) {
            texture.loopNoOverride(animWalk);
        }
    }

    public void moveLeft() {
        getEntity().setScaleX(-1); // Spiegeln für Linkslauf
        if (texture.getAnimationChannel() != animWalk) {
            texture.loopNoOverride(animWalk);
        }
    }

    public void stop() {
        if (texture.getAnimationChannel() != animIdle) {
            texture.loopNoOverride(animIdle);
        }
    }

    /**
     * Wechselt zwischen der ersten und zweiten Zeile des Spritesheets.
     */
    public void changeDimension() {
        frameOffset = (frameOffset == 0) ? 16 : 0;
        updateAnimationChannels();

        // Sofort die Textur aktualisieren, damit die Farbe wechselt
        if (texture.getAnimationChannel() == animWalk) {
            texture.loopNoOverride(animWalk);
        } else {
            texture.loopNoOverride(animIdle);
        }
    }
}