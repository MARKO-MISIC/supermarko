module at.htl.spiel {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.almasb.fxgl.all;

    opens at.htl.spiel to javafx.fxml, com.almasb.fxgl.core;

    opens assets.tmx;
    opens assets.textures;

    exports at.htl.spiel;
}