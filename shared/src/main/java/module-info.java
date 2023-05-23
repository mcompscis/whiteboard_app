/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

module shared {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires kotlinx.serialization.json;
    requires spring.messaging;
    requires exposed.core;

    requires com.fasterxml.jackson.databind;

    opens cs.shared.project to javafx.graphics, javafx.fxml;
    exports cs.shared.project.persistence.model;
    exports cs.shared.project.presentation.model;
    exports cs.shared.project.model;
    exports cs.shared.project.persistence;
    exports cs.shared.project.presentation.interfaces;
    opens cs.shared.project.model to com.fasterxml.jackson.databind;

    exports cs.shared.project;
}