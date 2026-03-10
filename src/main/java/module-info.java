module at.htl.spiel {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens at.htl.spiel to javafx.fxml;
    exports at.htl.spiel;
}