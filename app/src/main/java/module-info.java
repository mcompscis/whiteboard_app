/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

module app {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires kotlinx.serialization.json;
    requires javafx.graphics;

    requires shared;
    requires spring.core;
    requires spring.messaging;
    requires spring.websocket;
    requires java.sql;
    requires org.slf4j;
    requires org.apache.tomcat.embed.websocket;
    requires spring.boot.starter.websocket;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    opens cs.project to javafx.graphics, javafx.fxml;
    opens cs.project.presentation.controller to javafx.graphics, javafx.fxml, org.testfx.junit5;
    exports cs.project;
    exports cs.project.presentation.controller;
}