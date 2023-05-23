/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import com.fasterxml.jackson.databind.ObjectMapper
import cs.shared.project.model.AuthenticationMessage
import cs.shared.project.persistence.Connect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File
import java.util.*

// TESTS: To Test Signup and Login REST Endpoints

@SpringBootTest
@AutoConfigureMockMvc
class SignupAndLoginTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        val connect = Connect()
        connect.dbConnect()
    }

    @AfterEach
    fun cleanup() {
        val file = File("database/whiteboardapp.db")
        if (file.exists()) {
            file.delete()
        }
    }


    @Test
    fun testSignUp() {
        val msg = AuthenticationMessage("user", "pass")
        val jsonMsg = Json.encodeToString(msg)

        val request = post("/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(jsonMsg)

        val result = mockMvc.perform(request)
            .andExpect(status().isOk)
            .andReturn()

        // We expect result to return a string uuid if valid response
        println(isValidUUID(result.response.contentAsString))
    }

    @Test
    fun testSignUpAndLoginWorks() {
        val msg = AuthenticationMessage("user", "pass")
        val jsonMsg = Json.encodeToString(msg)

        var request = post("/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(jsonMsg)

        var result = mockMvc.perform(request)
            .andExpect(status().isOk)
            .andReturn()

        request = post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(jsonMsg)

        result = mockMvc.perform(request)
            .andExpect(status().isOk)
            .andReturn()

        // We expect result to return a string uuid if valid response
        println(isValidUUID(result.response.contentAsString))
    }

    @Test
    fun cannotLoginWithoutSigningUp() {
        val msg = AuthenticationMessage("user", "pass")
        val jsonMsg = Json.encodeToString(msg)


        val request = post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(jsonMsg)

        val result = mockMvc.perform(request)
            .andExpect(status().isOk)
            .andReturn()

        // We expect result to return a string null if login credentials are not valid
        assert(!isValidUUID(result.response.contentAsString))
    }

    fun isValidUUID(string: String): Boolean {
        return try {
            val uuid = UUID.fromString(string)
            true;
            //do something
        } catch (exception: IllegalArgumentException) {
            false
        }
    }
}