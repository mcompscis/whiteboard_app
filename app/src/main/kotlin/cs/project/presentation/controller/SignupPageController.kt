/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller;

import cs.project.business.ClientSocket
import cs.shared.project.model.AuthenticationMessage
import cs.shared.project.persistence.UserDataDao
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.stage.Window
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SignupPageController {
    lateinit var signupButton: Button
    lateinit var passwordField2: PasswordField
    lateinit var passwordField: PasswordField
    lateinit var usernameField: TextField
    private lateinit var loginScene: Scene

    fun setLoginScene(scene: Scene) {
        loginScene = scene
    }

    fun signup() {
        val owner = signupButton.scene.window
        if (!validateInput(owner)) return

        if (!post(AuthenticationMessage(usernameField.text, passwordField.text))) {
            showAlert(AlertType.INFORMATION, owner, "Signup Failed", "There's an error signing you up", true)
        } else {
            showInfoAlert(owner, "Success", "Signup successful! Please log in using your new credentials")
            (owner as Stage).scene = loginScene
        }
    }

    private fun validateInput(owner: Window): Boolean {
        var isValid = true

        if (usernameField.text.isEmpty()) {
            showAlert(
                AlertType.ERROR,
                owner, "Error!",
                "Please enter your username"
            )
            isValid = false
        } else if (passwordField.text.isEmpty()) {
            showAlert(
                AlertType.ERROR,
                owner, "Error!",
                "Please enter a password"
            )
            isValid = false
        } else if (passwordField2.text.isEmpty()) {
            showAlert(
                AlertType.ERROR,
                owner, "Error!",
                "Please re-enter the same password"
            )
            isValid = false
        } else if (passwordField2.text != passwordField.text) {
            showAlert(
                AlertType.ERROR,
                owner, "Error!",
                "Password mismatch"
            )
            isValid = false
        }

        return isValid
    }

    private fun showAlert(
        alertType: AlertType,
        owner: Window,
        title: String,
        message: String,
        shouldBlock: Boolean = false
    ) {
        val alert = Alert(alertType)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.initOwner(owner)

        if (shouldBlock) alert.showAndWait() else alert.show()
    }

    private fun showInfoAlert(owner: Window, title: String, message: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.initOwner(owner)
        alert.showAndWait()
    }

    fun back() {
        (signupButton.scene.window as Stage).scene = loginScene
    }

    private fun post(message: AuthenticationMessage): Boolean {
        val string = Json.encodeToString(message)

        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://${ClientSocket.serverUrl}:8080/signup"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(string))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body().toBoolean()
    }

}
