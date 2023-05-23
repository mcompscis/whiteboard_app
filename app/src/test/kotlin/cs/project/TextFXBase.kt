/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest


abstract class TestFXBase : ApplicationTest() {

    lateinit var appStage: Stage
    lateinit var robot: FxRobot

    @BeforeEach
    @Throws(Exception::class)
    fun initialChecks() {
        if (::appStage.isInitialized) {
            appStage.close()
        }
        launch(App::class.java)
        appStage = FxToolkit.registerPrimaryStage()
        robot = FxRobot()

        assertNotNull(appStage)
        assertNotNull(robot)
    }
}
