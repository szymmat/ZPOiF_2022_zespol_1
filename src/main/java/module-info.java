module com.example.currencyRateVisualizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.net.http;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires java.compiler;

    opens com.example.currencyRateVisualizer to javafx.fxml;
    exports com.example.currencyRateVisualizer;
    exports com.example.currencyRateVisualizer.tableModels;
    exports com.example.currencyRateVisualizer.chartModels;
    opens com.example.currencyRateVisualizer.tableModels to javafx.fxml;
}