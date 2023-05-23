/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import javafx.scene.input.KeyCode
import javafx.stage.Stage
import org.testfx.api.FxRobot

object TestHelper {
    fun login(robot: FxRobot, appStage: Stage) {
        // only login when needed
        appStage.scene.lookup("#usernameField") ?: return
        robot.clickOn("#usernameField")
        robot.type(KeyCode.T, KeyCode.E, KeyCode.S, KeyCode.T)
        robot.clickOn("#passwordField")
        robot.type(KeyCode.T, KeyCode.E, KeyCode.S, KeyCode.T)
        robot.clickOn("#submitButton")
    }
}