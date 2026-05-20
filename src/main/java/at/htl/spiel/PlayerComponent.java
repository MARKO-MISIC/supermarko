package at.htl.spiel;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import static com.almasb.fxgl.dsl.FXGL.*;

public class PlayerComponent extends Component {

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk;
    private int frameOffset = 0;
    private boolean poweredUp = false;

    public PlayerComponent() {
        updateAnimationChannels();
        texture = new AnimatedTexture(animIdle);
    }

    private void updateAnimationChannels() {
        animIdle = new AnimationChannel(
                image("../levels/mario_and_items.png"),
                16, 18, 36, Duration.seconds(1),
                frameOffset, frameOffset
        );

        animWalk = new AnimationChannel(
                image("../levels/mario_and_items.png"),
                16, 18, 36, Duration.seconds(0.5),
                frameOffset + 4, frameOffset + 6
        );
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
    }

    public void setPoweredUp(boolean active) {
        this.poweredUp = active;
        // Optisches Feedback: Marko wird während des Boosts leicht transparent
        entity.getViewComponent().setOpacity(active ? 0.6 : 1.0);
    }

    public boolean isPoweredUp() {
        return poweredUp;
    }

    public void moveRight() {
        getEntity().setScaleX(1);
        if (texture.getAnimationChannel() != animWalk) {
            texture.loopNoOverride(animWalk);
        }
    }

    public void moveLeft() {
        getEntity().setScaleX(-1);
        if (texture.getAnimationChannel() != animWalk) {
            texture.loopNoOverride(animWalk);
        }
    }

    public void stop() {
        if (texture.getAnimationChannel() != animIdle) {
            texture.loopNoOverride(animIdle);
        }
    }

    public void changeDimension() {
        frameOffset = (frameOffset == 0) ? 16 : 0;
        updateAnimationChannels();

        if (texture.getAnimationChannel() == animWalk) {
            texture.loopNoOverride(animWalk);
        } else {
            texture.loopNoOverride(animIdle);
        }
    }
}