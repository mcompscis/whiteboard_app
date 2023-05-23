/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller;

import cs.project.business.ClientSocket
import cs.shared.project.model.AuthenticationMessage
import cs.shared.project.persistence.ClientPersistenceDao
import cs.shared.project.persistence.UserDataDao
import cs.shared.project.presentation.model.DisplayModel
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.Duration
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*


class LoginPageController {

    lateinit var submitButton: Button
    lateinit var passwordField: PasswordField
    lateinit var usernameField: TextField
    private lateinit var mainScene: Scene

    fun setMainScene(scene: Scene) {
        mainScene = scene
    }

    fun login() {
        val owner = submitButton.scene.window
        if (!validateInput(owner)) return

        val userId = post(AuthenticationMessage(usernameField.text, passwordField.text))

        if (userId == null) {
            showErrorAlert(owner, "Login Failed", "Please enter correct Email and Password")
        } else {
            usernameField.text = ""
            passwordField.text = ""
            DisplayModel.currentUserId = userId
            ClientPersistenceDao.setUpDisplayModel(userId)
            (owner as Stage).scene = mainScene
        }
    }


    private fun validateInput(owner: Window): Boolean {
        var isValid = true

        if (usernameField.text.isEmpty()) {
            showErrorAlert(
                owner, "Error!",
                "Please enter your username"
            )
            isValid = false
        } else if (passwordField.text.isEmpty()) {
            showErrorAlert(
                owner, "Error!",
                "Please enter a password"
            )
            isValid = false
        }

        return isValid
    }

    private fun showErrorAlert(owner: Window, title: String, message: String) {
        val alert = Alert(AlertType.ERROR)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.initOwner(owner)
        alert.show()
    }

    fun signup() {
        usernameField.text = ""
        passwordField.text = ""
        val loader = FXMLLoader(javaClass.getResource("/fxml/SignupPage.fxml"))
        (submitButton.scene.window as Stage).scene =
            Scene(loader.load(), 700.0, 700.0)
        val controller = loader.getController<SignupPageController>()
        controller.setLoginScene(submitButton.scene)
    }

    private fun post(message: AuthenticationMessage): UUID? {
        val string = Json.encodeToString(message)

        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://${ClientSocket.serverUrl}:8080/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(string))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.body() == "null") return null
        return UUID.fromString(response.body())
    }
}
