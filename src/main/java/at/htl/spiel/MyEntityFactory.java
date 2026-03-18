package at.htl.spiel;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent; // Dieser Import muss da sein
import javafx.scene.Node;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MyEntityFactory implements EntityFactory {

    @Spawns("ground")
    public Entity newGround(SpawnData data) {
        return entityBuilder(data)
                .viewWithBBox((Node) data.get("tile"))
                .with(new CollidableComponent(true)) // Hier war das "e" zu viel
                .build();
    }
}