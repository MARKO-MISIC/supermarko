package at.htl.spiel;

import com.almasb.fxgl.entity.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MyEntityFactory implements EntityFactory {

    @Spawns("ground")
    public Entity newGround(SpawnData data) {
        return entityBuilder(data)
                .viewWithBBox((Node) data.get("tile"))
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        // Wir nutzen das Bild aus assets/levels/
        var view = texture("../levels/mario_and_items.png")
                .subTexture(new javafx.geometry.Rectangle2D(0, 0, 18, 36));

        return entityBuilder(data)
                .viewWithBBox(view)
                .with(new com.almasb.fxgl.entity.components.CollidableComponent(true))
                .build();
    }

    // Dieser Joker fängt alle unbekannten Objekte ab und verhindert den Absturz
    @Spawns("")
    public Entity empty(SpawnData data) {
        return entityBuilder(data).build();
    }
}