/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.api.FxAssert.verifyThat
import org.testfx.matcher.base.NodeMatchers.isVisible

internal class AppTest : TestFXBase() {

    @Test
    fun loginScreenComponentsAreVisible() {
        // Only run this test if login screen visible
        appStage.scene.lookup("#usernameField") ?: return

        val username = lookup("#usernameField")
        val password = lookup("#passwordField")
        val submit = lookup("#submitButton")
        val signup = lookup("#signupButton")

        verifyThat(username, isVisible())
        verifyThat(password, isVisible())
        verifyThat(submit, isVisible())
        verifyThat(signup, isVisible())
    }

    @Test
    fun signupScreenComponentsAreVisible() {
        // Only run this test if login screen visible
        appStage.scene.lookup("#usernameField") ?: return

        robot.clickOn("#signupButton")

        val username = lookup("#usernameField")
        val password = lookup("#passwordField")
        val password2 = lookup("#passwordField2")
        val signup = lookup("#signupButton")
        val back = lookup("#backButton")
        verifyThat(username, isVisible())
        verifyThat(password, isVisible())
        verifyThat(password2, isVisible())
        verifyThat(signup, isVisible())
        verifyThat(back, isVisible())

        robot.clickOn("#backButton")
    }


    @Test
    fun mainComponentsOnScreenAreVisible() {
        TestHelper.login(robot, appStage)

        println(appStage.title)
        assertTrue(appStage.title.equals("Whiteboard app"))

        val drawingPane = lookup("#mainPane")

        val whiteboard = lookup("#whiteboardTabPane")

        val toolBar = lookup("#toolBar")

        val menuBar = lookup("#menuBar")

        val detailTab = lookup("#detailTab")

        verifyThat(drawingPane, isVisible())
        verifyThat(whiteboard, isVisible())
        verifyThat(toolBar, isVisible())
        verifyThat(menuBar, isVisible())
        verifyThat(detailTab, isVisible())
    }
    @Test
    fun toolBarComponentsAreVisible() {
        TestHelper.login(robot, appStage)

        val penButton = lookup("#penButton")
        val rectangleButton = lookup("#rectangleButton")
        val circleButton = lookup("#circleButton")
        val lineButton = lookup("#lineButton")
        val textBoxButton = lookup("#textBoxButton")
        val selectButton = lookup("#selectButton")
        val eraseButton = lookup("#eraseButton")

        verifyThat(penButton, isVisible())
        verifyThat(rectangleButton, isVisible())
        verifyThat(circleButton, isVisible())
        verifyThat(lineButton, isVisible())
        verifyThat(textBoxButton, isVisible())
        verifyThat(selectButton, isVisible())
        verifyThat(eraseButton, isVisible())
    }

    @Test
    fun detailBarComponentsAreVisible() {
        TestHelper.login(robot, appStage)
        val titlePane = lookup("#titlePane")
        val thinOption = lookup("#thin")
        val color = lookup("#color")
        val font = lookup("#font")

        verifyThat(titlePane, isVisible())
        verifyThat(thinOption, isVisible())
        verifyThat(color, isVisible())
        verifyThat(font, isVisible())
    }

}