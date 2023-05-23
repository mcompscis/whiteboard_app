/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import cs.shared.project.model.JoinMessage
import cs.shared.project.model.NewShapeMessage
import cs.shared.project.model.WhiteboardMessage
import cs.shared.project.persistence.Connect
import cs.shared.project.persistence.model.CircleDataModel
import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.persistence.model.Whiteboard
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.commons.logging.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.io.File
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserAddShapesTest {
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
    fun userCanAddShape() {
        val numClients = 2
        val clients = Array(numClients) { WebSocketStompClient(StandardWebSocketClient()) }

        // queue to hold messages per client
        val clientsMessages: Array<BlockingQueue<String>> = Array(numClients) { LinkedBlockingQueue() }
        val queueOfSessions: BlockingQueue<StompSession> = LinkedBlockingQueue()
        val clientSessions: Array<StompSession?> = Array(numClients) { null }

        for (i in 0 until numClients) {
            val client = clients[i]
            client.messageConverter = MappingJackson2MessageConverter()

            client.connect(
                "ws://localhost:${randomServerPort}/whiteboard/websocket",
                object : StompSessionHandlerAdapter() {
                    override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
                        queueOfSessions.offer(session)
                        session.subscribe("/user/topic/join", object : StompFrameHandler {
                            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                val whiteboard =
                                    Json.decodeFromString<Whiteboard>((payload as WhiteboardMessage).whiteboardString!!)
                                clientsMessages[i].offer(whiteboard.id.toString())
                            }

                            override fun getPayloadType(headers: StompHeaders): Type {
                                return WhiteboardMessage::class.java
                            }
                        })
                    }
                }
            )
            clientSessions[i] = queueOfSessions.poll(1, TimeUnit.SECONDS) // wait for the client to connect
        }

        val client1Session = clientSessions[0]
        val client1Messages = clientsMessages[0]
        client1Session!!.send(
            "/app/join",
            JoinMessage(
                id = null,
                userId = UUID.randomUUID(),
                cursorString = Json.encodeToString(CircleDataModel() as ShapeDataModel),
                author = client1Session!!.sessionId
            )
        )

        val client1JoinedWhiteboardId = client1Messages.poll(2, TimeUnit.SECONDS).toInt()

        // WHEN: 2nd client joins client 1's whiteboard
        val client2Messages = clientsMessages[1]
        val client2Session = clientSessions[1]
        client2Session!!.send(
            "/app/join",
            JoinMessage(
                id = client1JoinedWhiteboardId,
                userId = UUID.randomUUID(),
                cursorString = Json.encodeToString(CircleDataModel() as ShapeDataModel),
                author = client2Session!!.sessionId
            )
        )
        val client2JoinedWhiteboardId = client2Messages.poll(1, TimeUnit.SECONDS)

        // both client sockets subscribe new shape topic for this whiteboard
        for (i in 0 until numClients) {
            val session = clientSessions[i]
            session!!.subscribe("/topic/new-shape/$client1JoinedWhiteboardId", object : StompFrameHandler {
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    clientsMessages[i].offer((payload as NewShapeMessage).shapeString!!)
                }

                override fun getPayloadType(headers: StompHeaders): Type {
                    return NewShapeMessage::class.java
                }
            })
        }
        // WHEN: client 1 adds new shape
        client1Session.send(
            "/app/new-shape/$client1JoinedWhiteboardId",
            NewShapeMessage(
                author = client1Session.sessionId,
                shapeString = Json.encodeToString(CircleDataModel(radius = 10.0) as ShapeDataModel),
            )
        )

        // THEN: client2 receives added shape in broadcast
        val addedNewShapeString = client2Messages.poll(2, TimeUnit.SECONDS)
        val addedShape = (Json.decodeFromString<ShapeDataModel>(addedNewShapeString) as CircleDataModel)
        assert(addedShape.shapeType == Whiteboard.ToolType.CIRCLE)
        assert(addedShape.radius == 10.0)
    }
}