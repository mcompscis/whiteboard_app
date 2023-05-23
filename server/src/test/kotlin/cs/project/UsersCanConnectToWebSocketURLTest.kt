/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import cs.shared.project.persistence.Connect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.commons.logging.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersCanConnectToWebSocketURLTest {
    @LocalServerPort
    private val randomServerPort = 0
    private val logger = LoggerFactory.getLogger(UsersCanConnectToWebSocketURLTest::class.java)

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
    fun singleUserCanConnectToWebSocketURL() {
        val client = WebSocketStompClient(StandardWebSocketClient())

        // queue to hold messages that client adds such as "Connected to server"
        val clientMessages: BlockingQueue<String> = LinkedBlockingQueue()
        client.messageConverter = MappingJackson2MessageConverter()

        // WHEN: Client connects to WebSocket URL
        client.connect(
            "ws://localhost:${randomServerPort}/whiteboard/websocket",
            object : StompSessionHandlerAdapter() {
                override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
                    clientMessages.offer("Connected to server")
                }
            }
        )

        // THEN: Client received a Connected to server response
        val response = clientMessages.poll(1, TimeUnit.SECONDS)
        assert(response == "Connected to server")
    }

    @Test
    fun multipleUsersCanConnectToWebSocketURL() {
        val numClients = 5

        val clients = Array(numClients) { WebSocketStompClient(StandardWebSocketClient()) }
        // queue to hold messages that client adds such as "Connected to server"
        val clientsMessages: BlockingQueue<String> = LinkedBlockingQueue()

        // WHEN: Multiple clients connect to WebSocket URL
        clients.forEach {
            run {
                it.messageConverter = MappingJackson2MessageConverter()
                it.connect(
                    "ws://localhost:${randomServerPort}/whiteboard/websocket",
                    object : StompSessionHandlerAdapter() {
                        override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
                            clientsMessages.offer("Connected to server")
                        }
                    }
                )
            }
        }

        // THEN: Each client received a connected response
        for (i in 1..numClients) {
            val response = clientsMessages.poll(1, TimeUnit.SECONDS)
            assert(response == "Connected to server")
        }
    }
}