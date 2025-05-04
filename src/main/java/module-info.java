module com.dataflow.textprocessing {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;

    opens com.dataflow.textprocessing to javafx.fxml;
    opens com.dataflow.textprocessing.controller to javafx.fxml;

    exports com.dataflow.textprocessing;
    exports com.dataflow.textprocessing.controller;
    exports com.dataflow.textprocessing.service;
    exports com.dataflow.textprocessing.service.impl;
    exports com.dataflow.textprocessing.model;
}
