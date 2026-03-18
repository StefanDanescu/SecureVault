module com.securevault {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.securevault to javafx.fxml;
    opens com.securevault.controller to javafx.fxml;
    opens com.securevault.model to com.fasterxml.jackson.databind;

    exports com.securevault;
    exports com.securevault.controller;
    exports com.securevault.model;
    exports com.securevault.service;
    exports com.securevault.crypto;
    exports com.securevault.util;
}
