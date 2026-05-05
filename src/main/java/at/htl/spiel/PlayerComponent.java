package at.htl.spiel;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

public class PlayerComponent extends Component {

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk;

    public PlayerComponent() {
        // animIdle: Nutzt den ersten Frame (Index 0)
        animIdle = new AnimationChannel(
                com.almasb.fxgl.dsl.FXGL.image("../levels/mario_and_items.png"),
                16, 18, 36, Duration.seconds(1), 0, 0);

        // animWalk: Nutzt z.B. Frame 4 bis 6 für die Lauf-Animation
        animWalk = new AnimationChannel(
                com.almasb.fxgl.dsl.FXGL.image("../levels/mario_and_items.png"),
                16, 18, 36, Duration.seconds(0.5), 4, 6);

        texture = new AnimatedTexture(animIdle);
    }

    @Override
    public void onAdded() {
        // Fügt die animierte Textur dem Entity hinzu
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        // Wenn Mario sich nicht bewegt, idle zeigen
        // (Wird über die Steuerung in MarioLevel getriggert)
    }

    public void moveRight() {
        getEntity().setScaleX(1); // Normal schauen
        if (texture.getAnimationChannel() != animWalk) {
            texture.loop(animWalk);
        }
    }

    public void moveLeft() {
        getEntity().setScaleX(-1); // Spiegeln
        if (texture.getAnimationChannel() != animWalk) {
            texture.loop(animWalk);
        }
    }

    public void stop() {
        texture.loop(animIdle);
    }
}