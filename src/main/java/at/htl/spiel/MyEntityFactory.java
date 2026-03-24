package at.htl.spiel;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MyEntityFactory implements EntityFactory {

    @Spawns("ground")
    public Entity newGround(SpawnData data) {
        return entityBuilder(data)
                .viewWithBBox((Node) data.get("tile"))
                .with(new CollidableComponent(true))
                .with(new PhysicsComponent()) // Standardmäßig statisch
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        // Ersten Mario 18x36 ausschneiden
        var view = texture("mario_and_items.png")
                .subTexture(new Rectangle2D(0, 0, 18, 36));

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        // Falls Mario umkippt, fixen wir das später über das Body-Objekt.
        // Für den ersten Test lassen wir es weg, um Fehler zu vermeiden.

        return entityBuilder(data)
                .viewWithBBox(view)
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }
}