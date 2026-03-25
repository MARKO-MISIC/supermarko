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
        // Wir nutzen ein rotes Rechteck als Platzhalter, damit wir sehen, ob er spawnt
        return entityBuilder(data)
                .viewWithBBox(new Rectangle(30, 30, Color.RED))
                .build();
    }

    // Dieser Joker fängt alle unbekannten Objekte ab und verhindert den Absturz
    @Spawns("")
    public Entity empty(SpawnData data) {
        return entityBuilder(data).build();
    }
}