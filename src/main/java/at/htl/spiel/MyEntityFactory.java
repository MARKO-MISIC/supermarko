package at.htl.spiel;

import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
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
        // Wir entfernen .viewWithBBox(view), da die PlayerComponent
        // die AnimatedTexture selbst zur View hinzufügt.

        return entityBuilder(data)
                .type(EntityType.PLAYER)
                // Eine feste Hitbox definieren (18x36 ist die Sprite-Größe)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(18, 36)))
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())
                .with(new PlayerComponent()) // Hier wird die Animation-Logik geladen
                .build();
    }

    @Spawns("")
    public Entity empty(SpawnData data) {
        return entityBuilder(data).build();
    }
}