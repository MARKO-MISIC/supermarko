package at.htl.spiel;

import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static com.almasb.fxgl.dsl.FXGL.*;
import at.htl.spiel.MarioLevel.EntityType;

public class MyEntityFactory implements EntityFactory {

    @Spawns("ground")
    public Entity newGround(SpawnData data) {
        int w = data.get("width");
        int h = data.get("height");

        return entityBuilder(data)
                .type(EntityType.GROUND)
                .viewWithBBox(new Rectangle(w, h, Color.TRANSPARENT))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        var view = texture("../levels/mario_and_items.png")
                .subTexture(new Rectangle2D(0, 0, 18, 36));

        return entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(view)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("")
    public Entity empty(SpawnData data) {
        return entityBuilder(data).build();
    }
}