module com.coffeeshop.coffeeshop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires static lombok;
    requires java.desktop;
    requires jdk.jfr;

    opens com.coffeeshop.coffeeshop to javafx.fxml;
    opens com.coffeeshop.coffeeshop.controller to javafx.fxml;
    opens com.coffeeshop.coffeeshop.model to javafx.base;

    exports com.coffeeshop.coffeeshop;
}
